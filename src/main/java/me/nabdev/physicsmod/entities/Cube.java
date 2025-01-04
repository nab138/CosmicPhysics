package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.game.util.BlockUtil;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.blocks.MissingBlockStateResult;
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
    public final Vector3 accel;

    protected final PhysicsRigidBody body;
    private final Quaternion lastRotation = new Quaternion();
    public Quaternion rotation = new Quaternion();
    public Player magnetPlayer = null;
    public float mass = 2.5f;


    public BlockState blockState;
    private Zone currentZone = null;

    private final Array<IPhysicsEntity> linkedEntities = new Array<>(false, 0, IPhysicsEntity.class);

    private Vector3f scale = new Vector3f(1, 1, 1);

    private static final EntityRenderRotationScalePacket rotationPacket = new EntityRenderRotationScalePacket();

    private IEntityModelInstance ropeModel = null;

    int updateCount;

    protected static final Matrix4 tmpModelMatrix = new Matrix4();
    protected static final Vector3 tmpRenderPos = new Vector3();



    public Cube(Vector3f pos, BlockState blockState) {
        super(id.toString());

        this.hasGravity = false;
        this.blockState = blockState;


        setPosition(pos.x, pos.y, pos.z);
        body = new PhysicsRigidBody(getCollisionMesh(), mass);
        accel = PhysicsUtils.v3fToV3(body.getGravity(new Vector3f()));
        body.setPhysicsLocation(pos);
        body.setFriction((float)frictionInterpolation(blockState.friction));

        PhysicsWorld.addCube(this);
    }

    public Cube() {
        this(new Vector3f(0f,0f,0f), Block.getInstance("cheese").getDefaultBlockState());
    }

    private CompoundCollisionShape getCollisionMesh(){
        return PhysicsUtils.getCollisionMeshForBlock(blockState);
    }

    public void read(CRBinDeserializer deserialize) {
        super.read(deserialize);
        this.localBoundingBox.min.set(-0.5F, -0.5F, -0.5F);
        this.localBoundingBox.max.set(0.5F, 0.5F, 0.5F);
        this.localBoundingBox.update();
        blockState = BlockState.getInstance(deserialize.readString("blockID"), MissingBlockStateResult.MISSING_OBJECT);
        body.setCollisionShape(getCollisionMesh());
        body.setFriction((float)frictionInterpolation(blockState.friction));
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
        lastPosition.set(position);

        if(updateCount < 3) updateCount++;
    }

    @Override
    public void render(Camera camera) {
        if (this.modelInstance == null && this.blockState != null) {
            this.modelInstance = GameSingletons.itemEntityModelLoader.load(new ItemStack(blockState.getItem()));

        }
        Vector3f pos = body.getPhysicsLocation(null);
        com.jme3.math.Quaternion rot = body.getPhysicsRotation(null);
        rotation = new Quaternion(rot.getX(), rot.getY(), rot.getZ(), rot.getW());
        position.set(pos.x, pos.y, pos.z);
        tmpRenderPos.set(this.lastRenderPosition);
        TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
        tmpRenderPos.set(this.position);
        this.lastRenderPosition.set(tmpRenderPos);
        if (camera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            tmpModelMatrix.idt();
            tmpModelMatrix.translate(tmpRenderPos);
            tmpModelMatrix.rotate(rotation);
            tmpModelMatrix.scale(scale.x, scale.y, scale.z);
            tmpModelMatrix.translate(-0.5f, -0.5f, -0.5f);

            this.modelInstance.render(this, camera, tmpModelMatrix);
        }

        for (IPhysicsEntity linkedEntity : linkedEntities) {
            // Place the entity directly between the two linked entities
            Vector3 linkedPos = linkedEntity.getPosition();
            Vector3 avgPos = position.cpy().add(linkedPos).scl(0.5f);
            Vector3 direction = linkedPos.cpy().sub(position).nor();
            float distance = position.dst(linkedPos);

            Quaternion rotation = new Quaternion().setFromCross(Vector3.Z, direction);

            tmpModelMatrix.idt();
            tmpModelMatrix.translate(avgPos);
            tmpModelMatrix.rotate(rotation);
            tmpModelMatrix.scale(0.2f, 0.2f, distance);
            tmpModelMatrix.translate(0, 0, -0.5f);


            if(ropeModel == null){
                ropeModel = GameSingletons.itemEntityModelLoader.load(new ItemStack(Block.getInstance("metal_panel").getDefaultBlockState().getItem()));
            }
            this.ropeModel.render(this, camera, tmpModelMatrix);
        }
    }

    @Override
    public void onUseInteraction(Player player, ItemStack heldItemStack) {
        if(updateCount < 2) return;
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
    public void setPosition(Vector3 position){
        setPosition(position.x, position.y, position.z);
        body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
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
        Linker.clearLinksFor(this);
        PhysicsWorld.removeCube(this);
        if (zone != null) super.onDeath();
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

    @Override
    public BoundingBox getBoundingBox() {
        BoundingBox box = new BoundingBox();
        getBoundingBox(box);
        return box;
    }

    @SuppressWarnings("unused")
    public void scale(Vector3 scale){
        if(scale.equals(getScale())) return;
        Vector3f scaleF = PhysicsUtils.v3ToV3f(scale);
        body.setPhysicsScale(scaleF);
        this.scale = scaleF;
        this.localBoundingBox.min.set(-scale.x/2, -scale.y/2, -scale.z/2);
        this.localBoundingBox.max.set(scale.x/2, scale.y/2, scale.z/2);
        this.localBoundingBox.update();
        getBoundingBox(this.globalBoundingBox);
    }

    @SuppressWarnings("unused")
    public Vector3 getScale(){
        return PhysicsUtils.v3fToV3(scale);
    }

    public static double frictionInterpolation(double x){
        if(x <= 0.001) return 0.0;
        if (x < 1) {
            // Exponential interpolation for x < 1
            return Math.exp(200 * (x - 1));
        } else {
            // Linear function for x >= 1
            return x;
        }
    }
}