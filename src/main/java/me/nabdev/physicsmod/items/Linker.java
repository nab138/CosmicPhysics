package me.nabdev.physicsmod.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.util.Identifier;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsWorld;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;

public class Linker implements IModItem {
    public record LinkData(IPhysicsEntity other, New6Dof joint) {
    }

    final DataTagManifest tagManifest = new DataTagManifest();
    public static final Identifier id = Identifier.of(Constants.MOD_ID, "linker");
    public static IPhysicsEntity entityOne = null;
    public static IPhysicsEntity entityTwo = null;

    public static final HashMap<IPhysicsEntity, ArrayList<LinkData>> links = new HashMap<>();


    public Linker() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "linker.png"));
    }

    public static void link(IPhysicsEntity eOne, IPhysicsEntity eTwo) {
        PhysicsRigidBody bodyOne = eOne.getBody();
        PhysicsRigidBody bodyTwo = eTwo.getBody();

        Vector3f diff = bodyOne.getPhysicsLocation(null).subtract(bodyTwo.getPhysicsLocation(null));
        New6Dof joint = new New6Dof(bodyOne, bodyTwo, diff, Vector3f.ZERO, Matrix3f.IDENTITY, Matrix3f.IDENTITY, RotationOrder.XYZ);
        PhysicsWorld.space.addJoint(joint);

        addLinkTo(eOne, new LinkData(eTwo, joint));
        addLinkTo(eTwo, new LinkData(eTwo, joint));
    }

    public static void link() {
        if (entityOne == null || entityTwo == null) {
            return;
        }

        link(entityOne, entityTwo);
        entityOne.linkWith(entityTwo);

        entityOne = null;
        entityTwo = null;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public DataTagManifest getTagManifest() {
        return tagManifest;
    }

    public static void addLinkTo(IPhysicsEntity entity, LinkData linkData) {
        if (links.containsKey(entity)) {
            links.get(entity).add(linkData);
        } else {
            ArrayList<LinkData> list = new ArrayList<>();
            list.add(linkData);
            links.put(entity, list);
        }
    }

    public static void clearLinksFor(IPhysicsEntity entity) {
        try {
            if (links.containsKey(entity)) {
                for (LinkData linkData : links.get(entity)) {
                    if (PhysicsWorld.space != null) PhysicsWorld.space.removeJoint(linkData.joint);
                    linkData.other.getLinkedEntities().removeValue(entity, true);
                    ArrayList<LinkData> otherLinkData = links.get(linkData.other);
                    if (otherLinkData != null) otherLinkData.remove(linkData);
                }
                links.remove(entity);
            }
        } catch (ConcurrentModificationException e) {
            Constants.LOGGER.error("Concurrent modification exception while clearing links for entity: " + entity);
        }
    }

    @Override
    public String getName() {
        return "Linker";
    }
}
