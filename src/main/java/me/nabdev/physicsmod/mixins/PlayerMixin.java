package me.nabdev.physicsmod.mixins;

import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.utils.IPhysicsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    private Entity entity;

    @Shadow
    public abstract Zone getZone(World world);

    @Inject(method = "spawnDroppedItem", at = @At("HEAD"), cancellable = true)
    public void spawnDroppedItem(World world, ItemStack itemStack, CallbackInfo ci) {
        Zone zone = getZone(world);
        ItemEntity itemEntity = itemStack.spawnItemEntityAt(zone, this.entity.position);
        itemEntity.minPickupAge = 2.0F;
        itemEntity.position.add(entity.viewPositionOffset);
        itemEntity.velocity.set(entity.viewDirection).scl(7.0F).add(0.0F, 1.0F, 0.0F);
        ((IPhysicsItem) itemEntity).physicsMod$setPlayerDropped();
        ci.cancel();
    }
}
