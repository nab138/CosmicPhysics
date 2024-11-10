package me.nabdev.physicsmod;

import com.github.puzzle.core.loader.launch.provider.mod.entrypoint.impls.ClientModInitializer;
import com.github.puzzle.game.PuzzleRegistries;
import com.github.puzzle.game.items.IModItem;
import com.jme3.bullet.objects.PhysicsRigidBody;
import finalforeach.cosmicreach.entities.EntityCreator;
import finalforeach.cosmicreach.items.Item;
import me.nabdev.physicsmod.commands.Commands;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.MysticalGem;
import me.nabdev.physicsmod.items.PhysicsInfuser;
import me.nabdev.physicsmod.utils.NativeLibraryLoader;

import java.util.function.Predicate;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class PhysicsMod implements ClientModInitializer {
    @Override
    public void onInit() {
        PuzzleRegistries.EVENT_BUS.subscribe(this);
        EntityCreator.registerEntityCreator(Cube.id.toString(), Cube::new);
        IModItem.registerItem(new MysticalGem());
        IModItem.registerItem(new PhysicsInfuser());
        IModItem.registerItem(new GravityGun());
        IModItem.registerItem(new Linker());

        Commands.register();

        boolean success = NativeLibraryLoader.loadLibbulletjme("Release", "Sp");
        if (!success) {
            throw new RuntimeException("Failed to load native library. Please contact nab138, he may need to add support for your platform.");
        }
        PhysicsRigidBody.logger2.setLevel(Level.WARNING);
    }

    public static Predicate<Item> itemPredicate(String id) {
        return item -> {
            if (item == null) return false;
            System.out.println(item.getID());
            return item.getID().equals(id);
        };
    }
}
