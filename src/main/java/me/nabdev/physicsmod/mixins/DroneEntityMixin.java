package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.entities.DroneEntity;
import finalforeach.cosmicreach.entities.Entity;
import me.nabdev.physicsmod.entities.Cube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DroneEntity.class)
public class DroneEntityMixin extends Entity {
    @Inject(method = "update", at = @At("TAIL"))
    private void update(CallbackInfo ci) {
        for(Cube c : Cube.cubes){
            BoundingBox cubeBB = new BoundingBox();
            c.getBoundingBox(cubeBB);
            if(this.globalBoundingBox.intersects(cubeBB)){
                hit(100);
            }
        }
    }
}
