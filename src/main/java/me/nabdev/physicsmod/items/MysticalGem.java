package me.nabdev.physicsmod.items;

import finalforeach.cosmicreach.util.Identifier;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import me.nabdev.physicsmod.Constants;

public class MysticalGem extends AbstractCosmicItem {
    public static final Identifier id = Identifier.of(Constants.MOD_ID, "mystical_gem");

    public MysticalGem() {
        super(id);
        addTexture(ItemModelType.ITEM_MODEL_2D, Identifier.of(Constants.MOD_ID, "mystical_gem.png"));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public String getName() {
        return "Mystical Gem";
    }
}
