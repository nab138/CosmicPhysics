package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.entities.ItemEntity;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemStack;
import finalforeach.cosmicreach.world.World;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.utils.IPhysicsItem;
import me.nabdev.physicsmod.utils.PhysicsPlayer;
import me.nabdev.physicsmod.utils.PhysicsUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Shadow
    private Entity entity;

    @Shadow public abstract Zone getZone();

    @Shadow private transient BoundingBox tmpBlockBoundingBox;
    @Shadow private transient BoundingBox tmpGlobalBoundingBox;
    @Shadow public transient BoundingBox proneBoundingBox;

    /**
     * @author nab138
     * @reason yuh
     */
    @Overwrite
    public void respawn(Zone zone){
        float spawnX = zone.spawnPoint.x;
        float spawnY = zone.spawnPoint.y;
        float spawnZ = zone.spawnPoint.z;

        for(boolean collidesWithGround = true; collidesWithGround; ++spawnY) {
            collidesWithGround = false;
            this.tmpGlobalBoundingBox.set(this.proneBoundingBox);
            this.tmpGlobalBoundingBox.min.add(spawnX, spawnY, spawnZ);
            Vector3 var10000 = this.tmpGlobalBoundingBox.min;
            var10000.y += this.entity.maxStepHeight;
            this.tmpGlobalBoundingBox.max.add(spawnX, spawnY, spawnZ);
            this.tmpGlobalBoundingBox.update();
            int minBx = (int)Math.floor((double)this.tmpGlobalBoundingBox.min.x);
            int minBy = (int)Math.floor((double)this.tmpGlobalBoundingBox.min.y);
            int minBz = (int)Math.floor((double)this.tmpGlobalBoundingBox.min.z);
            int maxBx = (int)Math.floor((double)this.tmpGlobalBoundingBox.max.x);
            int maxBy = (int)Math.floor((double)this.tmpGlobalBoundingBox.max.y);
            int maxBz = (int)Math.floor((double)this.tmpGlobalBoundingBox.max.z);

            label51:
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                            if (this.tmpBlockBoundingBox.intersects(this.tmpGlobalBoundingBox)) {
                                collidesWithGround = true;
                                break label51;
                            }
                        }
                    }
                }
            }

            if (!collidesWithGround) {
                break;
            }
        }

        ((PhysicsPlayer) this.entity).cosmicPhysics$setPosition(new Vector3(spawnX, spawnY, spawnZ));
//        if (GameSingletons.isHost && ServerSingletons.SERVER != null) {
//            ServerSingletons.SERVER.broadcast(zone, new PlayerPositionPacket(this));
//        }
    }

    @Inject(method = "spawnDroppedItem", at = @At("HEAD"), cancellable = true)
    public void spawnDroppedItem(World world, ItemStack itemStack, CallbackInfo ci) {
        Zone zone = getZone();

        ItemEntity itemEntity = itemStack.spawnItemEntityAt(zone, this.entity.position);
        itemEntity.minPickupAge = 2.0F;
        itemEntity.position.add(entity.viewPositionOffset);
        itemEntity.velocity.set(entity.viewDirection).scl(7.0F).add(0.0F, 1.0F, 0.0F);

        ((IPhysicsItem) itemEntity).physicsMod$setPlayerDropped(PhysicsUtils.v3ToV3f(entity.viewDirection));
        ci.cancel();
    }
}
