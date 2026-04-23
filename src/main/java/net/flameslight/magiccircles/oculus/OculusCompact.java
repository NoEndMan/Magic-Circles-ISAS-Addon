package net.flameslight.magiccircles.oculus;

import net.irisshaders.iris.api.v0.IrisApi;

public class OculusCompact {
    private static final boolean OCULUS_LOADED;

    static {
        boolean loaded = false;
        try {
            Class.forName("net.irisshaders.iris.api.v0.IrisApi");
//            ModLogger.info("found oculus/iris shaders mod loaded");
            loaded = true;
        } catch (ClassNotFoundException e) {
            // Oculus not installed
//            ModLogger.info("no oculus/iris shaders mod detected");
        }
        OCULUS_LOADED = loaded;
    }

    public static boolean isRenderingShadowPass() {
        if (!OCULUS_LOADED) return false;
        try {
            return IrisApi.getInstance().isRenderingShadowPass();
        } catch (Exception e) {
            return false;
        }
    }
}
