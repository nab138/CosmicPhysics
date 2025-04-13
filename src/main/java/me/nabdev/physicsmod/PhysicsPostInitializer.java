package me.nabdev.physicsmod;

import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.PostModInitializer;
import finalforeach.cosmicreach.entities.EntityCreator;
import io.github.puzzle.cosmic.item.AbstractCosmicItem;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.entities.PortalCube;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.MysticalGem;
import me.nabdev.physicsmod.items.PhysicsInfuser;

import static me.nabdev.physicsmod.PhysicsMod.portalsLoaded;

@SuppressWarnings("unused")
public class PhysicsPostInitializer implements PostModInitializer {

    @Override
    public void onPostInit() {
        AbstractCosmicItem.register(new MysticalGem());
        AbstractCosmicItem.register(new PhysicsInfuser());
        AbstractCosmicItem.register(new GravityGun());
        AbstractCosmicItem.register(new Linker());
        if(portalsLoaded){
            EntityCreator.registerEntityCreator(Cube.id.toString(), PortalCube::new);
        } else {
            EntityCreator.registerEntityCreator(Cube.id.toString(), Cube::new);
        }

    }
}
