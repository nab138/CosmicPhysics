package me.nabdev.physicsmod;

import com.github.puzzle.core.PuzzleRegistries;
import com.github.puzzle.game.events.OnPreLoadAssetsEvent;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.loader.entrypoint.interfaces.ModInitializer;
import finalforeach.cosmicreach.entities.EntityCreator;
import me.nabdev.physicsmod.commands.Commands;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.items.GravityGun;
import me.nabdev.physicsmod.items.Linker;
import me.nabdev.physicsmod.items.PhysicsInfuser;
import me.nabdev.physicsmod.utils.NativeLibraryLoader;
import org.greenrobot.eventbus.Subscribe;

@SuppressWarnings("unused")
public class PhysicsMod implements ModInitializer {
    @Override
    public void onInit() {
        PuzzleRegistries.EVENT_BUS.register(this);
        EntityCreator.registerEntityCreator(Cube.id.toString(), Cube::new);
        IModItem.registerItem(new GravityGun());
        IModItem.registerItem(new PhysicsInfuser());
        IModItem.registerItem(new Linker());

        Commands.register();

        boolean success = NativeLibraryLoader.loadLibbulletjme("Debug", "Sp");
        if (!success) {
            throw new RuntimeException("Failed to load native library. Please contact nab138, he may need to add support for your platform.");
        }
    }

    @Subscribe
    public void onEvent(OnPreLoadAssetsEvent event) {}
}
