package me.nabdev.physicsmod;

import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.PostModInitializer;
import com.github.puzzle.game.oredict.ResourceDictionary;
import com.github.puzzle.game.oredict.tags.Tag;
import finalforeach.cosmicreach.blocks.Block;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.items.recipes.ShapedCraftingRecipe;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.MysticalGem;
import me.nabdev.physicsmod.items.PhysicsInfuser;

@SuppressWarnings("unused")
public class PhysicsPostInitializer implements PostModInitializer {
    public static Tag cheeseTag = Tag.of("cheese");

    @Override
    public void onPostInit() {
        ResourceDictionary.addBlockStateToTag(cheeseTag, Block.getById("base:cheese").getDefaultBlockState());
        ShapedCraftingRecipe infuserRecipe = new ShapedCraftingRecipe(3, 3);
        infuserRecipe.setOutput(Item.getItem(PhysicsInfuser.id.toString()), 1);
        infuserRecipe.setInput(0, 0, Item.getItem("base:ingot_iron")::equals);
        infuserRecipe.setInput(0, 1, Item.getItem("base:ingot_iron")::equals);
        infuserRecipe.setInput(1, 0, Item.getItem("base:ingot_iron")::equals);
        infuserRecipe.setInput(1, 2, Item.getItem("base:ingot_iron")::equals);
        infuserRecipe.setInput(1, 1, (i) -> i != null && i.getID().equals(MysticalGem.id.toString()));
        infuserRecipe.setInput(2, 1, Item.getItem("base:ingot_iron")::equals);
        infuserRecipe.setInput(2, 2, Item.getItem("base:ingot_iron")::equals);
        infuserRecipe.init();
        CraftingRecipes.registerRecipe(infuserRecipe);

        ShapedCraftingRecipe gravityGunRecipe = new ShapedCraftingRecipe(3, 3);
        gravityGunRecipe.setOutput(Item.getItem(GravityGun.id.toString()), 1);
        gravityGunRecipe.setInput(0, 0, (i) -> i != null && i.getID().equals(MysticalGem.id.toString()));
        gravityGunRecipe.setInput(1, 1, (i) -> i != null && i.getID().equals(MysticalGem.id.toString()));
        gravityGunRecipe.setInput(0, 1, Item.getItem("base:ingot_aluminium")::equals);
        gravityGunRecipe.setInput(1, 0, Item.getItem("base:ingot_aluminium")::equals);
        gravityGunRecipe.setInput(1, 2, Item.getItem("base:ingot_aluminium")::equals);
        gravityGunRecipe.setInput(2, 1, Item.getItem("base:ingot_aluminium")::equals);
        gravityGunRecipe.setInput(2, 2, Item.getItem("base:ingot_aluminium")::equals);
        gravityGunRecipe.init();
        CraftingRecipes.registerRecipe(gravityGunRecipe);

        ShapedCraftingRecipe linkerRecipe = new ShapedCraftingRecipe(3, 3);
        linkerRecipe.setOutput(Item.getItem(Linker.id.toString()), 1);
        linkerRecipe.setInput(0, 0, Block.getById("base:cheese").getDefaultBlockState().getItem()::equals);
        linkerRecipe.setInput(2, 2, Block.getById("base:cheese").getDefaultBlockState().getItem()::equals);
        linkerRecipe.setInput(1, 1, Block.getById("base:metal_panel").getDefaultBlockState().getItem()::equals);
        linkerRecipe.setInput(0, 1, Item.getItem("base:ingot_aluminium")::equals);
        linkerRecipe.setInput(1, 0, Item.getItem("base:ingot_aluminium")::equals);
        linkerRecipe.setInput(1, 2, Item.getItem("base:ingot_aluminium")::equals);
        linkerRecipe.setInput(2, 1, Item.getItem("base:ingot_aluminium")::equals);
        linkerRecipe.init();
        CraftingRecipes.registerRecipe(linkerRecipe);
    }
}
