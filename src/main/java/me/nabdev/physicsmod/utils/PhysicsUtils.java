package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;

import java.util.ArrayList;

public class PhysicsUtils {
    public record QueuedLink(IPhysicsEntity e, int id) {
    }

    public static ArrayList<QueuedLink> queuedLinks = new ArrayList<>();

    public static void applyMagnetForce(Vector3 position, PhysicsRigidBody body) {
//        Player player = InGame.getLocalPlayer();
//        Vector3 playerPos = player.getPosition().cpy().add(0, 2, 0);
//        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
//        playerPos.add(cam.direction.cpy().scl(2f));
//        Vector3f playerPosF = new Vector3f(playerPos.x, playerPos.y, playerPos.z);
//
//        Vector3f myPos = new Vector3f(position.x, position.y, position.z);
//        Vector3f dir = new Vector3f(playerPosF);
//        dir = dir.subtract(myPos).mult(3);
//
//        body.setLinearVelocity(dir);
//        body.activate(true);
    }

    public static Vector3 getCameraDir() {
//        PerspectiveCamera cam = ((ICameraOwner) GameState.IN_GAME).browserMod$getCamera();
//        return cam.direction.cpy();
        return new Vector3();
    }

    public static Vector3f v3ToV3f(Vector3 v) {
        return new Vector3f(v.x, v.y, v.z);
    }

    public static void createBlockAt(Vector3 pos, BlockState state, Zone zone) {
        PhysicsWorld.initialize();
        if (isEmpty(state)) return;
        //Texture stitchedTexture = TextureUtils.getTextureForBlock(state);
        Cube e = new Cube(new Vector3f(pos.x, pos.y, pos.z), state);
        zone.addEntity(e);
        //e.setTexture(stitchedTexture);
        e.setMass(0);
    }

    public static boolean isEmpty(BlockState b) {
        return b == null || b.walkThrough;
    }

    public static void queueLinks(IPhysicsEntity a, int[] ids) {
        for (int id : ids) {
            queuedLinks.add(new QueuedLink(a, id));
        }
    }

    public static void applyQueuedLinks() {
        if (queuedLinks.isEmpty()) return;
        for (QueuedLink link : queuedLinks) {
            IPhysicsEntity a = link.e;
            IPhysicsEntity b = PhysicsWorld.getEntityById(link.id);
            if (b == null) continue;
            //Linker.link(a, b);
            a.linkWith(b);
        }
        queuedLinks.clear();
    }

    public static boolean epsilonEquals(Quaternion a, Quaternion b, float epsilon) {
        return Math.abs(a.x - b.x) < epsilon && Math.abs(a.y - b.y) < epsilon && Math.abs(a.z - b.z) < epsilon && Math.abs(a.w - b.w) < epsilon;
    }

    public static boolean epsilonEquals(Quaternion a, Quaternion b) {
        return epsilonEquals(a, b, 0.0001f);
    }
}
