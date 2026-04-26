package net.flameslight.magiccircles.datagen.types.transformations.render;

import com.mojang.math.Axis;
import net.flameslight.magiccircles.datagen.render.RendererUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Animations that applied on every animation tick
 */
public class RenderAnimations {
    public static RenderTransformExecutable getCurrentRotationExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) ->
            poseStack.mulPose(Axis.ZP.rotationDegrees(magicCircleData.getRotation()));
    }

    public static RenderTransformExecutable getCurrentFacingRotationExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            poseStack.mulPose(Axis.YP.rotationDegrees(magicCircleData.getYRotation()));
            poseStack.mulPose(Axis.XP.rotationDegrees(magicCircleData.getXRotation()));
        };
    }

    public static RenderTransformExecutable getCasterBottomPositionRelativeWorldSpaceExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            // 1. Get interpolated position
            double lerpX = Mth.lerp(partialTick, entitySnapshot.xo, entitySnapshot.x);
            double lerpY = Mth.lerp(partialTick, entitySnapshot.yo, entitySnapshot.y);
            double lerpZ = Mth.lerp(partialTick, entitySnapshot.zo, entitySnapshot.z);

            // 3. Move to the Caster's Eyes (Relative to Camera)
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 camPos = camera.getPosition();

            // Translate to the entity's feet, plus their eye height
            poseStack.translate(
                    lerpX - camPos.x,
                    lerpY - camPos.y,
                    lerpZ - camPos.z
            );
        };
    }

    public static RenderTransformExecutable getCasterBillboardPositionExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            // Yaw - rotation around y-axis (left/right)
            float yRot = Mth.lerp(partialTick, entitySnapshot.yRotO, entitySnapshot.yRot);

            // Pitch - rotation around x-axis (up/down)
            float xRot = Mth.lerp(partialTick, entitySnapshot.xRotO, entitySnapshot.xRot);

            Vec3 position = RendererUtils.getBillboardElementPositioning(magicCircleData.getXOffset(),
                    magicCircleData.getYOffset(),
                    magicCircleData.getZOffset(),
                    entitySnapshot.eyeHeight,
                    yRot,
                    xRot);

            poseStack.translate(position.x, position.y, position.z);
        };
    }

    public static RenderTransformExecutable getCurrentSizeScalingExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            float appliedSize = magicCircleData.getCurrentSize();

            poseStack.scale(appliedSize, appliedSize, 1);
        };
    }

    public static RenderTransformExecutable getSyncedPositionedExecutable() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            poseStack.translate(magicCircleData.getXOffset(), magicCircleData.getYOffset(), magicCircleData.getZOffset());
        };
    }
}
