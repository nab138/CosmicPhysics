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

public class Linker implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public static Identifier id = new Identifier(Constants.MOD_ID, "linker");
    public static IPhysicsEntity entityOne = null;
    public static IPhysicsEntity entityTwo = null;


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
}
