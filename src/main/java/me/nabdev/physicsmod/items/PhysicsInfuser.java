package me.nabdev.physicsmod.items;

import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.BlockUtil;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.utils.PhysicsUtils;

public class PhysicsInfuser implements IModItem {
    final DataTagManifest tagManifest = new DataTagManifest();
    public static final Identifier id = Identifier.of(Constants.MOD_ID, "infuser");

    public static boolean ignoreNextUse = false;

    public PhysicsInfuser() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "infuser.png"));
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
    public void use(ItemSlot slot, Player player, BlockPosition targetPlaceBlockPos, BlockPosition pos) {
        if (ignoreNextUse) {
            ignoreNextUse = false;
            return;
        }
        if (pos == null) {
            return;
        }
        BlockState block = pos.getBlockState();
        if (block == null || block.getBlock() == null || block.getBlock() == Block.AIR) {
            return;
        }


        Zone z = player.getZone();
        if(z == null) {
            return;
        }


        if(GameSingletons.isHost) {
            BlockUtil.setBlockAt(z, Block.AIR.getDefaultBlockState(), pos);
            PhysicsUtils.createBlockAt(new Vector3(pos.getGlobalX(), pos.getGlobalY(), pos.getGlobalZ()).add(0.5f), block, z);
        }
    }

    @Override
    public String getName() {
        return "Physics Infuser";
    }

}
