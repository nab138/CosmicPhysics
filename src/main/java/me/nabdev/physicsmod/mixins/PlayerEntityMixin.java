package me.nabdev.physicsmod.mixins;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.Entity;
import finalforeach.cosmicreach.util.Axis;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.utils.PhysicsWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class PlayerEntityMixin{
    @Shadow
    public Vector3 velocity;
    @Shadow
    public Vector3 onceVelocity;
    @Shadow
    public BoundingBox tmpEntityBoundingBox;
    @Shadow
    public BoundingBox tmpBlockBoundingBox;
    @Shadow
    public boolean collidedX;
    @Shadow
    public boolean collidedZ;
    @Shadow
    public boolean isOnGround;
    @Shadow
    public float footstepTimer;
    @Shadow
    private float floorFriction;
    @Shadow
    public float maxStepHeight;
    @Shadow
    public Vector3 position;
    @Shadow
    public BoundingBox localBoundingBox;
    @Shadow
    public BoundingBox tmpEntityBoundingBox2;
    @Shadow
    public BoundingBox tmpBlockBoundingBox2;
    @Shadow
    public Array<BoundingBox> tmpBlockBoundingBoxes;
    @Shadow
    public void onCollideWithBlock(Axis axis, BlockState block, int bx, int by, int bz) {}
    @Shadow
    public boolean shouldConstrainBySneak(Zone zone, BoundingBox blockBB, BoundingBox entityBB, int minBx, int minBy, int minBz, int maxBx, int maxBz) { return false; }
    @Shadow
    public boolean isInFluid() { return false; }


    /**
     * @author nab138
     * @reason Mixin Extras is broken so cant do local var modification
     */
    @Overwrite
    public void updateConstraints(Zone zone, Vector3 targetPosition) {
        float floorFriction = 0.0F;
        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(this.position);
        this.tmpEntityBoundingBox.max.add(this.position);
        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y;
        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
        this.tmpEntityBoundingBox.update();
        int minBx = (int)Math.floor(this.tmpEntityBoundingBox.min.x);
        int minBy = (int)Math.floor(this.tmpEntityBoundingBox.min.y);
        int minBz = (int)Math.floor(this.tmpEntityBoundingBox.min.z);
        int maxBx = (int)Math.floor(this.tmpEntityBoundingBox.max.x);
        int maxBy = (int)Math.floor(this.tmpEntityBoundingBox.max.y);
        int maxBz = (int)Math.floor(this.tmpEntityBoundingBox.max.z);
        boolean isOnGround = false;
        float minPosY = targetPosition.y;
        float maxPosY = targetPosition.y;

        for(int bx = minBx; bx <= maxBx; ++bx) {
            for(int by = minBy; by <= maxBy; ++by) {
                for(int bz = minBz; bz <= maxBz; ++bz) {
                    BlockState blockAdj = zone.getBlockState(bx, by, bz);
                    if (blockAdj != null && !blockAdj.walkThrough) {
                        blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                        if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                            blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz);
                            float oldY = this.tmpEntityBoundingBox.min.y;

                            for (BoundingBox bb : this.tmpBlockBoundingBoxes) {
                                if (bb.intersects(this.tmpEntityBoundingBox)) {
                                    this.velocity.y = 0.0F;
                                    this.onceVelocity.y = 0.0F;
                                    if (oldY <= bb.max.y && oldY >= bb.min.y) {
                                        minPosY = Math.max(minPosY, bb.max.y - this.localBoundingBox.min.y);
                                        maxPosY = Math.max(maxPosY, minPosY);
                                        if (!this.isOnGround) {
                                            this.footstepTimer = 0.45F;
                                        }

                                        isOnGround = true;
                                        floorFriction = Math.max(floorFriction, blockAdj.friction);
                                    } else {
                                        maxPosY = Math.min(maxPosY, bb.min.y - this.localBoundingBox.getHeight() - 0.01F);
                                    }

                                    this.onCollideWithBlock(Axis.Y, blockAdj, bx, by, bz);
                                }
                            }
                        }
                    }
                }
            }
        }



        for (Cube cube : PhysicsWorld.cubes) {
            if (cube == null) continue;
            cube.getBoundingBox(this.tmpBlockBoundingBox);
            // Expand the bounding box by 0.1f on all sides
            if (!this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) continue;
            Vector3 diff = new Vector3(targetPosition).sub(cube.position);

            // Check if the player is above the cube
            if (diff.y >= 0f && diff.y <= cube.localBoundingBox.getHeight() / 2f) {
                minPosY = (Math.max(minPosY, cube.position.y + cube.localBoundingBox.max.y - 0.01f));
                maxPosY = (Math.max(maxPosY, minPosY));
                if (!this.isOnGround) {
                    this.footstepTimer = 0.45F;
                }
                isOnGround = true;
                floorFriction = Math.max(floorFriction, cube.blockState.friction);
                break;
            }
            if (diff.y > -1.6f - cube.localBoundingBox.getHeight() / 2 && diff.y < 0f) {
                if (Math.abs(diff.x) > Math.abs(diff.z)) {
                    physicsMod$fixPositionX(targetPosition);
                }
            }

        }

        if (isOnGround) {
            this.floorFriction = floorFriction;
        } else if (this.isInFluid()) {
            this.floorFriction = 1.0F;
        }

        targetPosition.y = MathUtils.clamp(targetPosition.y, minPosY, maxPosY);
        this.isOnGround = isOnGround;
        this.tmpEntityBoundingBox.min.x = this.localBoundingBox.min.x + targetPosition.x;
        this.tmpEntityBoundingBox.max.x = this.localBoundingBox.max.x + targetPosition.x;
        this.tmpEntityBoundingBox.min.y = this.localBoundingBox.min.y + targetPosition.y + 0.01F;
        this.tmpEntityBoundingBox.max.y = this.localBoundingBox.max.y + targetPosition.y;
        this.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor(this.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor(this.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor(this.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor(this.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor(this.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor(this.tmpEntityBoundingBox.max.z);
        boolean constrainBySneaking = this.shouldConstrainBySneak(zone, this.tmpBlockBoundingBox, this.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        if (constrainBySneaking) {
            this.onceVelocity.x = 0.0F;
            this.velocity.x = 0.0F;
            targetPosition.x = this.position.x;
        }

        this.collidedX = false;
        this.collidedZ = false;
        boolean steppedUpForAll = true;
        float desiredStepUp = targetPosition.y;
        if (!constrainBySneaking) {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                            if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;

                                for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                                    if (bb.intersects(this.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - this.tmpEntityBoundingBox.min.y <= this.maxStepHeight) || !(bb.max.y > this.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - this.localBoundingBox.min.y);
                                        this.tmpEntityBoundingBox2.set(this.tmpEntityBoundingBox);
                                        this.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        this.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + this.localBoundingBox.getHeight();
                                        this.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label271:
                                        for (int bax = minBx; bax <= maxBx; ++bax) {
                                            for (int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for (int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(this.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp = !this.tmpBlockBoundingBox2.intersects(this.tmpEntityBoundingBox2);
                                                        if (!canStepUp) {
                                                            break label271;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (canStepUp) {
                                            desiredStepUp = currentDesiredStepUp;
                                            didStepUp = true;
                                        }
                                    }
                                }

                                if (!didStepUp) {

                                    for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                                        if (bb.intersects(this.tmpEntityBoundingBox)) {
                                            float centX = this.tmpBlockBoundingBox.getCenterX();
                                            if (centX > targetPosition.x) {
                                                targetPosition.x = bb.min.x - this.tmpEntityBoundingBox.getWidth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.x = bb.max.x + this.tmpEntityBoundingBox.getWidth() / 2.0F + 0.01F;
                                            }

                                            this.onCollideWithBlock(Axis.X, blockAdj, bx, by, bz);
                                            this.collidedX = true;
                                            this.onceVelocity.x = 0.0F;
                                            this.velocity.x = 0.0F;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }

        this.tmpEntityBoundingBox.min.set(this.localBoundingBox.min).add(targetPosition.x, targetPosition.y + 0.01F, targetPosition.z);
        this.tmpEntityBoundingBox.max.set(this.localBoundingBox.max).add(targetPosition);
        this.tmpEntityBoundingBox.update();
        minBx = (int)Math.floor(this.tmpEntityBoundingBox.min.x);
        minBy = (int)Math.floor(this.tmpEntityBoundingBox.min.y);
        minBz = (int)Math.floor(this.tmpEntityBoundingBox.min.z);
        maxBx = (int)Math.floor(this.tmpEntityBoundingBox.max.x);
        maxBy = (int)Math.floor(this.tmpEntityBoundingBox.max.y);
        maxBz = (int)Math.floor(this.tmpEntityBoundingBox.max.z);
        constrainBySneaking = this.shouldConstrainBySneak(zone, this.tmpBlockBoundingBox, this.tmpEntityBoundingBox, minBx, minBy, minBz, maxBx, maxBz);
        if (constrainBySneaking) {
            this.onceVelocity.z = 0.0F;
            this.velocity.z = 0.0F;
            targetPosition.z = this.position.z;
        }

        steppedUpForAll = true;
        desiredStepUp = targetPosition.y;
        if (!constrainBySneaking) {
            for(int bx = minBx; bx <= maxBx; ++bx) {
                for(int by = minBy; by <= maxBy; ++by) {
                    for(int bz = minBz; bz <= maxBz; ++bz) {
                        BlockState blockAdj = zone.getBlockState(bx, by, bz);
                        if (blockAdj != null && !blockAdj.walkThrough) {
                            blockAdj.getBoundingBox(this.tmpBlockBoundingBox, bx, by, bz);
                            if (this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) {
                                boolean didStepUp = false;

                                for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                                    if (bb.intersects(this.tmpEntityBoundingBox)) {
                                        if (!isOnGround || !(bb.max.y - this.tmpEntityBoundingBox.min.y <= this.maxStepHeight) || !(bb.max.y > this.tmpEntityBoundingBox.min.y)) {
                                            didStepUp = false;
                                            steppedUpForAll = false;
                                            break;
                                        }

                                        float currentDesiredStepUp = Math.max(desiredStepUp, bb.max.y - this.localBoundingBox.min.y);
                                        this.tmpEntityBoundingBox2.set(this.tmpEntityBoundingBox);
                                        this.tmpEntityBoundingBox2.min.y = currentDesiredStepUp;
                                        this.tmpEntityBoundingBox2.max.y = currentDesiredStepUp + this.localBoundingBox.getHeight();
                                        this.tmpEntityBoundingBox2.update();
                                        boolean canStepUp = true;

                                        label202:
                                        for (int bax = minBx; bax <= maxBx; ++bax) {
                                            for (int bay = by + 1; bay <= maxBy + 1; ++bay) {
                                                for (int baz = minBz; baz <= maxBz; ++baz) {
                                                    BlockState blockAbove = zone.getBlockState(bax, bay, baz);
                                                    if (blockAbove != null && !blockAbove.walkThrough) {
                                                        blockAbove.getBoundingBox(this.tmpBlockBoundingBox2, bax, bay, baz);
                                                        canStepUp = !this.tmpBlockBoundingBox2.intersects(this.tmpEntityBoundingBox2);
                                                        if (!canStepUp) {
                                                            break label202;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (canStepUp) {
                                            desiredStepUp = currentDesiredStepUp;
                                            didStepUp = true;
                                        }
                                    }
                                }

                                if (!didStepUp) {

                                    for (BoundingBox bb : blockAdj.getAllBoundingBoxes(this.tmpBlockBoundingBoxes, bx, by, bz)) {
                                        if (bb.intersects(this.tmpEntityBoundingBox)) {
                                            float centZ = this.tmpBlockBoundingBox.getCenterZ();
                                            if (centZ > targetPosition.z) {
                                                targetPosition.z = bb.min.z - this.tmpEntityBoundingBox.getDepth() / 2.0F - 0.01F;
                                            } else {
                                                targetPosition.z = bb.max.z + this.tmpEntityBoundingBox.getDepth() / 2.0F + 0.01F;
                                            }

                                            this.onCollideWithBlock(Axis.Z, blockAdj, bx, by, bz);
                                            this.collidedZ = true;
                                            this.onceVelocity.z = 0.0F;
                                            this.velocity.z = 0.0F;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Cube cube : PhysicsWorld.cubes) {
            if (cube == null) continue;
            cube.getBoundingBox(this.tmpBlockBoundingBox);
            // Expand the bounding box by 0.1f on all sides
            if (!this.tmpBlockBoundingBox.intersects(this.tmpEntityBoundingBox)) continue;
            Vector3 diff = new Vector3(targetPosition).sub(cube.position);

            // Check if the player is above the cube
            if (diff.y >= 0f && diff.y <= cube.localBoundingBox.getHeight() / 2f) {
                break;
            }
            if (diff.y > -2.5f * cube.localBoundingBox.getHeight() && diff.y < -0f) {
                if (Math.abs(diff.x) > Math.abs(diff.z)) {
                    physicsMod$fixPositionX(targetPosition);
                } else {
                    if (cube.position.z > targetPosition.z) {
                        targetPosition.z = this.tmpBlockBoundingBox.min.z - this.tmpEntityBoundingBox.getDepth() / 2.0F - 0.01F;
                    } else {
                        targetPosition.z = this.tmpBlockBoundingBox.max.z + this.tmpEntityBoundingBox.getDepth() / 2.0F + 0.01F;
                    }
                    this.collidedZ = true;
                    this.onceVelocity.z = 0.0F;
                    this.velocity.z = 0.0F;
                }
            }
        }

        if (steppedUpForAll) {
            targetPosition.y = desiredStepUp;
        }

        this.position.set(targetPosition.x, targetPosition.y, targetPosition.z);
    }

    @Unique
    private void physicsMod$fixPositionX(Vector3 targetPosition) {
        if (this.tmpBlockBoundingBox.getCenterX() > targetPosition.x) {
            targetPosition.x = this.tmpBlockBoundingBox.min.x - this.tmpEntityBoundingBox.getWidth() / 2.0F - 0.01F;
        } else {
            targetPosition.x = this.tmpBlockBoundingBox.max.x + this.tmpEntityBoundingBox.getWidth() / 2.0F + 0.01F;
        }
        this.collidedX = true;
        this.onceVelocity.x = 0.0F;
        this.velocity.x = 0.0F;
    }
}
