package net.flameslight.magiccircles.datagen.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MagicCircleEntityRenderer extends EntityRenderer<MagicCircleEntity> {
    public MagicCircleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MagicCircleEntity entity,
                       float entityYaw,
                       float newPartialTick,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight) {
        // Look up the MagicCircleData from the manager using the owner ID
        MagicCirclesRender.renderMagicCircleForClient(entity, newPartialTick, poseStack, bufferSource);
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureLocation(MagicCircleEntity entity) {
        // Return dummy — actual texture set via RenderType
        return new ResourceLocation("minecraft", "textures/entity/ghast/ghast.png");
    }
}
