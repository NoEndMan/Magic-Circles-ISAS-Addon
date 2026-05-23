package net.flameslight.magiccircles.oculus;

import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraftforge.fml.ModList;

public class OculusCompact {
    private static final boolean OCULUS_LOADED = ModList.get().isLoaded("oculus");
    private static boolean lastShaderState = false;

    public static boolean isOculusLoaded() {
        return OCULUS_LOADED;
    }

/*    public static boolean isRenderingShadowPass() {
        if (!isOculusLoaded()) return false;

        try {
            return IrisApi.getInstance().isRenderingShadowPass();
        } catch (Exception e) {
            return false;
        }
    }*/


    public static boolean isShaderPackInUse() {
        return isOculusLoaded() && IrisApi.getInstance().isShaderPackInUse();
    }

    public static void handleOnRenderUpdate() {
        if (!isOculusLoaded()) return;

        boolean currentState = isShaderPackInUse();

        if (currentState != lastShaderState) {
            // State has changed!
            onShaderToggle(currentState);
            lastShaderState = currentState;
        }
    }

    private static void onShaderToggle(boolean enabled) {
        MagicCirclesRender.clearCache();
    }
}
