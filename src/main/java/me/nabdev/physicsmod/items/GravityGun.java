package me.nabdev.physicsmod.items;

import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import me.nabdev.physicsmod.Constants;

import java.util.HashMap;

public class GravityGun extends AbstractCosmicItem {
    public static final Identifier id = Identifier.of(Constants.MOD_ID, "gravity_gun");
    public static HashMap<String, Boolean> isPlayerMag = new HashMap<>();

    public GravityGun() {
        super(id);
        addTexture(ItemModelType.ITEM_MODEL_2D, Identifier.of(Constants.MOD_ID, "gravity_gun.png"));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public String getName() {
        return "Gravity Gun";
    }
}
