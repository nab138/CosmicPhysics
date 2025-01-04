package me.nabdev.physicsmod.entities;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.jme3.math.Vector3f;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.extras.DirectionVector;
import com.nikrasoff.seamlessportals.extras.interfaces.IPortalableEntity;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.EntityUniqueId;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.utils.PhysicsUtils;

import java.util.Map;

public class PortalCube extends Cube implements IPortalableEntity {
    // Portal stuff
    private static final Vector3 portalPosCheckEpsilon = new Vector3(0f, 0.05f, 0f);
    private transient boolean justTeleported = false;
    private transient Portal teleportPortal;
    private transient boolean ignorePortals = false;
    public transient Array<Portal> nearbyPortals = new Array<>();
    private final transient Array<BlockPosition> tmpNonCollideBlocks = new Array<>();
    private final transient Array<BlockPosition> tmpCollidedBlocks = new Array<>();
    private final transient OrientedBoundingBox tmpPortaledBoundingBox = new OrientedBoundingBox();
    private final transient BoundingBox tmpPortalCheckBlockBoundingBox = new BoundingBox();
    private final transient Matrix4 tmpPortalTransformMatrix = new Matrix4();
    private final transient Vector3 tmpPortalNextPosition = new Vector3();

    @Override
    public void update(Zone zone, double deltaTime) {
        super.update(zone, deltaTime);
        this.updatePortalPositions(zone, deltaTime);
    }

    private Vector3 getVelocity(){
        return PhysicsUtils.v3fToV3(body.getLinearVelocity(new Vector3f()));
    }


    public void updatePortalPositions(Zone zone, double deltaTime) {
        if (this.ignorePortals) return;
        if(this.justTeleported){
            this.justTeleported = false;
            return;
        }
        this.tmpNonCollideBlocks.clear();
        this.tmpNonCollideBlocks.addAll(this.tmpCollidedBlocks);
        this.tmpCollidedBlocks.clear();
        this.tmpPortaledBoundingBox.setBounds(this.localBoundingBox);
        this.tmpPortaledBoundingBox.getBounds().min.add(new Vector3(-0.01F, -0.01F, -0.01F));
        this.tmpPortaledBoundingBox.getBounds().max.add(new Vector3(0.01F, 0.01F, 0.01F));
        this.justTeleported = false;
        this.teleportPortal = null;
        this.nearbyPortals.clear();
        updateTrackedPortals(deltaTime, zone);
    }


    private void updateTrackedPortals(double deltaTime, Zone zone){
        if (SeamlessPortals.portalManager.createdPortals.isEmpty()) return;

        Vector3 prevPos = this.position.cpy();
        Vector3 testVelocity = this.getVelocity();
        testVelocity.add(accel.cpy().scl((float)deltaTime));

        float vx = testVelocity.x * (float)deltaTime;
        float vy = testVelocity.y * (float)deltaTime;
        float vz = testVelocity.z * (float)deltaTime;
        Vector3 posDiff = new Vector3(vx, vy, vz);
        Vector3 targetPosition = (new Vector3(this.position)).add(posDiff);
        this.tmpPortalNextPosition.set(targetPosition);

        Ray posChange = new Ray(prevPos.cpy().add(portalPosCheckEpsilon), targetPosition.cpy().add(portalPosCheckEpsilon).sub(prevPos));

        this.getBoundingBox(this.globalBoundingBox);
        for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
            Portal portal = portalEntry.getValue();
            if (portal.isPortalDestroyed) {
                continue;
            }
            if (portal.zone != zone) continue;
            portal.getBoundingBox(tmpPortalCheckBlockBoundingBox);
            if (this.tmpPortalCheckBlockBoundingBox.intersects(this.globalBoundingBox)) {
                this.nearbyPortals.add(portal);
            }
            if (portal.isNotOnSameSideOfPortal(prevPos.cpy().add(portalPosCheckEpsilon), targetPosition.cpy().add(portalPosCheckEpsilon)) && Intersector.intersectRayOrientedBounds(posChange, portal.getMeshBoundingBox(), new Vector3())){
                if (portal.linkedPortal == null){
                    this.onDeath();
                    break;
                }
                this.teleportThroughPortal(portal, zone);
                break;
            }
        }
    }

