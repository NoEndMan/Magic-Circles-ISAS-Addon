package net.flameslight.magiccircles.datagen.types.magicCircle;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import net.flameslight.magiccircles.config.ClientConfig;
import net.flameslight.magiccircles.config.ConfigCache;
import net.flameslight.magiccircles.datagen.MagicCircleManager;
import net.flameslight.magiccircles.datagen.Utils;
import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.flameslight.magiccircles.datagen.types.transformations.TransformManager;
import net.flameslight.magiccircles.datagen.types.transformations.render.RenderAnimations;
import net.flameslight.magiccircles.datagen.types.transformations.data.DataTransformAnimations;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class MagicCircleFactory {
    @SuppressWarnings("removal")
    private static final ResourceLocation[] TEXTURES_PER_SIZE = new ResourceLocation[]{
            new ResourceLocation("magiccircles", "textures/circles/old/circle_1.png"),
            new ResourceLocation("magiccircles", "textures/circles/old/circle_2.png"),
            new ResourceLocation("magiccircles", "textures/circles/old/circle_3.png"),
            new ResourceLocation("magiccircles", "textures/circles/old/circle_4.png"),
            new ResourceLocation("magiccircles", "textures/circles/old/circle_5.png")
    };

    @SuppressWarnings("removal")
    private static final ResourceLocation[] TEXTURES_NEON_PER_SIZE = new ResourceLocation[]{
            new ResourceLocation("magiccircles", "textures/circles/old/circle_1.png"),
            new ResourceLocation("magiccircles", "textures/circles/neon/circle_2_neon.png"),
            new ResourceLocation("magiccircles", "textures/circles/neon/circle_3_neon.png"),
            new ResourceLocation("magiccircles", "textures/circles/neon/circle_4_neon.png"),
            new ResourceLocation("magiccircles", "textures/circles/old/circle_5.png")
    };

    private static final float[] SIZE_INDEX_BY_TIME = new float[]{1f, 1.9f, 2.2f, 4f, 6f};

    public static final int HAND_CIRCLE_FADE_IN_TICKS = 4;
    public static final int UNDER_PLAYER_FADE_IN_TICKS = 24;

    public static final int HAND_CIRCLE_FADE_OUT_TICKS = 4;
    public static final int UNDER_PLAYER_FADE_OUT_TICKS = 6;

//    private static final ResourceLocation STATIC_CIRCLE = new ResourceLocation("magiccircles", "textures/static_circle.png");

    public static MagicCircleData buildMagicCircleData(String spellName,
                                                       LivingEntity caster,
                                                       MagicCircleManager.CastInfo castInfo,
                                                       UUID circleEntityUUID) {
        int circleType = Mth.clamp(castInfo.circleType() - 1, 0, 4);
        AbstractSpell spell = castInfo.spell();
        ResourceLocation usedTexture;

        if(MagicCirclesRender.usedStyleInCache == CirclesStyle.NEON) {
            usedTexture = TEXTURES_NEON_PER_SIZE[circleType];
        } else {
            usedTexture = TEXTURES_PER_SIZE[circleType];
        }

        TransformManager animationManager = new TransformManager();
        float usedSize = SIZE_INDEX_BY_TIME[circleType];
        float xOffset, zOffset, yOffset;
        float rotationChangePerTick;
        int usedFadeInTicks, usedFadeOutTicks;
        int color = ConfigCache.resolveColorOverwrite(spellName, spell.getSchoolType());
        float[] colorRGB = Utils.extractRGBFromHexColor(color);
        float[] brighterColor = Utils.brighten(colorRGB[0], colorRGB[1], colorRGB[2], 0.2f);

        if (circleType > 2) {
            // --- Under Player ---
            if(caster instanceof LocalPlayer) {
                yOffset = ClientConfig.Y_OFFSET_FROM_PLAYER_BOTTOM.get().floatValue();
            } else {
                // infront of entity
                yOffset = ClientConfig.Y_OFFSET_FROM_ENTITY_BOTTOM.get().floatValue();
            }

            xOffset = 0;
            zOffset = 0;
            usedFadeInTicks = UNDER_PLAYER_FADE_IN_TICKS;
            usedFadeOutTicks = UNDER_PLAYER_FADE_OUT_TICKS;
            rotationChangePerTick = 5f;

            int targetSizeScaling;
            if(circleType == 4) {
                targetSizeScaling = 30;
            } else {
                targetSizeScaling = 16;
            }

            animationManager.addInitTransformation(DataTransformAnimations.getGradualOpacityChangeExecutable(1f, 10));
            animationManager.addInitTransformation(DataTransformAnimations.getGradualScalingExecutable(targetSizeScaling, UNDER_PLAYER_FADE_IN_TICKS));
            animationManager.addInitTransformation(DataTransformAnimations.getGradualRotationPerTick(2f, 6, UNDER_PLAYER_FADE_IN_TICKS));

            animationManager.addPermanentDataTransformation(DataTransformAnimations.getGroundFacingExecutable());
            animationManager.addPermanentDataTransformation(DataTransformAnimations.getConstantRotatedCircleExecutable());

            animationManager.addPermanentRenderTransformation(RenderAnimations.getSyncedPositionedExecutable(true));
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentFacingRotationExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentSizeScalingExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentRotationExecutable());

            animationManager.addFinalDataTransformation(DataTransformAnimations.getGradualOpacityChangeExecutable(0.35f, UNDER_PLAYER_FADE_OUT_TICKS));
        } else {
            if(caster instanceof LocalPlayer) {
                // infront of player
                zOffset = ClientConfig.Z_OFFSET_FROM_CROSS.get().floatValue();
                xOffset = ClientConfig.X_OFFSET_FROM_CROSS.get().floatValue();
                yOffset = ClientConfig.Y_OFFSET_FROM_CROSS.get().floatValue();
            } else {
                // infront of entity
                zOffset = ClientConfig.Z_OFFSET_FROM_VIEW.get().floatValue();
                xOffset = ClientConfig.X_OFFSET_FROM_VIEW.get().floatValue();
                yOffset = ClientConfig.Y_OFFSET_FROM_VIEW.get().floatValue();
            }

            usedFadeInTicks = HAND_CIRCLE_FADE_IN_TICKS;
            usedFadeOutTicks = HAND_CIRCLE_FADE_OUT_TICKS;
            rotationChangePerTick = 3f;

            animationManager.addInitTransformation(DataTransformAnimations.getGradualOpacityChangeExecutable(1f, HAND_CIRCLE_FADE_IN_TICKS));

            animationManager.addPermanentDataTransformation(DataTransformAnimations.getFacingCasterViewExecutable());
            animationManager.addPermanentDataTransformation(DataTransformAnimations.getConstantRotatedCircleExecutable());
            animationManager.addPermanentDataTransformation(DataTransformAnimations.getCasterBillboardPositionExecutable());

            animationManager.addPermanentRenderTransformation(RenderAnimations.getSyncedPositionedExecutable(false));
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentFacingRotationExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentSizeScalingExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentRotationExecutable());

            animationManager.addFinalDataTransformation(DataTransformAnimations.getGradualOpacityChangeExecutable(0.35f, HAND_CIRCLE_FADE_OUT_TICKS));
        }

        return new MagicCircleData(animationManager,
                caster,
                spellName,
                brighterColor,
                usedTexture,
                usedSize,
                rotationChangePerTick,
                xOffset,
                zOffset,
                yOffset,
                usedFadeInTicks,
                usedFadeOutTicks,
                circleEntityUUID);
    }
}
