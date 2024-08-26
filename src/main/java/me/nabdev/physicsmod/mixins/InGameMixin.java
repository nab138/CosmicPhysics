package me.nabdev.physicsmod.mixins;

import finalforeach.cosmicreach.gamestates.InGame;
import me.nabdev.physicsmod.entities.Cube;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGame.class)
public class InGameMixin {
    @Inject(method = "unloadWorld", at = @At("HEAD"))
    private void unload(CallbackInfo ci) {
        Cube.dynamicsWorld = null;
        Cube.blockBodies.clear();
        Cube.blocks.clear();
    }
}