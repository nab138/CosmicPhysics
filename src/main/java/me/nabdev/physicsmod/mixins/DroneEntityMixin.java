package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.entities.DroneEntity;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.IDamageSource;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DroneEntity.class)
public class DroneEntityMixin extends Entity {

    public DroneEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void update(CallbackInfo ci) {
        for (IPhysicsEntity c : PhysicsWorld.allObjects) {
            BoundingBox cubeBB = c.getBoundingBox();
            if (this.globalBoundingBox.intersects(cubeBB)) {
                hit((IDamageSource) c,100);
            }
        }
    }
}
