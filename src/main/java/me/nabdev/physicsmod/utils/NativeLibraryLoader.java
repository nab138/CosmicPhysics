package me.nabdev.physicsmod.utils;

import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public final class NativeLibraryLoader {
    public static final Logger logger = Logger.getLogger(NativeLibraryLoader.class.getName());


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


        String fullName = platform + buildType + flavor + "_" + name;

        boolean success = false;
        try {
            // have to use a stream
            InputStream in = NativeLibraryLoader.class.getClassLoader().getResourceAsStream("natives/" + fullName);
                    // always write to different location
            File fileOut = new File(System.getProperty("java.io.tmpdir") + "/cosmicphysics/natives/" + fullName);
            logger.info("Writing native to: " + fileOut.getAbsolutePath());
            OutputStream out = FileUtils.openOutputStream(fileOut);
            assert in != null;
            IOUtils.copy(in, out);
            in.close();
            out.close();
            System.load(fileOut.toString());
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Failed to load native library: " + fullName);
        }

        return success;
    }
}