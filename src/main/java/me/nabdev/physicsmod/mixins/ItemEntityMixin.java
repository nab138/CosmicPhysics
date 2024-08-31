package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.TickRunner;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.utils.ICameraOwner;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsUtils;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements IPhysicsEntity {

    @Unique
    private PhysicsRigidBody physicsMod$body;

    @Unique
    public Quaternion physicsMod$rotation = new Quaternion();


    @Unique
    public boolean physicsMod$isMagnet = false;

    @Shadow
    ItemStack itemStack;

    @Shadow
    float renderSize;

    @Shadow protected abstract void die(Zone zone);

    @Unique
    public Zone physicsMod$currentZone;

    @Override
    public void onDeath(Zone zone) {
        super.onDeath(zone);
        if(physicsMod$body != null) PhysicsWorld.removeEntity(this);
    }

    @Inject(method="update", at=@At("TAIL"))
    public void update(Zone zone, double deltaTime, CallbackInfo ci) {
        physicsMod$currentZone = zone;
        if (!PhysicsWorld.isRunning) {
            if(physicsMod$body == null) PhysicsWorld.initialize();
            else die(zone);
            return;
        }
        hasGravity = false;
        if(physicsMod$body == null){
            BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f).mult(renderSize));
            physicsMod$body = new PhysicsRigidBody(boxShape, 0.5f);
            physicsMod$body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            physicsMod$body.setFriction(1f);

            PhysicsWorld.addEntity(this);
        }
        if (physicsMod$isMagnet) PhysicsUtils.applyMagnetForce(position, physicsMod$body);

        PhysicsWorld.alertChunk(zone, zone.getChunkAtPosition(this.position));
        Vector3f pos = physicsMod$body.getPhysicsLocation(null);
        com.jme3.math.Quaternion rot = physicsMod$body.getPhysicsRotation(null);
        physicsMod$rotation = new Quaternion(rot.getX(), rot.getY(), rot.getZ(), rot.getW());
        position.set(pos.x, pos.y, pos.z);
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
    public void onUseInteraction(Zone zone, Player player, ItemStack heldItemStack) {
        if (heldItemStack == null) return;
        if (heldItemStack.getItem().getID().equals(GravityGun.id.toString())) {
            if (physicsMod$isMagnet) return;
            PhysicsWorld.magnet(this);
        }
    }

    @Override
    public void onAttackInteraction(Entity sourceEntity) {
        if (physicsMod$isMagnet) {
            PhysicsWorld.dropMagnet();
        }

        physicsMod$body.activate(true);
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        this.setVelocity(cam.direction.cpy().scl(10));
    }

    @SuppressWarnings("all")
    @Override
    public PhysicsRigidBody getBody() {
        return physicsMod$body;
    }

    @SuppressWarnings("all")
    @Override
    public BoundingBox getBoundingBox() {
        return globalBoundingBox;
    }

    @SuppressWarnings("all")
    @Override
    public void forceActivate() {
        physicsMod$body.activate(true);
    }

    @SuppressWarnings("all")
    @Override
    public void setMagnetised(boolean magnet) {
        physicsMod$isMagnet = magnet;
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
}
