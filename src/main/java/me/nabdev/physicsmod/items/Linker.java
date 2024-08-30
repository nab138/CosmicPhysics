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
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/launcher.png"));
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

    @Override
    public boolean isCatalogHidden() {
        return false;
    }

    public static void link() {
        if (entityOne == null || entityTwo == null) {
            return;
        }

        PhysicsRigidBody bodyOne = entityOne.getBody();
        PhysicsRigidBody bodyTwo = entityTwo.getBody();

        // Define pivots and rotations
        Vector3f pivotInBall = new Vector3f(0.5f, 0f, 0f);
        Vector3f pivotInPaddle = new Vector3f(-0.5f, 0f, 0f);
        Matrix3f rotInBall = Matrix3f.IDENTITY;
        Matrix3f rotInPaddle = Matrix3f.IDENTITY;
        New6Dof joint = new New6Dof(
                bodyOne, bodyTwo, pivotInBall, pivotInPaddle,
                rotInBall, rotInPaddle, RotationOrder.XYZ);
        PhysicsWorld.space.addJoint(joint);
        System.out.println("Linked!");
    }
}
