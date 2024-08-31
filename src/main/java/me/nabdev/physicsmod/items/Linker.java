package me.nabdev.physicsmod.items;

import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsWorld;

import java.util.ArrayList;
import java.util.HashMap;

public class Linker implements IModItem {
    public static class LinkData {
        public IPhysicsEntity other;
        public New6Dof joint;

        public LinkData(IPhysicsEntity other, New6Dof joint) {
            this.other = other;
            this.joint = joint;
        }
    }
    DataTagManifest tagManifest = new DataTagManifest();
    public static Identifier id = new Identifier(Constants.MOD_ID, "linker");
    public static IPhysicsEntity entityOne = null;
    public static IPhysicsEntity entityTwo = null;

    public static HashMap<IPhysicsEntity, ArrayList<LinkData>> links = new HashMap<>();


    public Linker() {
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/linker.png"));
    }

    public static void link() {
        if (entityOne == null || entityTwo == null) {
            return;
        }

        PhysicsRigidBody bodyOne = entityOne.getBody();
        PhysicsRigidBody bodyTwo = entityTwo.getBody();

        Vector3f diff = bodyOne.getPhysicsLocation(null).subtract(bodyTwo.getPhysicsLocation(null));

        New6Dof joint = new New6Dof(bodyOne, bodyTwo, diff, Vector3f.ZERO, Matrix3f.IDENTITY, Matrix3f.IDENTITY, RotationOrder.XYZ);
        PhysicsWorld.space.addJoint(joint);
        addLinkTo(entityOne, new LinkData(entityTwo, joint));
        addLinkTo(entityTwo, new LinkData(entityTwo, joint));

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
        if (links.containsKey(entity)) {
            for (LinkData linkData : links.get(entity)) {
                if(PhysicsWorld.space != null) PhysicsWorld.space.removeJoint(linkData.joint);
                linkData.other.getLinkedEntities().remove(entity);
                ArrayList<LinkData> otherLinkData = links.get(linkData.other);
                if(otherLinkData != null) otherLinkData.remove(linkData);
            }
            links.remove(entity);
        }
    }
}
