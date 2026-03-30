package net.flameslight.magiccircles.datagen;

import net.minecraft.client.renderer.RenderType;

public class MagicCircleData {
    private static final float MAX_OPACITY = 1f;
    private static final float MIN_OPACITY = 0.1f;
    public final RenderType renderType;
    public final String castedSpellName;
    private int color;
    private float size;
    private float xOffset; // positive to left
    private float zOffset; // positive to forwards
    private float yOffset; // positive to up
    private float opacityChangePerTick = 0f;
    private float opacity = 0.0f;
    private boolean isFadingIn = false;
    private boolean isFadingOut = false;
    private boolean isConcealed = false;
    private boolean isPlacedOnGroundElseViewFaced;

    public MagicCircleData(String castedSpellName,
                           int color,
                           RenderType renderType,
                           boolean isPlacedOnGroundElseViewFaced,
                           float size,
                           float xOffset,
                           float zOffset,
                           float yOffset) {
        this.castedSpellName = castedSpellName;
        this.color = color;
        this.renderType = renderType;
        this.isPlacedOnGroundElseViewFaced = isPlacedOnGroundElseViewFaced;
        this.size = size;
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.yOffset = yOffset;
    }

    public void updatePerTick() {
        if (isFadingIn) {
            this.isConcealed = false;

            if (this.opacity < MAX_OPACITY) {
                this.opacity = Math.min(this.opacity + opacityChangePerTick, MAX_OPACITY);
            } else
                this.isFadingIn = false;
        } else if (isFadingOut) {
            if (this.opacity > MIN_OPACITY) {
                this.opacity = Math.max(this.opacity - opacityChangePerTick, MIN_OPACITY);
            } else {
                isFadingOut = false;

                this.isConcealed = true;
            }
        }
    }

    public void startFadeInElseOut(boolean fadeInElseOut, int ticks) {
        this.opacityChangePerTick = (MAX_OPACITY - MIN_OPACITY) / ticks;

        if(fadeInElseOut) {
            this.isFadingIn = true;
            this.isFadingOut = false;
        } else {
            this.isFadingIn = false;
            this.isFadingOut = true;
        }
    }

    public boolean isConcealed() {
        return this.isConcealed;
    }

    public int getColor() {
        return this.color;
    }

    public float getOpacity() {
        return opacity;
    }

    public boolean isFadingIn() {
        return isFadingIn;
    }

    public boolean isFadingOut() {
        return isFadingOut;
    }

    public float getSize() {
        return size;
    }

    public float getXOffset() {
        return xOffset;
    }

    public float getZOffset() {
        return zOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public boolean isPlacedOnGroundElseViewFaced() {
        return isPlacedOnGroundElseViewFaced;
    }
}
