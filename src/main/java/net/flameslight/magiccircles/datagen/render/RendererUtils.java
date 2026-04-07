package net.flameslight.magiccircles.datagen.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class RendererUtils {
    public static void drawQuad(PoseStack ps, VertexConsumer builder, float r, float g, float b, float alpha) {
        Matrix4f matrix = ps.last().pose();
        float size = 0.5f;
        int overlay = OverlayTexture.NO_OVERLAY;
        int light = LightTexture.FULL_BRIGHT;

        // Front
        builder.vertex(matrix, -size, -size, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, -size, size, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, size, size, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, size, -size, 0).color(r, g, b, alpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
    }

    /**
     * Applying rotation and translate so element would face view direction no matter from where looked.
     * On 2D screen it would look like it doesn't move.
     * @param poseStack
     * @param xOffset
     * @param yOffset
     * @param zOffset
     * @param initialHeight - initial height of camera view from 0, like eyes height
     * @param yRotDegrees Yaw - rotation around y axis (left/right)
     * @param xRotDegrees Pitch - rotation around x axis (up/down)
     * @return new cords of element after look rotation
     */
    public static Vec3 getBillboardElementPositioning(PoseStack poseStack,
                                                      float xOffset,
                                                      float yOffset,
                                                      float zOffset,
                                                      float initialHeight,
                                                      float yRotDegrees,
                                                      float xRotDegrees) {
        float yRotRadians = (float) Math.toRadians(-yRotDegrees);
        float xRotRadians = (float) Math.toRadians(xRotDegrees);

        // --- Rotate around X (pitch) ---
        double cosPitch = Math.cos(xRotRadians);
        double sinPitch = Math.sin(xRotRadians);

        double y1 = yOffset * cosPitch - zOffset * sinPitch;
        double z1 = yOffset * sinPitch + zOffset * cosPitch;
        double x1 = xOffset;

        // --- Rotate around Y (yaw) ---
        double cosYaw = Math.cos(yRotRadians);
        double sinYaw = Math.sin(yRotRadians);

        double x2 = x1 * cosYaw + z1 * sinYaw;
        double z2 = -x1 * sinYaw + z1 * cosYaw;
        double y2 = y1 + initialHeight;

        poseStack.translate(x2, y2, z2);

        return new Vec3(x2, y2, z2);
    }

    /** Apply Rotation to match entity view */
    public static void makeElementToFaceCaster(PoseStack poseStack, float yRotDegrees, float xRotDegrees) {
        poseStack.mulPose(Axis.YP.rotationDegrees(-yRotDegrees));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRotDegrees));
    }
}
