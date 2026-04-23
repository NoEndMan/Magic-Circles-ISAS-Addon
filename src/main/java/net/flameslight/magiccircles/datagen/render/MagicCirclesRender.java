package net.flameslight.magiccircles.datagen.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

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
        if (!CACHE.isEmpty() && usedStyleInCache != usedStyle) {
            CACHE.clear();
        }

        if (CACHE.isEmpty()) {
            usedStyleInCache = usedStyle;
        }

        return CACHE.computeIfAbsent(texture, tex -> {
            if (usedStyle == CirclesStyle.NEON) {
                // use with shaders, getting color tint in vanilla
                /*CompositeState state = CompositeState.builder()
                        // glowing transparent shader
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)

                        // texture, blur (to allow blur) and mipmap (to allow texture scaling down)
                        .setTextureState(new TextureStateShard(tex, false, false))

                        *//*
                            transparency state of circle.
                            No opacity since vanilla opacity is buggy with clouds, water and water mask

                            Additive cause color tint by background which is not desired.
                         *//*
                        .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)

                        // render both sides
                        .setCullState(RenderStateShard.NO_CULL)

                        // Must be less than or equal to existing depth
                        .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)

                        *//*
                            Write to depth, must for being occluded correctly by entities, and rendered
                            correctly with sky and water behind
                         *//*
                        .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)

                        // ignore red tint and shite flash. Ignore dynamic color changes
                        .setOverlayState(RenderStateShard.NO_OVERLAY)

                        // Circle color doesn't affected by world light level
                        .setLightmapState(RenderStateShard.NO_LIGHTMAP)

                        *//*
                            Which gbuffer render is written into.
                            Changing this most likely would require to change render timing in ClientEvents
                         *//*
//                        .setOutputState(RenderStateShard.MAIN_TARGET)

                        // ignore glowing outline shader
                        .createCompositeState(false);

                *//*
                    affectsCrumbling - if "block breaking cracks" could appear on circle surface
                    sortOnUpload - to sort the vertices by distance from the camera before sending them to the GPU
                 *//*
                return RenderType.create(
                        "magic_circle_shader",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        256,
                        false, // affectsCrumbling
                        false, // sortOnUpload
                        state
                );*/

                // use for vanilla
                CompositeState state = CompositeState.builder()
                        // glowing transparent shader
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_SOLID_SHADER)

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

                        // Circle color doesn't affected by world light level
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
                        "magic_circle_shader_vanilla",
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        256,
                        false, // affectsCrumbling
                        false, // sortOnUpload
                        state
                );
            } else
                return RenderType.entityTranslucent(tex);
        });
    }

    public static void renderCircleForClient(MagicCircleData magicCircleData,
                                             PoseStack poseStack,
                                             MultiBufferSource.BufferSource bufferSource,
                                             int passedGameTicksForCircle,
                                             float newPartialTick) {
        poseStack.pushPose();
        EntitySnapshot caster = magicCircleData.caster;
        RenderType renderType = magicCircleData.renderType;

        magicCircleData.executeInitTransforms(caster, newPartialTick);
        magicCircleData.executeFinalTransforms(caster, newPartialTick);
        magicCircleData.executePermanentRenderTransforms(poseStack, caster, newPartialTick);
        magicCircleData.executePermanentDataTransforms(caster, newPartialTick);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        float[] color = magicCircleData.getColor(true);
        Vector3f usedNormal = RendererUtils.getNormalForAlwaysGlowing(magicCircleData, caster);
        RendererUtils.drawQuad(poseStack,
                vertexConsumer,
                color[0],
                color[1],
                color[2],
                color[3],
                magicCircleData.light,
                magicCircleData.overlay,
                usedNormal);

        magicCircleData.setLastFullTicks(passedGameTicksForCircle + newPartialTick);

        poseStack.popPose();

        // End batch to ensure it renders before the next stage
        bufferSource.endBatch(renderType);
    }

    public static void clearCache() {
        CACHE.clear();
    }
}