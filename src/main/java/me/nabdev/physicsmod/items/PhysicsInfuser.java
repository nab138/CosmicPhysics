package me.nabdev.physicsmod.items;

import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.util.BlockUtil;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.api.block.IBlock;
import io.github.puzzle.cosmic.api.block.IBlockPosition;
import io.github.puzzle.cosmic.api.block.PBlockState;
import io.github.puzzle.cosmic.api.entity.player.IPlayer;
import io.github.puzzle.cosmic.api.item.IItemSlot;
import io.github.puzzle.cosmic.api.world.IZone;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import io.github.puzzle.cosmic.util.APISide;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.utils.PhysicsUtils;

public class PhysicsInfuser extends AbstractCosmicItem {
    public static final Identifier id = Identifier.of(Constants.MOD_ID, "infuser");

    public static boolean ignoreNextUse = false;

    public PhysicsInfuser() {
        super(id);
        addTexture(ItemModelType.ITEM_MODEL_2D, Identifier.of(Constants.MOD_ID, "infuser.png"));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean pUse(APISide side, IItemSlot itemSlot, IPlayer player, IBlockPosition targetPlaceBlockPos, IBlockPosition pos, boolean isLeftClick) {
        if(side == APISide.REMOTE_CLIENT || isLeftClick) {
            return false;
        }
        if (ignoreNextUse) {
            ignoreNextUse = false;
            return true;
        }
        if (pos == null) {
            return true;
        }
        PBlockState block = pos.pGetBlockState();
        if (block == null || block.pGetBlock() == null || block.pGetBlock() == IBlock.as(Block.AIR)) {
            return true;
        }


        IZone z = player.pGetZone();
        if(z == null) {
            return true;
        }

        if(GameSingletons.isHost) {
            BlockUtil.setBlockAt(z.as(), Block.AIR.getDefaultBlockState(), pos.as());
            PhysicsUtils.createBlockAt(new Vector3(pos.pGetGlobalX(), pos.pGetGlobalY(), pos.pGetGlobalZ()).add(0.5f), block.as(), z.as());
        }
        return true;
    }

    @Override
    public String getName() {
        return "Physics Infuser";
    }

}
