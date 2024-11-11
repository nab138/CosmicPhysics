package me.nabdev.physicsmod.clientmixins;

import finalforeach.cosmicreach.gamestates.InGame;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public class InGameMixin {
    @Inject(method = "unloadWorld", at = @At("HEAD"))
    private void unload(CallbackInfo ci) {
        PhysicsWorld.reset();
    }
}