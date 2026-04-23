package net.flameslight.magiccircles.datagen;

import net.minecraft.util.Mth;

public class Utils {
    /** return [r,g,b] */
    public static float[] extractRGBFromHexColor(int color) {
        // Extract RGB
        float baseR = ((color >> 16) & 0xFF) / 255.0f;
        float baseG = ((color >> 8) & 0xFF) / 255.0f;
        float baseB = (color & 0xFF) / 255.0f;

        return new float[]{baseR, baseG, baseB};
    }

    /** @return [r,g,b,a] */
    public static float[] getColorDesaturationByOpacity(float baseR, float baseG, float baseB, float opacity) {
        /*return new float[]{baseR, baseG, baseB, opacity};*/
        // 1. Apply Sine easing to the opacity
        // This makes the fade stay vibrant longer before dropping
        float easeOpacity = (float) Math.sin(opacity * (Math.PI / 2));

        // 2. Calculate the "Gray Point" of the original color
        // This is the target color the circle "washes out" into
        float luminance = (0.2126f * baseR) + (0.7152f * baseG) + (0.0722f * baseB);

        // 3. Desaturation: Blend original color toward its own grayscale value
        // As easeOpacity drops, 'saturation' drops
        float lerpR = baseR + (luminance - baseR) * (1.0f - easeOpacity);
        float lerpG = baseG + (luminance - baseG) * (1.0f - easeOpacity);
        float lerpB = baseB + (luminance - baseB) * (1.0f - easeOpacity);

        // 4. Final Dimming: Scale the desaturated color by the opacity
        // This ensures it eventually hits (0,0,0)
        float finalR = lerpR * easeOpacity;
        float finalG = lerpG * easeOpacity;
        float finalB = lerpB * easeOpacity;

        return new float[]{finalR, finalG, finalB, opacity};

/*        // 2. Convert to HSL
        float[] hsl = rgbToHsl(baseR, baseG, baseB);

        // 3. Desaturate: S channel drops directly toward 0 (pure gray)
        //    Much cleaner than blending toward a luminance gray in RGB
        hsl[1] *= easeOpacity;

        // 4. Dim: L channel scales toward black
        hsl[2] *= easeOpacity;

        // 5. Convert back - hue is completely untouched
        float[] rgb = hslToRgb(hsl[0], hsl[1], hsl[2]);
        return new float[]{ rgb[0], rgb[1], rgb[2], opacity };*/
    }

    /**
     *
     * @param r
     * @param g
     * @param b
     * @param factor - illumination change factor between -1 to 1
     * @return [r,g,b]
     */
    public static float[] brighten(float r, float g, float b, float factor) {
        float[] hsv = rgbToHsv(r, g, b);
        hsv[2] =  Mth.clamp(hsv[2] + factor, 0, 1.0f); // Bump Value, clamp between 0 to 1
        return hsvToRgb(hsv[0], hsv[1], hsv[2]);
    }
    /**
     * Converts RGB (0.0-1.0 each) to HSL.
     * Returns float[3]: { hue (0-360), saturation (0-1), lightness (0-1) }
     */
    private static float[] rgbToHsl(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float lightness = (max + min) / 2.0f;

        float saturation = 0.0f;
        if (delta != 0.0f) {
            saturation = delta / (1.0f - Math.abs(2.0f * lightness - 1.0f));
        }

        float hue = 0.0f;
        if (delta != 0.0f) {
            if (max == r) {
                hue = 60.0f * (((g - b) / delta) % 6);
            } else if (max == g) {
                hue = 60.0f * (((b - r) / delta) + 2);
            } else {
                hue = 60.0f * (((r - g) / delta) + 4);
            }
        }
        if (hue < 0) hue += 360.0f;

        return new float[]{ hue, saturation, lightness };
    }

    /**
     * Converts HSL { hue (0-360), saturation (0-1), lightness (0-1) } to RGB (0.0-1.0 each).
     */
    private static float[] hslToRgb(float hue, float saturation, float lightness) {
        float c = (1.0f - Math.abs(2.0f * lightness - 1.0f)) * saturation;
        float x = c * (1.0f - Math.abs((hue / 60.0f) % 2 - 1.0f));
        float m = lightness - c / 2.0f;

        float r, g, b;
        if      (hue < 60)  { r = c; g = x; b = 0; }
        else if (hue < 120) { r = x; g = c; b = 0; }
        else if (hue < 180) { r = 0; g = c; b = x; }
        else if (hue < 240) { r = 0; g = x; b = c; }
        else if (hue < 300) { r = x; g = 0; b = c; }
        else                { r = c; g = 0; b = x; }

        return new float[]{ r + m, g + m, b + m };
    }

    /** RGB (0-1) → HSV: float[3] { hue (0-360), saturation (0-1), value (0-1) } */
    public static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float value = max;

        float saturation = (max == 0) ? 0 : delta / max;

        float hue = 0.0f;
        if (delta != 0.0f) {
            if (max == r)      hue = 60.0f * (((g - b) / delta) % 6);
            else if (max == g) hue = 60.0f * (((b - r) / delta) + 2);
            else               hue = 60.0f * (((r - g) / delta) + 4);
        }
        if (hue < 0) hue += 360.0f;

        return new float[]{ hue, saturation, value };
    }

    /** HSV { hue (0-360), saturation (0-1), value (0-1) } → RGB (0-1) */
    public static float[] hsvToRgb(float hue, float saturation, float value) {
        float c = value * saturation;
        float x = c * (1.0f - Math.abs((hue / 60.0f) % 2 - 1.0f));
        float m = value - c;

        float r, g, b;
        if      (hue < 60)  { r = c; g = x; b = 0; }
        else if (hue < 120) { r = x; g = c; b = 0; }
        else if (hue < 180) { r = 0; g = c; b = x; }
        else if (hue < 240) { r = 0; g = x; b = c; }
        else if (hue < 300) { r = x; g = 0; b = c; }
        else                { r = c; g = 0; b = x; }

        return new float[]{ r + m, g + m, b + m };
    }
}
