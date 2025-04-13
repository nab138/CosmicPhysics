package me.nabdev.physicsmod;

import com.github.puzzle.core.loader.provider.mod.entrypoint.impls.ModInitializer;
import com.github.puzzle.core.loader.util.ModLocator;
import com.github.puzzle.game.PuzzleRegistries;
import com.jme3.bullet.objects.PhysicsRigidBody;
import electrostatic4j.snaploader.LibraryInfo;
import electrostatic4j.snaploader.LoadingCriterion;
import electrostatic4j.snaploader.NativeBinaryLoader;
import electrostatic4j.snaploader.filesystem.DirectoryPath;
import electrostatic4j.snaploader.platform.NativeDynamicLibrary;
import electrostatic4j.snaploader.platform.util.PlatformPredicate;
import me.nabdev.physicsmod.commands.Commands;

import java.util.logging.Level;

@SuppressWarnings("unused")
public class PhysicsMod implements ModInitializer {
    public static final boolean portalsLoaded = ModLocator.isModLoaded("seamlessportals");
    @Override
    public void onInit() {


        PuzzleRegistries.EVENT_BUS.subscribe(this);



        Commands.register();

        LibraryInfo info = new LibraryInfo(
                new DirectoryPath("linux/x86-64/com/github/stephengold"),
                "bulletjme", DirectoryPath.USER_DIR);
        NativeBinaryLoader loader = new NativeBinaryLoader(info);
        NativeDynamicLibrary[] libraries = new NativeDynamicLibrary[]{
                new NativeDynamicLibrary("native/linux/x86_64", PlatformPredicate.LINUX_X86_64),
                new NativeDynamicLibrary("native/osx/arm64", PlatformPredicate.MACOS_ARM_64),
                new NativeDynamicLibrary("native/windows/x86_64", PlatformPredicate.WIN_X86_64)
        };
        loader.registerNativeLibraries(libraries).initPlatformLibrary();
        try {
            loader.loadLibrary(LoadingCriterion.INCREMENTAL_LOADING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load native library. Please contact nab138, he may need to add support for your platform.");
        }
        PhysicsRigidBody.logger2.setLevel(Level.WARNING);
    }
}
