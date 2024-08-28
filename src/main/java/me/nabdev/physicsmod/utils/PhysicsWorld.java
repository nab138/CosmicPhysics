package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Vector3;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;

public class PhysicsWorld {
    public static ArrayList<IPhysicsEntity> allObjects = new ArrayList<>();
    public static ArrayList<Cube> cubes = new ArrayList<>();

    public static RigidBody playerBody;

    public static DiscreteDynamicsWorld dynamicsWorld;

    private static Vector3 origin = null;

    public static final HashMap<Integer, String> blocks = new HashMap<>();
    public static final HashMap<Integer, RigidBody> blockBodies = new HashMap<>();

    public static IPhysicsEntity magnetEntity = null;
    public static boolean isMagneting = false;

    public static boolean isRunning = false;

    public static void initialize(){
        if(dynamicsWorld != null || isRunning) return;
        isRunning = true;
        origin = new Vector3(InGame.getLocalPlayer().getPosition());
        initializeWorld();
    }

    private static void initializeWorld() {
        DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
        Dispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);
        Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
        Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
        BroadphaseInterface broadphase = new AxisSweep3(worldAabbMin, worldAabbMax);
        SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();
        dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        dynamicsWorld.setGravity(new Vector3f(0, -9.81f, 0));

        // Create a 2 block tall, 0.5 block wide, 0.5 block deep box for the player
        CollisionShape boxShape = new BoxShape(new Vector3f(0.3f, 1f, 0.3f));
        Transform startTransform = new Transform();
        startTransform.setIdentity();
        startTransform.origin.set(new Vector3f(0, 0f, 0)); // Start at (0, 0, 0)
        float mass = 50.0f;
        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(mass, localInertia);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, null, boxShape, localInertia);
        playerBody = new RigidBody(rbInfo);
        playerBody.setWorldTransform(startTransform);
        playerBody.setGravity(new Vector3f(0, 0f, 0));

        // disable physics, because we will be setting the position manually
        dynamicsWorld.addRigidBody(playerBody);
    }

    public static Vector3 getPlayerPos(){
        return InGame.getLocalPlayer().getPosition().cpy().sub(origin);
    }

    public static Vector3 getOrigin() {
        return origin;
    }

    private static void addRigidBody(RigidBody body){
        dynamicsWorld.addRigidBody(body);
    }

    private static void addEntity(IPhysicsEntity entity){
        allObjects.add(entity);
        addRigidBody(entity.getBody());
    }

    public static void removeCube(Cube cube){
        cubes.remove(cube);
        allObjects.remove(cube);
        dynamicsWorld.removeRigidBody(cube.getBody());
    }

    public static void addCube(Cube cube){
        cubes.add(cube);
        addEntity(cube);
    }

    public static void processBlocks(Zone zone, IPhysicsEntity physicsEntity, int range) {
        Vector3 checkPos = new Vector3();
        Vector3 realPos = new Vector3();
        Transform startTransform = new Transform();
        CollisionShape boxShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(0.0f, localInertia);

        Vector3 myPos = new Vector3((int) physicsEntity.getPosition().x, (int) physicsEntity.getPosition().y, (int) physicsEntity.getPosition().z);

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    checkPos.set(myPos).add(x, y, z);
                    int globalX = (int) checkPos.x;
                    int globalY = (int) checkPos.y;
                    int globalZ = (int) checkPos.z;
                    if (zone.getBlockState(checkPos) == null) continue;
                    BlockState state = zone.getBlockState(checkPos);
                    String id = state.getBlockId();
                    int hashCode = globalX * 31 * 31 + globalY * 31 + globalZ;
                    String blockId = blocks.get(hashCode);
                    if (blockId != null && blockId.equals(id)) continue;
                    if (!isSolid(state)) {
                        if (blockId != null) {
                            dynamicsWorld.removeRigidBody(blockBodies.get(hashCode));
                            blocks.remove(hashCode);
                            blockBodies.remove(hashCode);
                        }
                        continue;
                    }

                    startTransform.setIdentity();
                    realPos.set(checkPos).sub(origin);
                    startTransform.origin.set(new Vector3f(realPos.x + 0.5f, realPos.y + 0.5f, realPos.z + 0.5f));
                    RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(0.0f, null, boxShape, localInertia);
                    RigidBody block = new RigidBody(rbInfo);
                    block.setWorldTransform(startTransform);
                    dynamicsWorld.addRigidBody(block);
                    blocks.put(hashCode, id);
                    blockBodies.put(hashCode, block);
                }
            }
        }
    }

    public static void processBlocks(Zone zone, IPhysicsEntity cube){
        processBlocks(zone, cube, 2);
    }

    public static boolean isSolid(BlockState b){
        return b != null && !b.walkThrough;
    }

    public static void dropMagnet() {
        if(magnetEntity != null) {
            magnetEntity.setMagneted(false);
            magnetEntity = null;
        }
    }

    public static void magnet(IPhysicsEntity entity) {
        dropMagnet();
        isMagneting = true;
        magnetEntity = entity;
        magnetEntity.setMagneted(true);
    }

    public static void reset() {
        dynamicsWorld = null;
        blockBodies.clear();
        allObjects.clear();
        blocks.clear();
        cubes.clear();
        playerBody = null;
        isRunning = false;
        if(magnetEntity != null) {
            magnetEntity.setMagneted(false);
            magnetEntity = null;
        }
    }

    public static void tick(double delta){
        if(!isRunning || dynamicsWorld == null) return;
        dynamicsWorld.stepSimulation((float) delta, 10);
    }
}
