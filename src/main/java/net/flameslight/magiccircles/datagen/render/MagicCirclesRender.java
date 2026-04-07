package net.flameslight.magiccircles.datagen.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

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

    public static void renderCircleForClient(MagicCircleData magicCircleData, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        poseStack.pushPose();
        EntitySnapshot caster = magicCircleData.caster;

        // Interpolated entity position
        double lerpX = Mth.lerp(partialTick, caster.xo, caster.x);
        double lerpY = Mth.lerp(partialTick, caster.yo, caster.y);
        double lerpZ = Mth.lerp(partialTick, caster.zo, caster.z);

        // Camera-relative position
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(lerpX - cameraPos.x, lerpY - cameraPos.y, lerpZ - cameraPos.z);
        RenderType renderType = magicCircleData.renderType;

        magicCircleData.executePermanentRenderTransforms(poseStack, caster, partialTick);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        float[] color = magicCircleData.getColor();
        RendererUtils.drawQuad(poseStack, vertexConsumer, color[0], color[1], color[2], color[3]);

        poseStack.popPose();
    }

    public static void clearCache() {
        CACHE.clear();
    }
}