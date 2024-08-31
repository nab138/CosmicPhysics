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
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.GameState;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.utils.ICameraOwner;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
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
    private PhysicsRigidBody body;

    @Unique
    public Quaternion rotation = new Quaternion();

    @Unique
    public boolean isMagnet = false;

    @Shadow
    ItemStack itemStack;

    @Shadow
    float renderSize;

    @Shadow protected abstract void die(Zone zone);

    @Inject(method = "die", at = @At("HEAD"))
    public void dieMixin(CallbackInfo ci) {
        if(body != null) PhysicsWorld.removeEntity(this);
    }

    @Inject(method="update", at=@At("TAIL"))
    public void update(Zone zone, double deltaTime, CallbackInfo ci) {
        if (!PhysicsWorld.isRunning) {
            die(zone);
            return;
        }
        hasGravity = false;
        if(body == null){
            BoxCollisionShape boxShape = new BoxCollisionShape(new Vector3f(0.5f, 0.5f, 0.5f).mult(renderSize));
            body = new PhysicsRigidBody(boxShape, 0.5f);
            body.setPhysicsLocation(new Vector3f(position.x, position.y, position.z));
            body.setFriction(1f);

            PhysicsWorld.addEntity(this);
        }
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
        PhysicsWorld.alertChunk(zone, zone.getChunkAtPosition(this.position));
        Vector3f pos = body.getPhysicsLocation(null);
        com.jme3.math.Quaternion rot = body.getPhysicsRotation(null);
        rotation = new Quaternion(rot.getX(), rot.getY(), rot.getZ(), rot.getW());
        position.set(pos.x, pos.y, pos.z);
    }

    @Override
    public void render(Camera worldCamera) {
        if (worldCamera.frustum.boundsInFrustum(this.globalBoundingBox)) {
            if (this.modelInstance == null && itemStack != null) {
                this.modelInstance = GameSingletons.itemEntityModelLoader.load(this.itemStack);
            }

            if (this.modelInstance != null) {
                //tmpRenderPos.set(this.lastRenderPosition);
                //TickRunner.INSTANCE.partTickLerp(tmpRenderPos, this.position);
                tmpRenderPos.set(this.position);
                this.lastRenderPosition.set(tmpRenderPos);
                tmpModelMatrix.idt();
                tmpModelMatrix.translate(tmpRenderPos);
                tmpModelMatrix.scl(renderSize);
                tmpModelMatrix.rotate(rotation);
                tmpModelMatrix.translate(-0.5F, -0.5F, -0.5F);

                this.renderModelAfterMatrixSet(worldCamera);
            }

        }
    }

    @Override
    public void onUseInteraction(Zone zone, Player player, ItemStack heldItemStack) {
        if (heldItemStack == null) return;
        if (heldItemStack.getItem().getID().equals(GravityGun.id.toString())) {
            if (isMagnet) return;
            PhysicsWorld.magnet(this);
        }
    }

    @Override
    public void onAttackInteraction(Entity sourceEntity) {
        if (isMagnet) {
            PhysicsWorld.dropMagnet();
        }

        body.activate(true);
        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
        this.setVelocity(cam.direction.cpy().scl(10));
    }

    @Override
    public PhysicsRigidBody getBody() {
        return body;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return globalBoundingBox;
    }

    @Override
    public void forceActivate() {
        body.activate(true);
    }

    @Override
    public void setMass(float mass) {
        body.setMass(mass);
    }

    @Override
    public void setMagneted(boolean magnet) {
        isMagnet = magnet;
    }

    @Unique
    public void setVelocity(Vector3 vel) {
        body.setLinearVelocity(new Vector3f(vel.x, vel.y, vel.z));
    }


}
