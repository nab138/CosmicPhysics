package me.nabdev.physicsmod.items;

import com.badlogic.gdx.math.Vector3;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import com.github.puzzle.game.util.BlockSelectionUtil;
import com.github.puzzle.game.util.BlockUtil;
import com.github.puzzle.game.util.IClientNetworkManager;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.blocks.BlockPosition;
import finalforeach.cosmicreach.blocks.BlockState;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.util.Identifier;
import finalforeach.cosmicreach.world.Zone;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.entities.CreateCubePacket;
import me.nabdev.physicsmod.utils.PhysicsUtils;

public class PhysicsInfuser implements IModItem {
    final DataTagManifest tagManifest = new DataTagManifest();
    public static final Identifier id = Identifier.of(Constants.MOD_ID, "infuser");

    public static boolean ignoreNextUse = false;

    public PhysicsInfuser() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "infuser.png"));
    }

    public static CreateCubePacket createPacket = new CreateCubePacket();

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
        if (ignoreNextUse) {
            ignoreNextUse = false;
            return;
        }
        BlockState block = BlockSelectionUtil.getBlockLookingAt();
        BlockPosition pos = BlockSelectionUtil.getBlockPositionLookingAt();
        if (block == null || pos == null) {
            return;
        }

        Zone z = player.getZone();
        if(z == null) {
            return;
        }
        if (IClientNetworkManager.isConnected()) {
            createPacket.setCubeInfo(z, pos, block);
            IClientNetworkManager.sendAsClient(createPacket);
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
