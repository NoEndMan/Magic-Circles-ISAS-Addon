package net.flameslight.magiccircles.datagen.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.flameslight.magiccircles.datagen.MagicCircleData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class MagicCirclesRender extends RenderType {

    private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

    // Required constructor (never used directly)
    private MagicCirclesRender(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                               boolean affectsCrumbling, boolean sortOnUpload,
                               Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType cachedCreateRenderType(ResourceLocation texture) {
        return CACHE.computeIfAbsent(texture, tex -> {
            CompositeState state = CompositeState.builder()
                    // stable shader
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)

                    // texture
                    .setTextureState(new TextureStateShard(tex, false, false))

                    // glow effect
                    .setTransparencyState(ADDITIVE_TRANSPARENCY)

                    // render both sides
                    .setCullState(NO_CULL)

                    // fullbright support
                    .setLightmapState(LIGHTMAP)

                    .setOverlayState(OVERLAY)

                    // avoids depth artifacts
                    .setWriteMaskState(COLOR_WRITE)

                    .createCompositeState(true);

            return RenderType.create(
                    "magic_circle_glow",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    state
            );
        });
    }

    public static void renderCircleForClient(LivingEntity livingEntity, MagicCircleData magicCircleData, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        poseStack.pushPose();

        // Interpolated entity position
        double lerpX = Mth.lerp(partialTick, livingEntity.xo, livingEntity.getX());
        double lerpY = Mth.lerp(partialTick, livingEntity.yo, livingEntity.getY());
        double lerpZ = Mth.lerp(partialTick, livingEntity.zo, livingEntity.getZ());

        // Camera-relative position
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(lerpX - cameraPos.x, lerpY - cameraPos.y, lerpZ - cameraPos.z);

        float gameTime = (float) livingEntity.level().getGameTime() + partialTick;
        float rotation = gameTime * 3.0f;
        float scale = magicCircleData.getSize();
        int color = magicCircleData.getColor();
        RenderType renderType = magicCircleData.renderType;

        // Apply vertical offset first
        float yOffset = magicCircleData.getYOffset();

        if (magicCircleData.isPlacedOnGroundElseViewFaced()) {
            // Circle fixed on ground
            poseStack.translate(0, yOffset, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
        } else {
            float xOffset = magicCircleData.getXOffset();
            float zOffset = magicCircleData.getZOffset();

            // Yaw - rotation around y axis (left/right)
            float yRot = Mth.lerp(partialTick, livingEntity.yRotO, livingEntity.getYRot());

            // Pitch - rotation around x axis (up/down)
            float xRot = Mth.lerp(partialTick, livingEntity.xRotO, livingEntity.getXRot());
            float yRotRadians = (float) Math.toRadians(-yRot);
            float xRotRadians = (float) Math.toRadians(xRot);

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
            double y2 = y1 + livingEntity.getEyeHeight();

            poseStack.translate(x2, y2, z2);

            // Apply Rotation to match entity view
            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        }

        // Apply Circle Spin
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

        poseStack.scale(scale, scale, 1);

        float alpha = magicCircleData.getOpacity();

        // Extract RGB
        float baseR = ((color >> 16) & 0xFF) / 255.0f;
        float baseG = ((color >> 8) & 0xFF) / 255.0f;
        float baseB = (color & 0xFF) / 255.0f;

        // Use alpha as intensity instead of transparency
        float r = baseR * alpha;
        float g = baseG * alpha;
        float b = baseB * alpha;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        drawQuad(poseStack, vertexConsumer, r, g, b, alpha);

        poseStack.popPose();
    }

    private static void drawQuad(PoseStack ps, VertexConsumer builder, float r, float g, float b, float alpha) {
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

    public static void clearCache() {
        CACHE.clear();
    }
}