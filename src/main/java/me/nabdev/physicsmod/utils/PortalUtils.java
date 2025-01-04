package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.OrientedBoundingBox;
import com.badlogic.gdx.utils.Array;
import com.nikrasoff.seamlessportals.extras.IntVector3;
import com.nikrasoff.seamlessportals.portals.HPGPortal;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;


public class PortalUtils {
    public static Array<Vec3Int> getBlocksOn(HPGPortal p, Zone z){
        float blockCheckBump = 0.1f;
        Vector3 tvec = p.viewDirection.cpy().scl(isSecond(p) ? -blockCheckBump : blockCheckBump);
        p.position.add(tvec);
//        SeamlessPortals.LOGGER.info("\nTesting at pos " + this.position);
        OrientedBoundingBox bb = p.getMeshBoundingBox();
        p.position.sub(tvec);
        Vector3[] vertices = bb.getVertices();

        IntVector3 min = IntVector3.leastVector(vertices);
        IntVector3 max = IntVector3.greatestVector(vertices);

        BoundingBox tBB = new BoundingBox();

        Array<Vec3Int> blocks = new Array<>();

        for (int bx = min.x; bx <= max.x; ++bx){
            for (int by = min.y; by <= max.y; ++by){
                for (int bz = min.z; bz <= max.z; ++bz){
                    BlockState checkBlock = z.getBlockState(bx, by, bz);
                    if (checkBlock != null && !checkBlock.walkThrough){
                        checkBlock.getBoundingBox(tBB, bx, by, bz);
                        if (bb.intersects(tBB)){
                            Vec3Int blockPos = new Vec3Int(bx, by, bz);
                            if(!blocks.contains(blockPos, false)){
                                blocks.add(blockPos);
                            }
                        }
                    }
                }
            }
        }
        return blocks;
    }

    // Is second is private :(
    public static boolean isSecond(HPGPortal p){
        return p.getOutlineColor().equals(HPGPortal.secondaryPortalColor);
    }
}
