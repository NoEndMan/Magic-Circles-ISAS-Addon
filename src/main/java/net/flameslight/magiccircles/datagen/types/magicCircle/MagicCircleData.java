package net.flameslight.magiccircles.datagen.types.magicCircle;

import com.mojang.blaze3d.vertex.PoseStack;
import net.flameslight.magiccircles.datagen.Utils;
import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.transformations.TransformManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;

public class MagicCircleData {
    public static final float MAX_OPACITY = 1f;
    public static final float MIN_OPACITY = 0.001f;

    public final RenderType renderType;
    public final String castedSpellName;
    public final EntitySnapshot caster;
    public final LivingEntity syncedCaster;
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
    private float xRotation;
    private float yRotation;
    private float baseR;
    private float baseG;
    private float baseB;
    private float currentSize;
    private float xOffset; // positive to left
    private float zOffset; // positive to forwards
    private float yOffset; // positive to up
    private float opacity;
    private boolean isFadingIn;
    private boolean isFadingOut;
    private boolean isConcealed;

    public MagicCircleData(TransformManager transformManager,
                           LivingEntity syncedCaster,
                           String castedSpellName,
                           float[] RGBColor,
                           RenderType renderType,
                           float staticSize,
                           float rotationChange,
                           float xOffset,
                           float zOffset,
                           float yOffset,
                           int initTotalTicks,
                           int finalTotalTicks) {
        this.opacity = MIN_OPACITY;
        this.isFadingIn = false;
        this.isFadingOut = false;
        this.isConcealed = false;
        this.currentSize = staticSize;
        this.finalTicks = 1;
        this.ticks = 0;
        this.rotation = 0;
        this.xRotation = 0;
        this.yRotation = 0;
        this.lastFullTicks = 0;

        this.baseR = RGBColor[0];
        this.baseG = RGBColor[1];
        this.baseB = RGBColor[2];

        this.transformManager = transformManager;
        this.caster = new EntitySnapshot(syncedCaster);
        this.syncedCaster = syncedCaster;
        this.castedSpellName = castedSpellName;
        this.renderType = renderType;
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

            if(this.initTicks <= this.initTotalTicks)
                this.initTicks++;
            else
                this.isFadingIn = false;
        } else if (isFadingOut) {
            if(this.finalTicks <= this.finalTotalTicks)
                this.finalTicks++;
            else {
                this.isConcealed = true;

                isFadingOut = false;
            }
        }
    }

    public void startInitElseTermination(boolean fadeInElseOut) {
        if(fadeInElseOut) {
            this.isFadingIn = true;
            this.isFadingOut = false;
        } else {
            this.isFadingIn = false;
            this.isFadingOut = true;
        }
    }

    public void executePermanentRenderTransforms(PoseStack poseStack, EntitySnapshot entitySnapshot, float partialTick) {
        this.transformManager.executeRenderTransformations(poseStack, entitySnapshot, this, partialTick);
    }

    public void executeUntilFinalDataTransforms(EntitySnapshot entitySnapshot, float newPartialTicks) {
        if(!this.isFadingOut && !this.isConcealed)
            this.transformManager.executeUntilFinalTransformations(entitySnapshot, this, this.getTicksDifferenceFromLastFullTicks(newPartialTicks), this.ticks + newPartialTicks);
    }

    public void executePermanentDataTransforms(EntitySnapshot entitySnapshot, float newPartialTicks) {
        this.transformManager.executePermanentDataTransformations(entitySnapshot, this, this.getTicksDifferenceFromLastFullTicks(newPartialTicks), this.ticks + newPartialTicks);
    }

    public void executeInitTransforms(EntitySnapshot entitySnapshot, float newPartialTicks) {
        if(this.isFadingIn)
            this.transformManager.executeInitTransformations(entitySnapshot, this, this.getTicksDifferenceFromLastFullTicks(newPartialTicks), this.initTicks + newPartialTicks);
    }

    public void executeFinalTransforms(EntitySnapshot entitySnapshot, float newPartialTicks) {
        if(this.isFadingOut)
            this.transformManager.executeFinalTransformations(entitySnapshot, this, this.getTicksDifferenceFromLastFullTicks(newPartialTicks), this.finalTicks + newPartialTicks);
    }

    public boolean isConcealed() {
        return this.isConcealed;
    }

    /** @return [r,g,b,a] */
    public float[] getColor(boolean useDesaturation) {
        if(useDesaturation)
            return Utils.getColorDesaturationByOpacity(baseR, baseG, baseB, opacity);

        return new float[]{baseR, baseG, baseB, opacity};

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

    public int getTicks() {
        return this.ticks;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float newOpacity) {
        this.opacity = newOpacity;
    }

    public float getXRotation() {
        return xRotation;
    }

    public void setXRotation(float xRotation) {
        this.xRotation = xRotation;
    }

    public float getYRotation() {
        return yRotation;
    }

    public void setYRotation(float yRotation) {
        this.yRotation = yRotation;
    }

    private float getTicksDifferenceFromLastFullTicks(float newPartialTicks) {
        return this.ticks + newPartialTicks - this.lastFullTicks;
    }
}