//   @Override
//   public void updateConstraints(Zone zone, Vector3 targetPosition){
//        super.updateConstraints();
//        return this.checkIfShouldCollidePortal(zone, (int)targetPosition.x, (int)targetPosition.y, (int)targetPosition.z, this::updateConstraints);
//    }
//
//    public BlockState checkIfShouldCollidePortal(Zone instance, int x, int y, int z, Operation<BlockState> original){
//        // Refer to the comment in teleportThroughPortal for an explanation
//        BlockState orBlockState = original.call(instance, x, y, z);
//        if (this.ignorePortals){
//            return orBlockState;
//        }
//
//        Chunk c = instance.getChunkAtBlock(x, y, z);
//        if (c == null) return orBlockState;
//        BlockPosition curBlockPos = new BlockPosition(c, x - c.blockX, y - c.blockY, z - c.blockZ);
//        if (this.tmpNonCollideBlocks.contains(curBlockPos, false)){
//            if (!this.tmpCollidedBlocks.contains(curBlockPos, false)){
//                this.tmpCollidedBlocks.add(curBlockPos);
//            }
//            return null;
//        }
//
//        if (orBlockState != null && !orBlockState.walkThrough){
//            orBlockState.getBoundingBox(this.tmpPortalCheckBlockBoundingBox, x, y, z);
//            Vector3 checkCenter = new Vector3();
//            this.tmpPortalCheckBlockBoundingBox.getCenter(checkCenter);
//            Vector3 portalCollisionCheckPos = this.isJustTeleported() ? this.tmpPortalNextPosition : this.position.cpy();
//            Ray ray = new Ray(portalCollisionCheckPos, checkCenter.cpy().sub(portalCollisionCheckPos));
//            for (Map.Entry<EntityUniqueId, Portal> portalEntry : SeamlessPortals.portalManager.createdPortals.entrySet()){
//                Portal portal = portalEntry.getValue();
//                if (portal.zone == zone && portal.isNotOnSameSideOfPortal(portalCollisionCheckPos, checkCenter) && Intersector.intersectRayOrientedBounds(ray, portal.getMeshBoundingBox(), new Vector3())){
//                    if (!portal.getMeshBoundingBox().intersects(this.tmpPortalCheckBlockBoundingBox)){
//                        return null;
//                    }
//                    else{
//                        System.out.println(this.tmpPortalCheckBlockBoundingBox + " and [" + portal.getMeshBoundingBox().getCorner000(new Vector3()) + "|" + portal.getMeshBoundingBox().getCorner111(new Vector3()) + "]");
//                    }
//                }
//            }
//
//            if (this.isJustTeleported()){
//                if (!this.tmpPortaledBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox) && this.tmpEntityBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox)){
//                    this.tmpNonCollideBlocks.add(curBlockPos);
//                    this.tmpCollidedBlocks.add(curBlockPos);
//                    return null;
//                }
//            }
//        }
//        return orBlockState;
//    }

    public void teleportThroughPortal(Portal portal, Zone zone) {
        if (portal.zone != portal.linkedPortal.zone){
            GameSingletons.world.getZoneIfExists(portal.zone.zoneId).removeEntity(this);
            GameSingletons.world.getZoneIfExists(portal.linkedPortal.zone.zoneId).addEntity(this);
        }
        this.tmpPortalNextPosition.set(portal.getPortaledPos(this.tmpPortalNextPosition));
        this.viewDirection = portal.getPortaledVector(this.viewDirection);
        Vector3 tmpVelocity = this.getVelocity();
        tmpVelocity.sub(portal.velocity);
        tmpVelocity.sub(portal.onceVelocity);
        this.setPosition(portal.getPortaledPos(this.position));
        tmpVelocity = portal.getPortaledVector(tmpVelocity);
        tmpVelocity.add(portal.linkedPortal.velocity);
        tmpVelocity.add(portal.linkedPortal.onceVelocity);
        body.setLinearVelocity(PhysicsUtils.v3ToV3f(tmpVelocity));

        // A bunch of magic to make mismatched portals more intuitive
        // to the player and less intuitive to any poor soul
        // who happens to be looking through this code

        // sorry not sorry
        this.snapOnGoThroughPortal(portal, zone);

        this.justTeleported = true;
        this.teleportPortal = portal;
        Vector3 orPos = new Vector3(this.position);
        this.tmpPortalTransformMatrix.setToLookAt(orPos, orPos.cpy().add(portal.linkedPortal.getPortaledVector(new Vector3(0, 0, 1))), portal.linkedPortal.getPortaledVector(new Vector3(0, 1, 0))).inv();
        this.tmpPortaledBoundingBox.setTransform(this.tmpPortalTransformMatrix);
    }

    private void snapOnGoThroughPortal(Portal portal, Zone zone){
        // Making this is pure suffering
        // Why is collision with blocks so hard to do?
        // Isn't it, like, THE thing this game should be good at?

        // First, figure out the direction we should be checking
        DirectionVector direction = DirectionVector.getClosestDirection(portal.getPortaledVector(DirectionVector.POS_Y.getVector()));

        // Now, we get the new bounding box
        this.tmpEntityBoundingBox.set(this.localBoundingBox);
        this.tmpEntityBoundingBox.min.add(this.position);
        this.tmpEntityBoundingBox.max.add(this.position);
        this.tmpEntityBoundingBox.update();

        // Now, get the range of blocks to check against
        Vector3 minPoint = new Vector3();
        Vector3 maxPoint = new Vector3();

        switch (direction.getName()){
            case "negZ":
                this.tmpEntityBoundingBox.getCorner001(minPoint);
                this.tmpEntityBoundingBox.getCorner111(maxPoint);
                break;
            case "posX":
                this.tmpEntityBoundingBox.getCorner000(minPoint);
                this.tmpEntityBoundingBox.getCorner011(maxPoint);
                break;
            case "negX":
                this.tmpEntityBoundingBox.getCorner100(minPoint);
                this.tmpEntityBoundingBox.getCorner111(maxPoint);
                break;
            case "posY":
                this.tmpEntityBoundingBox.getCorner000(minPoint);
                this.tmpEntityBoundingBox.getCorner101(maxPoint);
                break;
            case "negY":
                this.tmpEntityBoundingBox.getCorner010(minPoint);
                this.tmpEntityBoundingBox.getCorner111(maxPoint);
                break;
            default:
                this.tmpEntityBoundingBox.getCorner000(minPoint);
                this.tmpEntityBoundingBox.getCorner110(maxPoint);
        }

        int minbx = (int) Math.floor(minPoint.x);
        int minby = (int) Math.floor(minPoint.y);
        int minbz = (int) Math.floor(minPoint.z);
        int maxbx = (int) Math.floor(maxPoint.x);
        int maxby = (int) Math.floor(maxPoint.y);
        int maxbz = (int) Math.floor(maxPoint.z);

        // And now for the actual checking collisions part

        float highestPoint = 0;
        for (int bx = minbx; bx <= maxbx; ++bx){
            for (int by = minby; by <= maxby; ++by){
                for (int bz = minbz; bz <= maxbz; ++bz){
                    BlockState checkBlock = zone.getBlockState(bx, by, bz);
                    if (checkBlock != null && !checkBlock.walkThrough){
                        checkBlock.getBoundingBox(this.tmpPortalCheckBlockBoundingBox, bx, by, bz);
                        // Figure out if the block can just be discarded (to prevent some weirdness)
                        float checkPoint;
                        switch (direction.getName()){
                            case "negZ", "negX", "negY" -> checkPoint = portal.linkedPortal.getPortaledPos(this.tmpPortalCheckBlockBoundingBox.min).y;
                            default -> checkPoint = portal.linkedPortal.getPortaledPos(this.tmpPortalCheckBlockBoundingBox.max).y;
                        }
                        if (checkPoint > portal.linkedPortal.getPortaledPos(this.position).y + 0.01) continue;

                        if (this.tmpEntityBoundingBox.intersects(this.tmpPortalCheckBlockBoundingBox)){
                            // Figure out how high the player should be snapped
                            float curPoint;
                            Vector3 blockOffsetMin = this.tmpPortalCheckBlockBoundingBox.max.cpy().sub(this.tmpEntityBoundingBox.min);
                            Vector3 blockOffsetMax = this.tmpPortalCheckBlockBoundingBox.min.cpy().sub(this.tmpEntityBoundingBox.max).scl(-1);
                            switch (direction.getName()){
                                case "negZ" -> curPoint = blockOffsetMax.z;
                                case "posX" -> curPoint = blockOffsetMin.x;
                                case "negX" -> curPoint = blockOffsetMax.x;
                                case "posY" -> curPoint = blockOffsetMin.y;
                                case "negY" -> curPoint = blockOffsetMax.y;
                                default -> curPoint = blockOffsetMin.z;
                            }
                            highestPoint = Math.max(highestPoint, curPoint);
                        }
                    }
                }
            }
        }
        if (highestPoint > 0){
            highestPoint += 0.01F;
        }
        // finally, snap the player in the chosen direction by the chosen amount
        Vector3 bump = direction.getVector().cpy().scl(highestPoint);
        this.position.add(bump);
        this.tmpPortalNextPosition.add(bump);
    }

    @Override
    public boolean cosmicReach_Seamless_Portals$isJustTeleported(){
        return this.justTeleported;
    }

    @Override
    public BlockState cosmicReach_Seamless_Portals$checkIfShouldCollidePortal(Zone zone, int i, int i1, int i2, Operation<BlockState> operation) {
        return null;
    }

    @Override
    public void cosmicReach_Seamless_Portals$setIgnorePortals(boolean value){
        this.ignorePortals = value;
    }

    @Override
    public Portal cosmicReach_Seamless_Portals$getTeleportingPortal(){
        return this.teleportPortal;
    }

    @Override
    public Array<Portal> cosmicReach_Seamless_Portals$getNearbyPortals() {
        return nearbyPortals;
    }
}
