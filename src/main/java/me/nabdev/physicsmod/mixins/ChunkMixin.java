package me.nabdev.physicsmod.mixins;

import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.savelib.blocks.IBlockState;
import finalforeach.cosmicreach.world.Chunk;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class ChunkMixin {
    @Inject(method="setBlockState(Lfinalforeach/cosmicreach/blocks/BlockState;III)V", at=@At("TAIL"))
    private void setBlockState(BlockState blockState, int x, int y, int z, CallbackInfo ci) {
        PhysicsWorld.invalidateChunk((Chunk)(Object)this);
    }

    @Inject(method = "setBlockState(Lfinalforeach/cosmicreach/savelib/blocks/IBlockState;III)V", at = @At("TAIL"))
    private void setBlockState2(IBlockState par1, int par2, int par3, int par4, CallbackInfo ci) {
        PhysicsWorld.invalidateChunk((Chunk)(Object)this);
    }
}
