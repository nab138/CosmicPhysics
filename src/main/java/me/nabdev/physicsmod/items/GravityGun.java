package me.nabdev.physicsmod.items;

import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import finalforeach.cosmicreach.entities.player.Player;
import finalforeach.cosmicreach.items.ItemSlot;
import finalforeach.cosmicreach.util.Identifier;
import me.nabdev.physicsmod.Constants;
import me.nabdev.physicsmod.utils.PhysicsWorld;

import java.util.HashMap;

public class GravityGun implements IModItem {
    final DataTagManifest tagManifest = new DataTagManifest();
    public static final Identifier id = Identifier.of(Constants.MOD_ID, "gravity_gun");
    public static HashMap<String, Boolean> isPlayerMag = new HashMap<>();

    public GravityGun() {
        addTexture(IModItem.MODEL_2_5D_ITEM, Identifier.of(Constants.MOD_ID, "gravity_gun.png"));
    }

    @Override
    public void use(ItemSlot slot, Player player) {
        if (PhysicsWorld.magnetEntities.containsKey(player.getAccount().getUniqueId())) {
            if (isPlayerMag.get(player.getAccount().getUniqueId())) PhysicsWorld.dropMagnet(player);
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

    @Override
    public String getName() {
        return "Gravity Gun";
    }
}
