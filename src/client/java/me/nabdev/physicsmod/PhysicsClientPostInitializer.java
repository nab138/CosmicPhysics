package me.nabdev.physicsmod;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientPostModInitializer;
import finalforeach.cosmicreach.items.Item;
import finalforeach.cosmicreach.items.recipes.CraftingRecipe;
import finalforeach.cosmicreach.items.recipes.CraftingRecipes;
import finalforeach.cosmicreach.util.Identifier;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.PhysicsInfuser;

public class PhysicsClientPostInitializer implements ClientPostModInitializer {
    @Override
    public void onPostInit() {
        CraftingRecipe infuserRecipe = new CraftingRecipe(Identifier.of("physicsmod", "infuser_recipe"), 2);
        infuserRecipe.setOutput(Item.getItem(PhysicsInfuser.id.toString()), 1);
        infuserRecipe.setInput(0, Item.getItem("base:ingot_iron"), 6);
        infuserRecipe.setInput(1, Item.getItem("physics:mystical_gem"), 1);
        CraftingRecipes.registerRecipe(infuserRecipe.rootIdentifier, infuserRecipe);

        CraftingRecipe gravityGunRecipe = new CraftingRecipe(Identifier.of("physicsmod", "gravity_gun_recipe"), 2);
        gravityGunRecipe.setOutput(Item.getItem(GravityGun.id.toString()), 1);
        gravityGunRecipe.setInput(0, Item.getItem("physics:mystical_gem"), 2);
        gravityGunRecipe.setInput(1, Item.getItem("base:ingot_aluminium"), 5);
        CraftingRecipes.registerRecipe(gravityGunRecipe.rootIdentifier, gravityGunRecipe);

        CraftingRecipe linkerRecipe = new CraftingRecipe(Identifier.of("physicsmod", "linker_recipe"), 3);
        linkerRecipe.setOutput(Item.getItem(Linker.id.toString()), 1);
        linkerRecipe.setInput(0, Item.getItem("physics:mystical_gem"), 1);
        linkerRecipe.setInput(1, Item.getItem("base:ingot_aluminium"), 4);
        linkerRecipe.setInput(2, Item.getItem("base:cheese"), 2);
    }
}
