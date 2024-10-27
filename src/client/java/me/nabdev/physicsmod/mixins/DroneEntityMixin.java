package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.github.puzzle.core.loader.util.Reflection;
import de.pottgames.tuningfork.SoundBuffer;
import finalforeach.cosmicreach.GameAssetLoader;
import finalforeach.cosmicreach.Threads;
import finalforeach.cosmicreach.entities.DroneEntity;
import finalforeach.cosmicreach.entities.Entity;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(DroneEntity.class)
public class DroneEntityMixin extends Entity {
    @Shadow
    static Array<SoundBuffer> cries;

    @Shadow
    static Array<SoundBuffer> steps;

    @Shadow
    static Array<SoundBuffer> hurts;

    public DroneEntityMixin(String entityTypeId) {
        super(entityTypeId);
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void update(CallbackInfo ci) {
        for (IPhysicsEntity c : PhysicsWorld.allObjects) {
            BoundingBox cubeBB = c.getBoundingBox();
            if (this.globalBoundingBox.intersects(cubeBB)) {
                hit(100);
            }
        }
    }

    @Inject(method = "<clinit>", at = @At("HEAD"), cancellable = true)
    private static void clinit(CallbackInfo ci) {
        cries = new Array<>();
        steps = new Array<>();
        hurts = new Array<>();
        Threads.runOnMainThread(() -> {
            Array<SoundBuffer> cries = Reflection.getFieldContents(DroneEntity.class, "cries");
            cries.add(GameAssetLoader.getSound("sounds/entities/drone_interceptor/drone-cry-1.ogg"));
            cries.add(GameAssetLoader.getSound("sounds/entities/drone_interceptor/drone-cry-2.ogg"));
            cries.add(GameAssetLoader.getSound("sounds/entities/drone_interceptor/drone-cry-3.ogg"));
            Reflection.setFieldContents(DroneEntity.class, "cries", cries);

            Array<SoundBuffer> steps = Reflection.getFieldContents(DroneEntity.class, "steps");
            Array<SoundBuffer> hurts = Reflection.getFieldContents(DroneEntity.class, "hurts");
            steps.add(GameAssetLoader.getSound("sounds/entities/drone_interceptor/drone-step-1.ogg"));
            hurts.add(GameAssetLoader.getSound("sounds/entities/drone_interceptor/drone-hurt-1.ogg"));
            Reflection.setFieldContents(DroneEntity.class, "steps", steps);
            Reflection.setFieldContents(DroneEntity.class, "hurts", hurts);
        });
        ci.cancel();
    }
}
