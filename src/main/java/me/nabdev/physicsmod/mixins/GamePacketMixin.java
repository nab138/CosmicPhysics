package me.nabdev.physicsmod.mixins;

import finalforeach.cosmicreach.networking.GamePacket;
import me.nabdev.physicsmod.entities.CreateCubePacket;
import me.nabdev.physicsmod.entities.EntityRenderRotationPacket;
import me.nabdev.physicsmod.items.LinkCubePacket;
import me.nabdev.physicsmod.utils.MagnetPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static finalforeach.cosmicreach.networking.GamePacket.registerPacket;

@Mixin(GamePacket.class)
public abstract class GamePacketMixin {

    @Inject(method = "registerPackets", at = @At("TAIL"))
    private static void registerPackets(CallbackInfo ci) {
       registerPacket(EntityRenderRotationPacket.class);
       registerPacket(CreateCubePacket.class);
       registerPacket(MagnetPacket.class);
       registerPacket(LinkCubePacket.class);
    }
}
