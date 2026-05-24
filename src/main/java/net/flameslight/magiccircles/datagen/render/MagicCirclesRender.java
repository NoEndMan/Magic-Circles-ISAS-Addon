package net.flameslight.magiccircles.datagen.render;

import com.mojang.blaze3d.vertex.*;
import net.flameslight.magiccircles.config.ClientConfig;
import net.flameslight.magiccircles.datagen.MagicCircleManager;
import net.flameslight.magiccircles.datagen.Utils;
import net.flameslight.magiccircles.datagen.entity.MagicCircleEntity;
import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleData;
import net.flameslight.magiccircles.oculus.OculusCompact;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class MagicCirclesRender extends RenderType {
    public static CirclesStyle usedStyleInCache = CirclesStyle.NEON;

    private static final int UPDATE_CONFIG_CACHE_TICK = 10;

    private static int tickCounter = UPDATE_CONFIG_CACHE_TICK;
    private static final Map<ResourceLocation, RenderType> RENDER_CACHE = new HashMap<>();
    private static final Map<ResourceLocation, RenderType> WATER_MASK_BLOCKER_CACHE = new HashMap<>();

    // Required constructor (never used directly)
    private MagicCirclesRender(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                               boolean affectsCrumbling, boolean sortOnUpload,
                               Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static void handleOnClientTick() {
        tickCounter++;

        if(tickCounter < UPDATE_CONFIG_CACHE_TICK)
            return;

        tickCounter = 0;

        CirclesStyle usedStyle = ClientConfig.getCircleStyle();

        if (!RENDER_CACHE.isEmpty() && usedStyleInCache != usedStyle) {
            clearCache();
        }

        if (RENDER_CACHE.isEmpty()) {
            usedStyleInCache = usedStyle;
        }
    }

    public static RenderType cachedCreateRenderType(ResourceLocation texture) {
        return RENDER_CACHE.computeIfAbsent(texture, tex -> {
            if (OculusCompact.isShaderPackInUse()) {
                // use with shaders
                CompositeState state = CompositeState.builder()
                        // glowing transparent shader
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)

                        // texture, blur (to allow blur) and mipmap (to allow texture scaling down)
                        .setTextureState(new TextureStateShard(tex, false, false))

                        .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)

                        // render both sides
                        .setCullState(RenderStateShard.NO_CULL)

                        // Must be less than or equal to existing depth
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)

                        /*
                            Write to depth, must for being occluded correctly by entities, and rendered
                            correctly with sky and water behind
                         */
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)

                        // ignore red tint and shite flash. Ignore dynamic color changes
                        .setOverlayState(RenderStateShard.NO_OVERLAY)

                        // Circle color doesn't affected by world light level
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)

                        /*
                            Which gbuffer render is written into.
                            Changing this most likely would require to change render timing in ClientEvents
                         */
//                        .setOutputState(RenderStateShard.PARTICLES_TARGET)

                        // ignore glowing outline shader
                        .createCompositeState(false);

                /*
                    affectsCrumbling - if "block breaking cracks" could appear on circle surface
                    sortOnUpload - to sort the vertices by distance from the camera before sending them to the GPU
                 */
                return RenderType.create(
                        "magic_circle_neo_shader",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        256,
                        false, // affectsCrumbling
                        false, // sortOnUpload
                        state
                );
            } else {
                // use for vanilla
                CompositeState state = CompositeState.builder()
                        // need the cutout so no black background would appear
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_SHADER)

                        // texture, blur (to allow blur) and mipmap (to allow texture scaling down)
                        .setTextureState(new TextureStateShard(tex, false, false))

                        /*
                            transparency state of circle.
                            No opacity since vanilla opacity is buggy with clouds, water and water mask

                            Additive causes color tint by background which is not desired.
                         */
                        .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)

                        // render both sides
                        .setCullState(RenderStateShard.NO_CULL)

                        // Must be less than or equal to existing depth
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)

                        /*
                            Write to depth, must for being occluded correctly by entities, and rendered
                            correctly with sky and water behind
                         */
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)

                        // ignore red tint and shite flash. Ignore dynamic color changes
                        .setOverlayState(RenderStateShard.NO_OVERLAY)

                        // Circle color affected by world light level, used with fake normal for always glowing circle
                        .setLightmapState(RenderStateShard.LIGHTMAP)

                        /*
                        Which gbuffer render is written into.
                        Changing this most likely would require to change render timing in ClientEvents
                         */
