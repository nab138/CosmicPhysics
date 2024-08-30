package me.nabdev.physicsmod.mixins;

import finalforeach.cosmicreach.rendering.BatchedZoneRenderer;
import finalforeach.cosmicreach.world.Chunk;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BatchedZoneRenderer.class)
public class BatchedZoneRendererMixin {
    @Inject(method = "onChunkMeshed", at = @At("HEAD"))
    private void onChunkMeshedMixin(Chunk chunk, CallbackInfo ci) {
        PhysicsWorld.invalidateChunk(chunk);
    }
}
