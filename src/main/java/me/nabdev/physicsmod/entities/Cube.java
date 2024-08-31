package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.game.engine.blocks.models.PuzzleBlockModel;
import com.github.puzzle.game.util.BlockUtil;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.io.CRBinDeserializer;
import finalforeach.cosmicreach.io.CRBinSerializer;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.rendering.entities.EntityModel;
import finalforeach.cosmicreach.rendering.items.ItemModel;
import finalforeach.cosmicreach.world.Sky;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.PhysicsInfuser;
import me.nabdev.physicsmod.utils.ICameraOwner;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsWorld;

import java.util.ArrayList;

public class Cube extends Entity implements IPhysicsEntity {

    public static Identifier id = new Identifier(Constants.MOD_ID, "cube");

    private final PhysicsRigidBody body;
    public Quaternion rotation = new Quaternion();
    public boolean isMagnet = false;
    public float mass = 2.5f;
    public Texture queuedTexture = null;

    private BlockState blockState;
    private Zone currentZone = null;

    private final ArrayList<IPhysicsEntity> linkedEntities = new ArrayList<>();

    public static Texture ropeTexture;

    BlockPosition tmpBlockPos = new BlockPosition(null, 0, 0, 0);
    Color tinyTint = Color.WHITE.cpy();

    public Cube(Vector3f pos, BlockState blockState) {
        super(id.toString());

        Threads.runOnMainThread(
                () -> this.modelInstance = GameSingletons.entityModelLoader
                        .load(this, "model_cube.json", "cube.animation.json", "animation.screen.idle", "cheese.png").getNewModelInstance()
        );
        this.hasGravity = false;
        this.blockState = blockState;

        PhysicsWorld.initialize();
        BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
        setPosition(pos.x, pos.y, pos.z);
        body = new PhysicsRigidBody(boxShape, mass);
        body.setPhysicsLocation(pos);
        body.setFriction(1f);

        PhysicsWorld.addCube(this);

        if(ropeTexture == null) {
            ropeTexture = PhysicsWorld.createStitchedTexture(((PuzzleBlockModel)Block.getInstance("block_metal_panel").getDefaultBlockState().getModel()).getTextures());
        }
    }

    public Cube() {
        this(getSpawnPos(), Block.getInstance("block_cheese").getDefaultBlockState());
    }

    public void read(CRBinDeserializer deserial) {
        super.read(deserial);
        this.localBoundingBox.min.set(-0.5F, -0.5F, -0.5F);
        this.localBoundingBox.max.set(0.5F, 0.5F, 0.5F);
        this.localBoundingBox.update();
        blockState = BlockState.getInstance(deserial.readString("blockID"));
        setTexture(PhysicsWorld.createStitchedTexture(((PuzzleBlockModel)blockState.getModel()).getTextures()));
        body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
        float[] rot = deserial.readFloatArray("rotation");
        rotation = new Quaternion(rot[0], rot[1], rot[2], rot[3]);
        body.setPhysicsRotation(new com.jme3.math.Quaternion(rot[0], rot[1], rot[2], rot[3]));
        if(deserial.readBoolean("isMagnet", false)) {
            PhysicsWorld.magnet(this);
        }
    }

    public void write(CRBinSerializer serial) {
        super.write(serial);
        serial.writeString("blockID", blockState.getSaveKey());
        serial.writeFloatArray("rotation", new float[]{rotation.x, rotation.y, rotation.z, rotation.w});
        serial.writeBoolean("isMagnet", isMagnet);
    }

    public static Vector3f getSpawnPos() {
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        Vector3 offsetPos = PhysicsWorld.getPlayerPos().add(0, 1.5f, 0).add(cam.direction.cpy().scl(2f));
        return new Vector3f(offsetPos.x, offsetPos.y, offsetPos.z);
    }


    @Override
    public void update(Zone zone, double delta) {
        if (!PhysicsWorld.isRunning) {
            this.onDeath(zone);
            return;
        }
        currentZone = zone;
        PhysicsWorld.alertChunk(zone, zone.getChunkAtPosition(this.position));

        if (isMagnet) {
            Player player = InGame.getLocalPlayer();
            Vector3 playerPos = player.getPosition().cpy().add(0, 2, 0);
            PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
            playerPos.add(cam.direction.cpy().scl(2f));
            Vector3f playerPosF = new Vector3f(playerPos.x, playerPos.y, playerPos.z);

            Vector3f myPos = new Vector3f(position.x, position.y, position.z);
            Vector3f dir = new Vector3f(playerPosF);
            dir = dir.subtract(myPos).mult(3);

            body.setLinearVelocity(dir);
            body.activate(true);
        }

        // Update the position and rotation from the physics simulation
        Vector3f pos = body.getPhysicsLocation(null);
        com.jme3.math.Quaternion rot = body.getPhysicsRotation(null);
        rotation = new Quaternion(rot.getX(), rot.getY(), rot.getZ(), rot.getW());

        position.set(pos.x, pos.y, pos.z);


        this.getBoundingBox(this.globalBoundingBox);
        this.updateEntityChunk(zone);
    }

