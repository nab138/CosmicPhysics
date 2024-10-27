package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import finalforeach.cosmicreach.gamestates.InGame;
import me.nabdev.physicsmod.utils.ICameraOwner;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public class InGameMixin implements ICameraOwner {
    @Shadow
    private static PerspectiveCamera rawWorldCamera;

    @Inject(method = "unloadWorld", at = @At("HEAD"))
    private void unload(CallbackInfo ci) {
        PhysicsWorld.reset();
    }

    @Override
    public PerspectiveCamera browserMod$getCamera() {
        return rawWorldCamera;
    }
}