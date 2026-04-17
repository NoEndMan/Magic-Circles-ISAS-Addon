package net.flameslight.magiccircles.datagen.types.magicCircle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.flameslight.magiccircles.datagen.Utils;
import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.transformations.TransformManager;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class MagicCircleData {
    public static final float MAX_OPACITY = 1f;
    public static final float MIN_OPACITY = 0.001f;

    public final RenderType renderType;
    public final int overlay;
    public final int light;
    public final String castedSpellName;
    public final EntitySnapshot caster;
    public final float staticSize;

    private final TransformManager transformManager;

    /** must be greater than 0*/
    private final int initTotalTicks;
    private final int finalTotalTicks;

    private int ticks;
    private int initTicks;
    private int finalTicks;
    private float lastFullTicks;
    private float rotation;
    private float rotationChange;
    private float baseR;
    private float baseG;
    private float baseB;
    private float opacityChangePerTick;
    private float currentSize;
    private float xOffset; // positive to left
    private float zOffset; // positive to forwards
    private float yOffset; // positive to up
    private float opacity;
    private boolean isFadingIn;
    private boolean isFadingOut;
    private boolean isConcealed;

    public MagicCircleData(TransformManager transformManager,
                           EntitySnapshot caster,
                           String castedSpellName,
                           int hexColor,
                           RenderType renderType,
                           float staticSize,
                           float rotationChange,
                           float xOffset,
                           float zOffset,
                           float yOffset,
                           int initTotalTicks,
                           int finalTotalTicks,
                           int light,
                           int overlay) {
        this.opacity = MIN_OPACITY;
        this.isFadingIn = false;
        this.isFadingOut = false;
        this.isConcealed = false;
        this.opacityChangePerTick = 0f;
        this.currentSize = staticSize;
        this.finalTicks = 0;
        this.rotation = 0;
        this.lastFullTicks = 0;

        float[] color = Utils.extractRGBFromHexColor(hexColor);

        this.baseR = color[0];
        this.baseG = color[1];
        this.baseB = color[2];

        this.transformManager = transformManager;
        this.caster = caster;
        this.castedSpellName = castedSpellName;
        this.renderType = renderType;
        this.light = light;
        this.overlay = overlay;
        this.staticSize = staticSize;
        this.rotationChange = rotationChange;
        this.xOffset = xOffset;
        this.zOffset = zOffset;
        this.yOffset = yOffset;
        this.initTotalTicks = initTotalTicks;
        this.finalTotalTicks = finalTotalTicks;
    }

    public void updatePerTick() {
        this.ticks++;

        if (isFadingIn) {
            this.isConcealed = false;

            if(this.initTicks < this.initTotalTicks)
                this.initTicks++;

            if (this.opacity < MAX_OPACITY)
                this.opacity = Math.min(MAX_OPACITY, this.opacity + opacityChangePerTick);
            else
                this.isFadingIn = false;
        } else if (isFadingOut) {
            if(this.finalTicks < this.finalTotalTicks)
                this.finalTicks++;

            if (this.opacity > MIN_OPACITY) {
                this.opacity = Math.max(MIN_OPACITY, this.opacity + opacityChangePerTick);
            } else {
                isFadingOut = false;

                this.isConcealed = true;
            }
        }
    }

    public void startInitElseTermination(boolean fadeInElseOut) {
        if(fadeInElseOut) {
            this.opacityChangePerTick = (MAX_OPACITY - opacity) / this.initTotalTicks;
            this.isFadingIn = true;
            this.isFadingOut = false;
        } else {
            this.opacityChangePerTick = (MIN_OPACITY - opacity) / this.finalTotalTicks;
            this.isFadingIn = false;
            this.isFadingOut = true;
        }
    }

    public void executePermanentRenderTransforms(PoseStack poseStack, EntitySnapshot entitySnapshot, float partialTick) {
        this.transformManager.executeRenderTransformations(poseStack, entitySnapshot, this, partialTick);
    }

    public void executePermanentDataTransforms(EntitySnapshot entitySnapshot, float newPartialTicks) {
        this.transformManager.executePermanentDataTransformations(entitySnapshot, this, this.ticks + newPartialTicks);
    }

    public void executeInitTransforms(EntitySnapshot entitySnapshot, float newPartialTicks) {
        if(this.isFadingIn)
            this.transformManager.executeInitTransformations(entitySnapshot, this, this.initTicks + newPartialTicks, this.initTotalTicks);
    }

    public void executeFinalTransforms(EntitySnapshot entitySnapshot, float newPartialTicks) {
        if(this.isFadingOut)
            this.transformManager.executeFinalTransformations(entitySnapshot, this, this.finalTicks + newPartialTicks, this.finalTotalTicks);
    }

    public boolean isConcealed() {
        return this.isConcealed;
    }

    /** @return [r,g,b,a] */
    public float[] getColor() {
        return new float[]{baseR * opacity, baseG * opacity, baseB * opacity, opacity};
    }

    public boolean isFadingIn() {
        return isFadingIn;
    }

    public boolean isFadingOut() {
        return isFadingOut;
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

    public float getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(float currentSize) {
        this.currentSize = currentSize;
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public float getRotationChange() {
        return rotationChange;
    }

    public void setRotationChange(float change) {
        this.rotationChange = change;
    }

    public void setLastFullTicks(float lastFullTicks) {
        this.lastFullTicks = lastFullTicks;
    }

    public float getLastFullTicks() {
        return this.lastFullTicks;
    }

    public int getTicks() {
        return this.ticks;
    }
}
