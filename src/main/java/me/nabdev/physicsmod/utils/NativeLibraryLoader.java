package me.nabdev.physicsmod.utils;

import com.jme3.system.JmeSystem;
import com.jme3.system.Platform;
import me.nabdev.physicsmod.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public final class NativeLibraryLoader {
    public static boolean loadLibbulletjme(String buildType, String flavor) {
        String fullName = getFullName(buildType, flavor);

        boolean success = false;
        try {
            InputStream in = NativeLibraryLoader.class.getClassLoader().getResourceAsStream("natives/" + fullName);
            //noinspection SpellCheckingInspection
            File fileOut = new File(System.getProperty("java.io.tmpdir") + "/cosmicphysics/natives/" + fullName);
            fileOut.deleteOnExit();
            Constants.LOGGER.info("Writing native to: {}", fileOut.getAbsolutePath());
            OutputStream out = FileUtils.openOutputStream(fileOut);
            assert in != null;
            IOUtils.copy(in, out);
            in.close();
            out.close();
            System.load(fileOut.toString());
            success = true;
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            Constants.LOGGER.error("Failed to load native library: {}", fullName);
        }

        return success;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static String getFullName(String buildType, String flavor) {
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


        return platform + buildType + flavor + "_" + name;
    }
}