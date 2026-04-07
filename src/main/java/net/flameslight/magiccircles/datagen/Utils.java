package net.flameslight.magiccircles.datagen;

public class Utils {
    /** return [r,g,b] */
    public static float[] extractRGBFromHexColor(int color) {
        // Extract RGB
        float baseR = ((color >> 16) & 0xFF) / 255.0f;
        float baseG = ((color >> 8) & 0xFF) / 255.0f;
        float baseB = (color & 0xFF) / 255.0f;

        return new float[]{baseR, baseG, baseB};
    }
}