    @Override
    public void render(Camera camera) {
        if (this.modelInstance == null) return;
        if (queuedTexture != null) {
            ((EntityModel) this.modelInstance.getModel()).diffuseTexture = queuedTexture;
            queuedTexture = null;
        }
        //tmpRenderPos.set(this.lastRenderPosition);
        //TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        tmpRenderPos.set(this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (camera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            tmpModelMatrix.translate(tmpRenderPos);
            tmpModelMatrix.rotate(rotation);

            try {
                Entity.setLightingColor(currentZone, position, Sky.currentSky.currentAmbientColor, tinyTint, tmpBlockPos, tmpBlockPos);
            } catch (Exception ignore) {
                tinyTint.set(Color.WHITE.cpy());
            }
//            if (tinyTint.r == 0 && tinyTint.g == 0 && tinyTint.b == 0)
//                ((PhysicsModelInstance) modelInstance).tintSet(tinyTint);
//            else
                ((PhysicsModelInstance) modelInstance).tintSet(tinyTint.add(0.2f, 0.2f, 0.2f, 0));
            this.modelInstance.render(this, camera, tmpModelMatrix);
        }

        if(!PhysicsWorld.queuedLinks.isEmpty()) return;
        for(IPhysicsEntity linkedEntity : linkedEntities) {
            // Place the entity directly between the two linked entities
            Vector3f linkedPosF = linkedEntity.getBody().getPhysicsLocation(null);
            Vector3 linkedPos = new Vector3(linkedPosF.x, linkedPosF.y, linkedPosF.z);
            Vector3 avgPos = position.cpy().add(linkedPos).scl(0.5f);

            tmpModelMatrix.idt();
            tmpModelMatrix.translate(avgPos);

            // Calculate the direction vector and length
            Vector3f dir = linkedPosF.subtract(body.getPhysicsLocation(null));
            float length = dir.length();
            dir.normalize();
            Quaternion rotation = new Quaternion();
            Vector3 forward = new Vector3(0, 0, 1);

            // Calculate the axis of rotation
            Vector3 axis = forward.cpy().crs(new Vector3(dir.x, dir.y, dir.z)).nor();

            // Calculate the angle of rotation
            float angle = angleBetween(forward, new Vector3(dir.x, dir.y, dir.z));

            // Set the rotation quaternion from the axis and angle
            if (axis.isZero()) {
                // Handle the case where the direction is directly forward or backward
                if (forward.dot(new Vector3(dir.x, dir.y, dir.z)) < 0) {
                    rotation.setFromAxisRad(Vector3.X, (float) Math.PI);
                }
            } else {
                rotation.setFromAxisRad(axis, angle);
            }

            // Apply the rotation to the matrix
            tmpModelMatrix.rotate(rotation);
            tmpModelMatrix.scale(0.2f, 0.2f, length);

            Texture old = this.getTexture();
            this.setTexture(ropeTexture);
            this.modelInstance.render(this, camera, tmpModelMatrix);
            this.setTexture(old);
        }
    }

    @Override
    public void onUseInteraction(Zone zone, Player player, ItemStack heldItemStack) {
        if (heldItemStack == null) return;
        if (heldItemStack.getItem().getID().equals(GravityGun.id.toString())) {
            if (isMagnet) return;
            PhysicsWorld.magnet(this);
        } else if (heldItemStack.getItem().getID().equals(PhysicsInfuser.id.toString())) {
            PhysicsInfuser.ignoreNextUse = true;
            solidify();
        } else if (heldItemStack.getItem().getID().equals(Linker.id.toString())) {
            if (Linker.entityOne == null) {
                Linker.entityOne = this;
            } else if (Linker.entityTwo == null && Linker.entityOne != this) {
                Linker.entityTwo = this;
                Linker.link();
            }
        }
    }

    public void setVelocity(Vector3 vel) {
        body.setLinearVelocity(new Vector3f(vel.x, vel.y, vel.z));
    }

    @Override
    public void hit(float amount) {
        if (isMagnet) {
            PhysicsWorld.dropMagnet();
        }

        body.activate(true);
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        this.setVelocity(cam.direction.cpy().scl(12));
    }

    @Override
    public PhysicsRigidBody getBody() {
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
    public void forceActivate() {
        if (body != null) body.activate(true);
    }

    @Override
    public void onDeath(Zone zone) {
        Linker.clearLinksFor(this);
        PhysicsWorld.removeCube(this);
        if(zone != null) super.onDeath(zone);
    }

    @Override
    public void setMass(float mass) {
        if(mass == this.mass || mass < 0) return;
        this.mass = mass;
        body.setMass(mass);
    }

    @Override
    public void linkWith(IPhysicsEntity entity) {
        linkedEntities.add(entity);
    }

    @Override
    public ArrayList<IPhysicsEntity> getLinkedEntities() {
        return linkedEntities;
    }

    @Override
    public void solidify() {
        if(currentZone == null) return;

        BlockUtil.setBlockAt(currentZone, blockState, new Vector3((float)Math.floor(position.x), (float)Math.floor(position.y), (float)Math.floor(position.z)));
        this.onDeath(currentZone);
    }

    public void setTexture(Texture tex) {
        if (this.modelInstance == null) {
            queuedTexture = tex;
            return;
        }
        ((EntityModel) this.modelInstance.getModel()).diffuseTexture = tex;
    }

    public Texture getTexture() {
        return ((EntityModel) this.modelInstance.getModel()).diffuseTexture;
    }

    public static float angleBetween(Vector3 a, Vector3 b) {
        return (float) Math.acos(a.dot(b) / (a.len() * b.len()));
    }
}