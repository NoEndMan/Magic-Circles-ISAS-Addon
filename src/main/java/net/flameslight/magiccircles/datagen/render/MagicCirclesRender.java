package net.flameslight.magiccircles.datagen.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleData;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class MagicCirclesRender extends RenderType {
    private static CirclesStyle usedStyleInCache;
    private static final Map<ResourceLocation, RenderType> CACHE = new HashMap<>();

    // Required constructor (never used directly)
    private MagicCirclesRender(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                               boolean affectsCrumbling, boolean sortOnUpload,
                               Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType cachedCreateRenderType(ResourceLocation texture, CirclesStyle usedStyle) {
        if(!CACHE.isEmpty() && usedStyleInCache != usedStyle) {
            CACHE.clear();
        }

        if(CACHE.isEmpty()) {
            usedStyleInCache = usedStyle;
        }

        return CACHE.computeIfAbsent(texture, tex -> {
            if(usedStyle == CirclesStyle.NEON) {
                CompositeState state = CompositeState.builder()
                        // glowing transparent shader
                        .setShaderState(RenderStateShard.RENDERTYPE_EYES_SHADER)

                        // texture
                        .setTextureState(new TextureStateShard(tex, false, false))

                        // glow effect
                        .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)

                        // render both sides
                        .setCullState(RenderStateShard.NO_CULL)

                        // no fullbright support
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)

                        .setOverlayState(RenderStateShard.NO_OVERLAY)

                        // avoids depth artifacts
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)

                        .createCompositeState(true);

                return RenderType.create(
                        "magic_circle_glow",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        256,
                        false, // affectsCrumbling
                        true, // sortOnUpload
                        state
                );
            } else
                return RenderType.entityTranslucent(tex);
        });
    }

    public static void renderCircleForClient(MagicCircleData magicCircleData,
                                             PoseStack poseStack,
                                             MultiBufferSource bufferSource,
                                             int passedGameTicksForCircle,
                                             float newPartialTick) {
        poseStack.pushPose();
        EntitySnapshot caster = magicCircleData.caster;

        // Interpolated entity position
        double lerpX = Mth.lerp(newPartialTick, caster.xo, caster.x);
        double lerpY = Mth.lerp(newPartialTick, caster.yo, caster.y);
        double lerpZ = Mth.lerp(newPartialTick, caster.zo, caster.z);

        // Camera-relative position
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(lerpX - cameraPos.x, lerpY - cameraPos.y, lerpZ - cameraPos.z);
        RenderType renderType = magicCircleData.renderType;

        magicCircleData.executeInitTransforms(caster, newPartialTick);
        magicCircleData.executeFinalTransforms(caster, newPartialTick);
        magicCircleData.executePermanentRenderTransforms(poseStack, caster, newPartialTick);
        magicCircleData.executePermanentDataTransforms(caster, newPartialTick);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        float[] color = magicCircleData.getColor();
        RendererUtils.drawQuad(poseStack, vertexConsumer, color[0], color[1], color[2], color[3], magicCircleData.light, magicCircleData.overlay);

        magicCircleData.setLastFullTicks(passedGameTicksForCircle + newPartialTick);

        poseStack.popPose();
    }

    public static void clearCache() {
        CACHE.clear();
    }
}