package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.core.Identifier;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.linearmath.Transform;
import me.nabdev.physicsmod.utils.ICameraOwner;
import me.nabdev.physicsmod.items.Launcher;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsWorld;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import static me.nabdev.physicsmod.utils.PhysicsWorld.processBlocks;

public class Cube extends Entity implements IPhysicsEntity {

    public static Identifier id = new Identifier(Constants.MOD_ID, "cube");

    private final RigidBody body;
    public Quaternion rotation = new Quaternion();
    public boolean isMagnet = false;


    public Cube() {
        super(id.toString());
        Threads.runOnMainThread(
                () -> this.modelInstance = GameSingletons.entityModelLoader
                        .load(this, "model_cube.json", "cube.animation.json", "animation.screen.idle", "cheese.png").getNewModelInstance()
        );
        this.hasGravity = false;

        PhysicsWorld.initialize();


        CollisionShape boxShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
        Transform startTransform = new Transform();
        startTransform.setIdentity();
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        Vector3 offsetPos = PhysicsWorld.getPlayerPos().add(0, 1.5f, 0).add(cam.direction.cpy().scl(2f));
        startTransform.origin.set(new Vector3f(offsetPos.x, offsetPos.y, offsetPos.z));
        float mass = 5.0f;
        Vector3f localInertia = new Vector3f(0, 0, 0);
        boxShape.calculateLocalInertia(mass, localInertia);
        RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass, null, boxShape, localInertia);
        body = new RigidBody(rbInfo);
        body.setWorldTransform(startTransform);
        body.setFriction(1f);

        PhysicsWorld.addCube(this);
    }



    @Override
    public void update(Zone zone, double delta) {
        if (!PhysicsWorld.isRunning) {
            this.onDeath(zone);
            return;
        }
        processBlocks(zone, this);

        if (isMagnet) {
            Player player = InGame.getLocalPlayer();
            Vector3 playerPos = player.getPosition().cpy().add(0, 2, 0);
            PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
            playerPos.add(cam.direction.cpy().scl(2f));
            Vector3f playerPosF = new Vector3f(playerPos.x, playerPos.y, playerPos.z);

            Vector3f myPos = new Vector3f(position.x, position.y, position.z);
            Vector3f dir = new Vector3f(playerPosF);
            dir.sub(myPos);
            dir.scale(3);

            body.setLinearVelocity(dir);
        }

        // Update the position and rotation from the physics simulation
        Transform trans = new Transform();
        body.getWorldTransform(trans);
        Vector3f pos = trans.origin;
        Quat4f rot = new Quat4f();
        trans.getRotation(rot);
        rotation = new Quaternion(rot.x, rot.y, rot.z, rot.w);

        position.set(pos.x, pos.y, pos.z).add(PhysicsWorld.getOrigin());


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
    public void onUseInteraction(Zone zone, Player player, ItemStack heldItemStack) {
        if(heldItemStack == null) return;
        if (heldItemStack.getItem().getID().equals(Launcher.id.toString())) {
            if (isMagnet) return;
            PhysicsWorld.magnet(this);
        }
    }

    public void setVelocity(Vector3 vel) {
        body.setLinearVelocity(new Vector3f(vel.x, vel.y, vel.z));
    }

    @Override
    public void hit(float amount){
        if(isMagnet){
            PhysicsWorld.dropMagnet();
        }
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        this.setVelocity(cam.direction.cpy().scl(20));
    }

    @Override
    public RigidBody getBody() {
        return body;
    }

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox box = new BoundingBox();
        getBoundingBox(box);
        return box;
    }

    @Override
    public void setMagneted(boolean magneted) {
        isMagnet = magneted;
    }

    @Override
    public Vector3 getPosition() {
        return position;
    }

    @Override
    public void onDeath(Zone zone) {
        PhysicsWorld.removeCube(this);
        super.onDeath(zone);
    }
}