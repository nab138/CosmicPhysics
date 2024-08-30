package me.nabdev.physicsmod.items;

import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.BlockUtil;
import com.jme3.bullet.RotationOrder;
import com.jme3.bullet.joints.New6Dof;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import finalforeach.cosmicreach.BlockSelection;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.gamestates.InGame;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.utils.IPhysicsEntity;
import me.nabdev.physicsmod.utils.PhysicsWorld;

public class PhysicsInfuser implements IModItem {
    DataTagManifest tagManifest = new DataTagManifest();
    public static Identifier id = new Identifier(Constants.MOD_ID, "infuser");

    public static IPhysicsEntity entityOne = null;
    public static IPhysicsEntity entityTwo = null;

    public static boolean ignoreNextUse = false;

    public PhysicsInfuser() {
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/infuser.png"));
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
    public void use(ItemSlot slot, Player player) {
        if(ignoreNextUse) {
            ignoreNextUse = false;
            return;
        }
        BlockState block = BlockSelection.getBlockLookingAt();
        BlockPosition pos = BlockSelection.getBlockPositionLookingAt();
        if(block == null || pos == null) {
            return;
        }

        Zone z = player.getZone(InGame.world);
        BlockUtil.setBlockAt(z, Block.AIR.getDefaultBlockState(), pos);
        PhysicsWorld.createBlockAt(new Vector3(pos.getGlobalX(), pos.getGlobalY(), pos.getGlobalZ()).add(0.5f), block, z);
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
