package me.nabdev.physicsmod.utils;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.entities.Cube;

import java.util.ArrayList;

public class PhysicsUtils {
    public record QueuedLink(IPhysicsEntity e, int id) {
    }

    public static ArrayList<QueuedLink> queuedLinks = new ArrayList<>();

    public static void applyMagnetForce(Player player, Vector3 position, PhysicsRigidBody body) {
        Vector3 playerPos = player.getPosition().cpy().add(0, 2, 0);
        playerPos.add(player.getEntity().viewDirection.cpy().scl(2f));
        Vector3f playerPosF = new Vector3f(playerPos.x, playerPos.y, playerPos.z);

        Vector3f myPos = new Vector3f(position.x, position.y, position.z);
        Vector3f dir = new Vector3f(playerPosF);
        dir = dir.subtract(myPos).mult(3);

        body.setLinearVelocity(dir);
        body.activate(true);
    }

    public static Vector3f v3ToV3f(Vector3 v) {
        return new Vector3f(v.x, v.y, v.z);
    }

    public static Vector3 v3fToV3(Vector3f v) {
        return new Vector3(v.x, v.y, v.z);
    }

    public static Cube createBlockAt(Vector3 pos, BlockState state, Zone zone) {
        PhysicsWorld.initialize();
        if (isEmpty(state)) return null;
        Cube e = new Cube(new Vector3f(pos.x, pos.y, pos.z), state);
        zone.addEntity(e);
        return e;
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
//            IPhysicsEntity a = link.e;
//            IPhysicsEntity b = PhysicsWorld.getEntityById(link.id);
//            if (b == null) continue;
//            Linker.link(a, b);
//            a.linkWith(b.getID());
        }
        queuedLinks.clear();
    }

    public static boolean epsilonEquals(Quaternion a, Quaternion b, float epsilon) {
        return Math.abs(a.x - b.x) < epsilon && Math.abs(a.y - b.y) < epsilon && Math.abs(a.z - b.z) < epsilon && Math.abs(a.w - b.w) < epsilon;
    }

    public static boolean epsilonEquals(Quaternion a, Quaternion b) {
        return epsilonEquals(a, b, 0.0001f);
    }

    public static CompoundCollisionShape getCollisionMeshForBlock(BlockState blockState) {
        Array<BoundingBox> boxes = new Array<>();
        blockState.getModel().getAllBoundingBoxes(boxes, 0, 0, 0);
        CompoundCollisionShape compoundShape = new CompoundCollisionShape();
        boxes.forEach(box -> {
            Vector3f halfExtents = new Vector3f((box.max.x - box.min.x) / 2, (box.max.y - box.min.y) / 2, (box.max.z - box.min.z) / 2);
            Vector3f center = new Vector3f(box.getCenterX() - 0.5f, box.getCenterY() - 0.5f, box.getCenterZ() - 0.5f);
            BoxCollisionShape boxShape = new BoxCollisionShape(halfExtents);
            compoundShape.addChildShape(boxShape, center);
        });
        return compoundShape;
    }
}
