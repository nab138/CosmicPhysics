package me.nabdev.physicsmod.items;

import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.utils.PhysicsWorld;

public class GravityGun implements IModItem {
    final DataTagManifest tagManifest = new DataTagManifest();
    public static final Identifier id = new Identifier(Constants.MOD_ID, "gravity_gun");
    public static boolean isMag = false;

    public GravityGun() {
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/gravity_gun.png"));
    }

    @Override
    public void use(ItemSlot slot, Player player) {
        if (PhysicsWorld.magnetEntity != null) {
            if (isMag) PhysicsWorld.dropMagnet();
            isMag = !isMag;
        }
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
