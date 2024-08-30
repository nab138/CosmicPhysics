package me.nabdev.physicsmod.utils;

import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import jme3utilities.MyString;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NativeLibraryLoader {
    public static final Logger logger = Logger.getLogger(NativeLibraryLoader.class.getName());

    private NativeLibraryLoader() {
    }

    public static boolean loadLibbulletjme(String buildType, String flavor) {
        assert buildType.equals("Debug") || buildType.equals("Release") : buildType;

        assert flavor.equals("Sp") || flavor.equals("SpMt") || flavor.equals("SpMtQuickprof") || flavor.equals("SpQuickprof") || flavor.equals("Dp") || flavor.equals("DpMt") : flavor;

        Platform platform = JmeSystem.getPlatform();
        Platform.Os os = platform.getOs();
        String name = switch (os) {
            case Android, Linux -> "libbulletjme.so";
            case MacOS -> "libbulletjme.dylib";
            case Windows -> "bulletjme.dll";
            default -> throw new RuntimeException("platform = " + platform);
        };


        //File file = new ResourceLocation(Constants.MOD_ID, "natives/" + platform + buildType + flavor + "_" + name).locate().file();
        File file = getFileFromURL("natives/" + platform + buildType + flavor + "_" + name);
        String absoluteFilename = file.getAbsolutePath();
        System.out.println("Loading native library from " + MyString.quote(absoluteFilename));
        boolean success = false;
        if (!file.exists()) {
            logger.log(Level.SEVERE, "{0} does not exist", absoluteFilename);
        } else if (!file.canRead()) {
            logger.log(Level.SEVERE, "{0} is not readable", absoluteFilename);
        } else {
            logger.log(Level.INFO, "Loading native library from {0}", absoluteFilename);
            System.load(absoluteFilename);
            success = true;
        }

        return success;
    }

    private static File getFileFromURL(String path) {
        URL url =  NativeLibraryLoader.class.getClassLoader().getResource(path);
        File file;
        try {
            assert url != null;
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        return file;
    }
}