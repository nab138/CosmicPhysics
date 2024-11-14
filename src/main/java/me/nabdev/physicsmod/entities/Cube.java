package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.game.util.BlockUtil;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.rendering.entities.IEntityModelInstance;
import finalforeach.cosmicreach.savelib.crbin.CRBinDeserializer;
import finalforeach.cosmicreach.savelib.crbin.CRBinSerializer;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.PhysicsInfuser;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsUtils;
import me.nabdev.physicsmod.utils.PhysicsWorld;

public class Cube extends Entity implements IPhysicsEntity {
    public static final Identifier id = Identifier.of(Constants.MOD_ID,"cube");

    private final PhysicsRigidBody body;
    private final Quaternion lastRotation = new Quaternion();
    public Quaternion rotation = new Quaternion();
    public Player magnetPlayer = null;
    public float mass = 2.5f;

    private BlockState blockState;
    private Zone currentZone = null;

    private final Array<IPhysicsEntity> linkedEntities = new Array<>(false, 0, IPhysicsEntity.class);

    private Vector3f scale = new Vector3f(1, 1, 1);

    private static final EntityRenderRotationPacket rotationPacket = new EntityRenderRotationPacket();

    private IEntityModelInstance ropeModel = null;


    public Cube(Vector3f pos, BlockState blockState) {
        super(id.toString());

        this.hasGravity = false;
        this.blockState = blockState;

        BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f));
        setPosition(pos.x, pos.y, pos.z);
        body = new PhysicsRigidBody(boxShape, mass);
        body.setPhysicsLocation(pos);
        body.setFriction(1f);

        PhysicsWorld.addCube(this);
    }

    public Cube() {
        this(new Vector3f(0f,0f,0f), Block.getInstance("cheese").getDefaultBlockState());
    }

    public void read(CRBinDeserializer deserialize) {
        super.read(deserialize);
        this.localBoundingBox.min.set(-0.5F, -0.5F, -0.5F);
        this.localBoundingBox.max.set(0.5F, 0.5F, 0.5F);
        this.localBoundingBox.update();
        blockState = BlockState.getInstance(deserialize.readString("blockID"));
        //setTexture(TextureUtils.getTextureForBlock(blockState));
        body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
        float[] rot = deserialize.readFloatArray("rotation");
        rotation = new Quaternion(rot[0], rot[1], rot[2], rot[3]);
        body.setPhysicsRotation(new com.jme3.math.Quaternion(rot[0], rot[1], rot[2], rot[3]));
//        int[] linkedIDs = deserialize.readIntArray("linkedEntities");
//        PhysicsUtils.queueLinks(this, linkedIDs);
    }

    public void write(CRBinSerializer serial) {
        super.write(serial);
        serial.writeString("blockID", blockState.getSaveKey());
        serial.writeFloatArray("rotation", new float[]{rotation.x, rotation.y, rotation.z, rotation.w});
//        int[] linkedIDs = new int[linkedEntities.size];
//        for (int i = 0; i < linkedEntities.size; i++) {
//            linkedIDs[i] = linkedEntities.get(i).getID();
//        }
//        serial.writeIntArray("linkedEntities", linkedIDs);
    }


    @Override
    public void update(Zone zone, double delta) {
        currentZone = zone;
        if (!PhysicsWorld.isRunning) {
            PhysicsWorld.initialize();
            return;
        }
        PhysicsWorld.alertChunk(zone, zone.getChunkAtPosition(this.position));

        if (magnetPlayer != null) PhysicsUtils.applyMagnetForce(magnetPlayer, position, body);

        Vector3f pos = body.getPhysicsLocation(null);
        com.jme3.math.Quaternion rot = body.getPhysicsRotation(null);
        rotation = new Quaternion(rot.getX(), rot.getY(), rot.getZ(), rot.getW());
        position.set(pos.x, pos.y, pos.z);

        this.getBoundingBox(this.globalBoundingBox);
        this.updateEntityChunk(zone);

        if (ServerSingletons.SERVER != null) {
            if (!PhysicsUtils.epsilonEquals(rotation, lastRotation)) {
                rotationPacket.setEntity(this);
                ServerSingletons.SERVER.broadcast(zone, rotationPacket);
            }

            boolean shouldSendPacket;
            shouldSendPacket = !this.position.epsilonEquals(this.lastPosition);
            shouldSendPacket |= !this.viewDirection.epsilonEquals(this.lastViewDirection);
            if (shouldSendPacket) {
                positionPacket.setEntity(this);
                ServerSingletons.SERVER.broadcast(zone, positionPacket);
            }
        }

        this.lastViewDirection.set(this.viewDirection);

        lastRotation.set(rotation);
    }

    @Override
    public void render(Camera camera) {
        if (this.modelInstance == null && this.blockState != null) {
            this.modelInstance = GameSingletons.itemEntityModelLoader.load(new ItemStack(blockState.getItem()));
        }
        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        tmpRenderPos.set(this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (camera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            tmpModelMatrix.translate(tmpRenderPos);
            tmpModelMatrix.rotate(rotation);
            tmpModelMatrix.scale(scale.x, scale.y, scale.z);
            tmpModelMatrix.translate(-scale.x / 2, -scale.y / 2, -scale.z / 2);

            this.modelInstance.render(this, camera, tmpModelMatrix);
        }

        for (IPhysicsEntity linkedEntity : linkedEntities) {
            // Place the entity directly between the two linked entities
            Vector3 linkedPos = linkedEntity.getPosition();
            Vector3 avgPos = position.cpy().add(linkedPos).scl(0.5f);

            tmpModelMatrix.idt();
            tmpModelMatrix.translate(avgPos);

            // Calculate the direction vector and length
            Vector3 dir = linkedPos.sub(position);
            float length = dir.len();
            dir = dir.nor();
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
            //tmpModelMatrix.translate(-0.5f, -0.5f, -0.5f);
            tmpModelMatrix.scale(0.2f, 0.2f, length);
            tmpModelMatrix.translate(-.1f, -.1f, -length / 2);

            if(ropeModel == null){
                ropeModel = GameSingletons.itemEntityModelLoader.load(new ItemStack(Block.getInstance("metal_panel").getDefaultBlockState().getItem()));
            }
            this.ropeModel.render(this, camera, tmpModelMatrix);
        }
    }

    @Override
    public void onUseInteraction(Player player, ItemStack heldItemStack) {
        if (heldItemStack == null) return;
        if (heldItemStack.getItem().getID().equals(GravityGun.id.toString())) {
            if (magnetPlayer != null) {
                if(magnetPlayer.getAccount().getUniqueId().equals(player.getAccount().getUniqueId())) PhysicsWorld.dropMagnet(player);
            } else {
                PhysicsWorld.magnet(player, this);
            }
        } else if (heldItemStack.getItem().getID().equals(PhysicsInfuser.id.toString())) {
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
    public void onAttackInteraction(Entity sourceEntity) {
        if (magnetPlayer != null) {
            PhysicsWorld.dropMagnet(magnetPlayer);
        }

        body.activate(true);
        setVelocity(sourceEntity.viewDirection.scl(12f));
    }

    @Override
    public PhysicsRigidBody getBody() {
        return body;
    }

    @Override
    public Quaternion getRotation() {
        return rotation;
    }

    @Override
    public void setRenderRotation(Quaternion rotation) {
        this.rotation = rotation;
    }

    @Override
    public void setMagnetised(Player magnetPlayer) {
        this.magnetPlayer = magnetPlayer;
    }

    @Override
    public void forceActivate() {
        if (body != null) body.activate(true);
    }

    @Override
    public void onDeath() {
        //Linker.clearLinksFor(this);
        PhysicsWorld.removeCube(this);
        if (zone != null) super.onDeath();
    }

    public void setMass(float mass) {
        if (mass == this.mass || mass < 0) return;
        this.mass = mass;
        body.setMass(mass);
    }

    @Override
    public void linkWith(EntityUniqueId id) {
        Entity entity = zone.getEntity(id);
        if(entity == null) return;
        if(entity instanceof IPhysicsEntity physicsEntity) {
            linkedEntities.add(physicsEntity);
        }
    }

    @Override
    public Array<IPhysicsEntity> getLinkedEntities() {
        return linkedEntities;
    }

    @Override
    public void solidify() {
        if (currentZone == null) return;

        BlockUtil.setBlockAt(currentZone, blockState, new Vector3((float) Math.floor(position.x), (float) Math.floor(position.y), (float) Math.floor(position.z)));
        kill();
    }

    public static float angleBetween(Vector3 a, Vector3 b) {
        return (float) Math.acos(a.dot(b) / (a.len() * b.len()));
    }

    @Override
    public void kill() {
        this.onDeath();
    }

    @Override
    public EntityUniqueId getID() {
        return uniqueId;
    }

    @Override
    public Zone getZone() {
        return zone;
    }

    @Override
    public Vector3 getPosition() {
        return position;
    }

    @SuppressWarnings("unused")
    public void scale(Vector3f scale){
        body.setPhysicsScale(scale);
        this.scale = scale;
    }
}