//                        .setOutputState(RenderStateShard.MAIN_TARGET)

                        // ignore glowing outline shader
                        .createCompositeState(false);

                /*
                    affectsCrumbling - if "block breaking cracks" could appear on circle surface
                    sortOnUpload - to sort the vertices by distance from the camera before sending them to the GPU
                 */
                return RenderType.create(
                        "magic_circle_vanilla_shader",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        256,
                        false, // affectsCrumbling
                        false, // sortOnUpload
                        state
                );
            }
        });
    }

    public static void renderMagicCircleForClient(MagicCircleEntity entity,
                                                  float newPartialTick,
                                                  PoseStack poseStack,
                                                  MultiBufferSource bufferSource) {
        MagicCircleData magicCircleData = MagicCircleManager.CIRCLES_DATA_BY_ID.get(entity.getUUID());

        if (magicCircleData == null || magicCircleData.isConcealed()) return;

        LivingEntity casterEntity = entity.getCaster();
        EntitySnapshot entitySnapshot = magicCircleData.caster;
        boolean isUsingShaders = OculusCompact.isShaderPackInUse();
        RenderType circleRenderType = MagicCirclesRender.cachedCreateRenderType(magicCircleData.usedTexture);

        if (!magicCircleData.isFadingOut())
            entitySnapshot.capture(casterEntity);

        magicCircleData.executeInitTransforms(entitySnapshot, newPartialTick);
        magicCircleData.executeFinalDataTransforms(entitySnapshot, newPartialTick);
        magicCircleData.executePermanentDataTransforms(entitySnapshot, newPartialTick);

        poseStack.pushPose();
        EntitySnapshot casterSnapshot = magicCircleData.caster;

        magicCircleData.executeUntilFinalRenderTransforms(poseStack, casterSnapshot, newPartialTick);
        magicCircleData.executeFinalRenderTransforms(poseStack, casterSnapshot, newPartialTick);
        magicCircleData.executePermanentRenderTransforms(poseStack, casterSnapshot, newPartialTick);

        if (isUsingShaders) {
            RenderType waterMaskRenderType = MagicCirclesRender.cachedCreateDepthRenderType(magicCircleData.usedTexture);
            MagicCirclesRender.drawWaterMaskCircle(magicCircleData, waterMaskRenderType, poseStack, bufferSource);
        }

        MagicCirclesRender.drawCircle(magicCircleData, circleRenderType, poseStack, bufferSource, !isUsingShaders);

        poseStack.popPose();

        magicCircleData.setLastFullTicks(magicCircleData.getTicks() + newPartialTick);
    }

    public static void drawCircle(MagicCircleData magicCircleData,
                                  RenderType renderType,
                                  PoseStack poseStack,
                                  MultiBufferSource bufferSource,
                                  boolean toUseAlwaysGlowingNormal) {
        float[] color = magicCircleData.getColor(true);

        Vector3f usedNormal;
        if (toUseAlwaysGlowingNormal)
            usedNormal = RendererUtils.getNormalForAlwaysGlowing(magicCircleData);
        else {
            usedNormal = new Vector3f();
            poseStack.last().normal().transform(usedNormal);
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        RendererUtils.drawQuad(poseStack,
                vertexConsumer,
                color[0],
                color[1],
                color[2],
                color[3],
                usedNormal);
    }

    public static void drawWaterMaskCircle(MagicCircleData magicCircleData,
                                           RenderType renderType,
                                           PoseStack poseStack,
                                           MultiBufferSource bufferSource) {
        Vector3f usedNormal = new Vector3f();
        poseStack.last().normal().transform(usedNormal);
        float[] color = magicCircleData.getColor(true);
        color = Utils.getColorDesaturationByOpacity(color[0], color[1], color[2], 0.6f);

        VertexConsumer vc = bufferSource.getBuffer(renderType);
        RendererUtils.drawQuad(poseStack, vc, color[0], color[1], color[2], 1, usedNormal);
    }

    public static void clearCache() {
        RENDER_CACHE.clear();
        WATER_MASK_BLOCKER_CACHE.clear();
    }

    public static RenderType cachedCreateDepthRenderType(ResourceLocation texture) {
        return WATER_MASK_BLOCKER_CACHE.computeIfAbsent(texture, tex -> {
            CompositeState state = CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                    .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .setOverlayState(RenderStateShard.NO_OVERLAY)
                    .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                    .createCompositeState(false);

            return RenderType.create(
                    "magic_circle_neo_depth_shader",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false, // affectsCrumbling
                    false, // sortOnUpload
                    state
            );
        });
    }
}