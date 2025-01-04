package me.nabdev.physicsmod;

import com.github.puzzle.core.Constants;
import com.github.puzzle.core.loader.meta.EnvType;
import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.core.loader.util.ModLocator;
import com.github.puzzle.game.PuzzleRegistries;
import com.github.puzzle.game.items.IModItem;
import com.jme3.bullet.objects.PhysicsRigidBody;
import finalforeach.cosmicreach.entities.EntityCreator;
import me.nabdev.physicsmod.commands.Commands;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.entities.PortalCube;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.MysticalGem;
import me.nabdev.physicsmod.items.PhysicsInfuser;
import me.nabdev.physicsmod.utils.NativeLibraryLoader;

import java.util.logging.Level;

@SuppressWarnings("unused")
public class PhysicsMod implements ModInitializer {
    public static final boolean portalsLoaded = ModLocator.isModLoaded("seamlessportals");
    @Override
    public void onInit() {

        IModItem.registerItem(new MysticalGem());
        IModItem.registerItem(new PhysicsInfuser());
        IModItem.registerItem(new GravityGun());
        IModItem.registerItem(new Linker());
        PuzzleRegistries.EVENT_BUS.subscribe(this);
        if(portalsLoaded){
            EntityCreator.registerEntityCreator(Cube.id.toString(), PortalCube::new);
        } else {
            EntityCreator.registerEntityCreator(Cube.id.toString(), Cube::new);
        }


        Commands.register();

        boolean success = NativeLibraryLoader.loadLibbulletjme("Release", "Sp", Constants.SIDE == EnvType.CLIENT);
        if (!success) {
            throw new RuntimeException("Failed to load native library. Please contact nab138, he may need to add support for your platform.");
        }
        PhysicsRigidBody.logger2.setLevel(Level.WARNING);
    }
}
