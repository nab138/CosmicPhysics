package me.nabdev.physicsmod.items;

import com.github.puzzle.core.Identifier;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.game.items.data.DataTagManifest;
import me.nabdev.physicsmod.Constants;

public class MysticalGem implements IModItem {
    final DataTagManifest tagManifest = new DataTagManifest();
    public static final Identifier id = new Identifier(Constants.MOD_ID, "mystical_gem");

    public MysticalGem() {
        addTexture(IModItem.MODEL_2_5D_ITEM, new ResourceLocation(Constants.MOD_ID, "textures/items/mystical_gem.png"));
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
