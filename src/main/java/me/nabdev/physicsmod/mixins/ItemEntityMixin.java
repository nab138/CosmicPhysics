package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import de.pottgames.tuningfork.SoundBuffer;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.networking.packets.ContainerSyncPacket;
import finalforeach.cosmicreach.networking.packets.sounds.PlaySound3DPacket;
import finalforeach.cosmicreach.networking.server.ServerIdentity;
import finalforeach.cosmicreach.networking.server.ServerSingletons;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.EntityRenderRotationPacket;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.IPhysicsItem;
import me.nabdev.physicsmod.utils.PhysicsUtils;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements IPhysicsEntity, IPhysicsItem {
    @Unique
    private PhysicsRigidBody physicsMod$body;

    @Unique
    public Quaternion physicsMod$rotation = new Quaternion();

    @Unique
    public Quaternion physicsMod$lastRotation = new Quaternion();

    @Unique
    public Vector3f physicsMod$playerDropped = null;


    @Unique
    public Player physicsMod$magnetPlayer = null;

    @Shadow
    ItemStack itemStack;

    @Shadow
    float renderSize;

    public ItemEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }

    @Shadow
    protected abstract void die(Zone zone);

    @Unique
    public Zone physicsMod$currentZone;

    @Unique
    private static final EntityRenderRotationPacket physicsMod$rotationPacket = new EntityRenderRotationPacket();

    @Override
    public void onDeath() {
        super.onDeath();
        if (physicsMod$body != null) PhysicsWorld.removeEntity(this);
    }

    @Shadow
    float maxAge;

    @Shadow
    public float minPickupAge;

    @Shadow
    float followTime;

    @Shadow
    boolean isFollowed;

    @Shadow
    public static SoundBuffer pickupSound;

    @Shadow
    public static Identifier pickupSoundId;


    /**
     * @author nab138
     * @reason physics
     */
    @Overwrite
    public void update(Zone zone, double deltaTime) {
        physicsMod$currentZone = zone;
        if (!PhysicsWorld.isRunning) {
            if (physicsMod$body == null) PhysicsWorld.initialize();
            else die(zone);
            return;
        }
        hasGravity = false;
        if (physicsMod$body == null) {
            BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f).mult(renderSize));
            physicsMod$body = new PhysicsRigidBody(boxShape, 0.5f);
            physicsMod$body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            physicsMod$body.setFriction(1f);

            PhysicsWorld.addEntity(this);

            if (physicsMod$playerDropped != null)
                physicsMod$body.setLinearVelocity(physicsMod$playerDropped.mult(6f));
        }
        if (physicsMod$magnetPlayer != null) PhysicsUtils.applyMagnetForce(physicsMod$magnetPlayer, position, physicsMod$body);

        PhysicsWorld.alertChunk(zone, zone.getChunkAtPosition(this.position));
        Vector3f pos = physicsMod$body.getPhysicsLocation(null);
        com.jme3.math.Quaternion rot = physicsMod$body.getPhysicsRotation(null);
        physicsMod$rotation = new Quaternion(rot.getX(), rot.getY(), rot.getZ(), rot.getW());
        position.set(pos.x, pos.y, pos.z);

        this.getBoundingBox(this.globalBoundingBox);
        this.updateEntityChunk(zone);

        if (ServerSingletons.SERVER != null) {
            if (!PhysicsUtils.epsilonEquals(physicsMod$rotation, physicsMod$lastRotation)) {
                physicsMod$rotationPacket.setEntity(this);
                ServerSingletons.SERVER.broadcast(zone, physicsMod$rotationPacket);
            }

            boolean shouldSendPacket = !this.position.epsilonEquals(this.lastPosition);
            shouldSendPacket |= !this.viewDirection.epsilonEquals(this.lastViewDirection);
            if (shouldSendPacket) {
                positionPacket.setEntity(this);
                ServerSingletons.SERVER.broadcast(zone, positionPacket);
            }
        }

        this.lastViewDirection.set(this.viewDirection);

        physicsMod$lastRotation.set(physicsMod$rotation);

        this.age = (float)((double)this.age + deltaTime);

        if (this.age > this.minPickupAge) {
            this.isFollowed = false;
            zone.forEachPlayer((p) -> {
                Entity playerEntity = p.getEntity();
                Vector3 playerPos = playerEntity.position;
                float dist = this.position.dst(playerPos.x, playerPos.y + 1.0F, playerPos.z);
                float distBottom = this.position.dst(playerPos.x, playerPos.y + 0.25F, playerPos.z);
                if (dist < 1.25F || distBottom < 1.0F) {
                    this.viewDirection.set(playerPos).add(playerEntity.viewPositionOffset).sub(this.position).nor();
                    float followSpeed = 15.0F * this.followTime;
                    physicsMod$body.applyCentralImpulse(PhysicsUtils.v3ToV3f(viewDirection.scl(followSpeed)));
                    if (dist < 1.0F) {
                        int originalAmount = this.itemStack.amount;
                        int newAmount;
                        if (p.inventory.addItemStack(this.itemStack)) {
                            newAmount = 0;
                            this.die(zone);
                        } else {
                            newAmount = this.itemStack.amount;
                        }

                        if (originalAmount > newAmount) {
                            if (GameSingletons.soundManager != null) {
                                GameSingletons.soundManager.playSound3D(pickupSound, this.position, 1.0F, MathUtils.random(1.0F, 1.2F));
                            }

                            if (ServerSingletons.SERVER != null) {
                                ServerIdentity conn = ServerSingletons.getConnection(p);
                                conn.send(new PlaySound3DPacket(pickupSoundId, this.position, 1.0F, MathUtils.random(1.0F, 1.2F)));
                                conn.send(new ContainerSyncPacket(0, p.inventory));
                            }
                        }
                    }

                    this.followTime = (float)((double)this.followTime + deltaTime);
                    this.isFollowed = true;
                }

            });
            if (!this.isFollowed) {
                this.followTime = 0.0F;
            }
        }

        BlockState inBlockState = zone.getBlockState(this.position);
        if (inBlockState != null && !inBlockState.walkThrough && (this.collidedX || this.collidedZ)) {
            float floatSpeed = 2.0F;
            this.onceVelocity.add(0.0F, floatSpeed, 0.0F);
        }

        if (this.age > this.maxAge) {
            this.die(zone);
        }
    }

    @Override
    public void render(Camera worldCamera) {
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            if (this.modelInstance == null && itemStack != null) {
                this.modelInstance = GameSingletons.itemEntityModelLoader.load(this.itemStack);
            }

            if (this.modelInstance != null) {
                tmpRenderPos.set(this.lastRenderPosition);
                TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
                tmpRenderPos.set(this.position);
                this.lastRenderPosition.set(tmpRenderPos);
                tmpModelMatrix.idt();
                tmpModelMatrix.translate(tmpRenderPos);
                tmpModelMatrix.scl(renderSize);
                tmpModelMatrix.rotate(physicsMod$rotation);
                tmpModelMatrix.translate(-0.5F, -0.5F, -0.5F);

                this.renderModelAfterMatrixSet(worldCamera);
            }

        }
    }

    @Override
    public void onUseInteraction(Player player, ItemStack heldItemStack) {
        if (heldItemStack == null) return;
        if (heldItemStack.getItem().getID().equals(GravityGun.id.toString())) {
            if (physicsMod$magnetPlayer != null) return;
            PhysicsWorld.magnet(player, this);
        }
    }

    @Override
    public void onAttackInteraction(Entity sourceEntity) {
        if (physicsMod$magnetPlayer != null) {
            PhysicsWorld.dropMagnet(physicsMod$magnetPlayer);
        }

        physicsMod$body.activate(true);
        this.setVelocity(sourceEntity.viewDirection.scl(10));
    }

    @SuppressWarnings("all")
    @Override
    public PhysicsRigidBody getBody() {
        return physicsMod$body;
    }

    @SuppressWarnings("all")
    @Override
    public void forceActivate() {
        physicsMod$body.activate(true);
    }

    @SuppressWarnings("all")
    @Override
    public void setMagnetised(Player magnetPlayer) {
        physicsMod$magnetPlayer = magnetPlayer;
    }

    @SuppressWarnings("all")
    @Unique
    public void setVelocity(Vector3 vel) {
        physicsMod$body.setLinearVelocity(new Vector3f(vel.x, vel.y, vel.z));
    }

    @SuppressWarnings("all")
    @Override
    public void kill() {
        assert physicsMod$currentZone != null;
        die(physicsMod$currentZone);
    }

    @SuppressWarnings("all")
    @Override
    public EntityUniqueId getID() {
        return uniqueId;
    }

    @Override
    public void physicsMod$setPlayerDropped(Vector3f viewDirection) {
        physicsMod$playerDropped = viewDirection;
    }

    @SuppressWarnings("all")
    @Override
    public Quaternion getRotation() {
        return physicsMod$rotation;
    }

    @SuppressWarnings("all")
    @Override
    public void setRenderRotation(Quaternion rotation) {
        this.physicsMod$rotation = rotation;
    }

    @SuppressWarnings("all")
    @Override
    public Zone getZone() {
        return zone;
    }

    @SuppressWarnings("all")
    @Override
    public BoundingBox getBoundingBox() {
        return globalBoundingBox;
    }
}
