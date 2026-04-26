package net.flameslight.magiccircles.datagen.types.transformations.render;

import com.mojang.math.Axis;
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

    public static RenderTransformExecutable getWorldRelativeSpaceFromCasterPosition() {
        return (poseStack, entitySnapshot, magicCircleData, partialTick) -> {
            Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
            Vec3 camPos = camera.getPosition();

            poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
            poseStack.translate(entitySnapshot.x, entitySnapshot.y, entitySnapshot.z);
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
            poseStack.translate(magicCircleData.getX(),
                    magicCircleData.getY(),
                    magicCircleData.getZ());
        };
    }
}
