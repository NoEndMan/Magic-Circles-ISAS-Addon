package net.flameslight.magiccircles.datagen.types.transformations.render;

import com.mojang.math.Axis;
import net.flameslight.magiccircles.datagen.render.RendererUtils;
import net.minecraft.util.Mth;

/**
 * Animations that applied on every animation tick
 */
public class RenderAnimations {
    public static RenderTransformExecutable getCurrentRotationExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) ->
            poseStack.mulPose(Axis.ZP.rotationDegrees(magicCircleData.getRotation()));
    }

    public static RenderTransformExecutable getBillboardPositioningExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            // Yaw - rotation around y-axis (left/right)
            float yRot = Mth.lerp(partialTick, entitySnapshot.yRotO, entitySnapshot.yRot);

            // Pitch - rotation around x-axis (up/down)
            float xRot = Mth.lerp(partialTick, entitySnapshot.xRotO, entitySnapshot.xRot);

            RendererUtils.getBillboardElementPositioning(poseStack,
                    magicCircleData.getXOffset(),
                    magicCircleData.getYOffset(),
                    magicCircleData.getZOffset(),
                    entitySnapshot.eyeHeight,
                    yRot,
                    xRot);
            RendererUtils.makeElementToFaceCaster(poseStack, yRot, xRot);
        };
    }

    public static RenderTransformExecutable getGroundFacingExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) ->
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
    }

    public static RenderTransformExecutable getCurrentSizeScalingExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            float appliedSize = magicCircleData.getCurrentSize();

            poseStack.scale(appliedSize, appliedSize, 1);
        };
    }
}
