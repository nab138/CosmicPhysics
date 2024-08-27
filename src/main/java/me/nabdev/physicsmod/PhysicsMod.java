package me.nabdev.physicsmod;

import com.github.puzzle.core.PuzzleRegistries;
import com.github.puzzle.core.localization.ILanguageFile;
import com.github.puzzle.core.localization.LanguageManager;
import com.github.puzzle.core.localization.files.LanguageFileVersion1;
import com.github.puzzle.core.resources.ResourceLocation;
import com.github.puzzle.game.events.OnPreLoadAssetsEvent;
import com.github.puzzle.game.items.IModItem;
import com.github.puzzle.loader.entrypoint.interfaces.ModInitializer;
import finalforeach.cosmicreach.entities.EntityCreator;
import me.nabdev.physicsmod.entities.Cube;
import me.nabdev.physicsmod.commands.Commands;
import me.nabdev.physicsmod.items.Launcher;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

@SuppressWarnings("unused")
public class PhysicsMod implements ModInitializer {
    @Override
    public void onInit() {
        PuzzleRegistries.EVENT_BUS.register(this);

        EntityCreator.registerEntityCreator(Cube.id.toString(), Cube::new);
        IModItem.registerItem(new Launcher());

        Commands.register();
    }

    @Subscribe
    public void onEvent(OnPreLoadAssetsEvent event) {
        ILanguageFile lang;
        try {
            lang = LanguageFileVersion1.loadLanguageFromString(new ResourceLocation(Constants.MOD_ID, "languages/en-US.json").locate().readString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LanguageManager.registerLanguageFile(lang);
    }

}
