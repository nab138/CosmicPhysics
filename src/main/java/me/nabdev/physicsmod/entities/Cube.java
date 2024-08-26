package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.core.Identifier;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.Transform;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.HashMap;

public class Cube extends Entity {

    static Identifier id = new Identifier(Constants.MOD_ID, "cube");
    public static DiscreteDynamicsWorld dynamicsWorld;
    private final RigidBody body;

    private Quaternion rotation = new Quaternion();

    private static Vector3 origin = null;

    private boolean isOriginal = false;

    public static final HashMap<Integer, String> blocks = new HashMap<>();
    public static final HashMap<Integer, RigidBody> blockBodies = new HashMap<>();

    public Cube() {
        super(id.toString());
        Threads.runOnMainThread(
                () -> this.modelInstance = GameSingletons.entityModelLoader
                        .load(this, "model_screen.json", "screen.animation.json", "animation.screen.idle", "cheese.png").getNewModelInstance()
        );
        this.hasGravity = false;

        if (dynamicsWorld == null) {
            isOriginal = true;
            initializeWorld();
            origin = new Vector3(InGame.getLocalPlayer().getPosition());
        }

        // Create a box shape and rigid body for the cube
        CollisionShape boxShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
        Transform startTransform = new Transform();
        startTransform.setIdentity();
        Vector3 offsetPos = new Vector3(InGame.getLocalPlayer().getPosition()).sub(origin);
        startTransform.origin.set(new Vector3f(offsetPos.x, offsetPos.y, offsetPos.z)); // Start at (0, 0, 0)
        float mass = 5.0f;
        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(mass, localInertia);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, null, boxShape, localInertia);
        body = new RigidBody(rbInfo);
        body.setWorldTransform(startTransform);
        // Add friction to the body
        body.setFriction(0.5f);
        dynamicsWorld.addRigidBody(body);
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
    }

    private void processBlocks(Zone zone) {
        Vector3 checkPos = new Vector3();
        Vector3 realPos = new Vector3();
        Transform startTransform = new Transform();
        CollisionShape boxShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(0.0f, localInertia);

        Vector3 myPos = new Vector3((int)position.x, (int)position.y, (int)position.z);

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    checkPos.set(myPos).add(x, y, z);
                    int globalX = (int)checkPos.x;
                    int globalY = (int)checkPos.y;
                    int globalZ = (int)checkPos.z;
                    if (zone.getBlockState(checkPos) == null) continue;
                    String id = zone.getBlockState(checkPos).getBlockId();
                    int hashCode = (int)globalX * 31 * 31 + (int)globalY * 31 + (int)globalZ;
                    String blockId = blocks.get(hashCode);
                    if (blockId != null && blockId.equals(id)) continue;
                    if (id.contains("base:air")) {
                        if (blockId != null) {
                            dynamicsWorld.removeRigidBody(blockBodies.get(hashCode));
                            blocks.remove(hashCode);
                            blockBodies.remove(hashCode);
                            body.activate(true);
                        }
                        continue;
                    }

                    startTransform.setIdentity();
                    realPos.set(checkPos).sub(origin);
                    startTransform.origin.set(new Vector3f(realPos.x + 0.5f, realPos.y+ 0.5f, realPos.z+ 0.5f));
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

    @Override
    public void update(Zone zone, double delta) {
        processBlocks(zone);

        if (isOriginal) {
            dynamicsWorld.stepSimulation((float) delta, 50);
        }

        // Update the position and rotation from the physics simulation
        Transform trans = new Transform();
        body.getWorldTransform(trans);
        Vector3f pos = trans.origin;
        Quat4f rot = new Quat4f();
        trans.getRotation(rot);
        rotation = new Quaternion(rot.x, rot.y, rot.z, rot.w);

        position.set(pos.x, pos.y, pos.z).add(origin);


        this.getBoundingBox(this.globalBoundingBox);
    }

    @Override
    public void render(Camera camera) {
        if (this.modelInstance == null) return;
        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (camera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            tmpModelMatrix.translate(tmpRenderPos);
            tmpModelMatrix.rotate(rotation);

            this.renderModelAfterMatrixSet(camera);
        }
    }

    @Override
    public void hit(float amount) {
//        Vector3 force = new Vector3(0, 20, 0);
//        body.activate(true);
//        body.applyCentralImpulse(new Vector3f(force.x, force.y, force.z));
    }
}