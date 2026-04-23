package net.flameslight.magiccircles.datagen.types.magicCircle;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.flameslight.magiccircles.config.ClientConfig;
import net.flameslight.magiccircles.datagen.MagicCircleManager;
import net.flameslight.magiccircles.datagen.Utils;
import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.flameslight.magiccircles.datagen.types.transformations.TransformManager;
import net.flameslight.magiccircles.datagen.types.transformations.render.RenderAnimations;
import net.flameslight.magiccircles.datagen.types.transformations.data.DataTransformAnimations;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

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

    private static final float[] SIZES_BY_INDEX = new float[]{1.2f, 2.2f, 2.7f, 4f, 18f};

    public static final int HAND_CIRCLE_FADE_IN_TICKS = 4;
    public static final int UNDER_PLAYER_FADE_IN_TICKS = 24;

    public static final int HAND_CIRCLE_FADE_OUT_TICKS = 4;
    public static final int UNDER_PLAYER_FADE_OUT_TICKS = 6;

//    private static final ResourceLocation STATIC_CIRCLE = new ResourceLocation("magiccircles", "textures/static_circle.png");

    public static MagicCircleData buildMagicCircleData(String spellName,
                                                       LivingEntity caster,
                                                       MagicCircleManager.CastInfo castInfo) {
        AbstractSpell spell = castInfo.spell();
        int totalCastTime = castInfo.castTime();
        CastType castType = castInfo.castType();

        // Calculate properties for the *new* circle
        int sizeIndex = Mth.clamp((totalCastTime / 20 - 1), 0, 4);

        if(sizeIndex < 0 || sizeIndex >= TEXTURES_PER_SIZE.length)
            return null;

        if(castType != CastType.LONG)
            sizeIndex = Math.min(2, sizeIndex);

        ResourceLocation usedTexture;
        int light, overlay;
        CirclesStyle usedStyle = ClientConfig.CIRCLES_STYLE.get();

        if(usedStyle == CirclesStyle.NEON) {
            usedTexture = TEXTURES_NEON_PER_SIZE[sizeIndex];
            light = LightTexture.FULL_BRIGHT;
            overlay = OverlayTexture.NO_OVERLAY;
        } else {
            usedTexture = TEXTURES_PER_SIZE[sizeIndex];
            light = LightTexture.FULL_BRIGHT;
            overlay = OverlayTexture.NO_OVERLAY;
        }

        RenderType usedRenderType = MagicCirclesRender.cachedCreateRenderType(usedTexture, usedStyle);
        TransformManager animationManager = new TransformManager();
        float usedSize = SIZES_BY_INDEX[sizeIndex];
        float xOffset, zOffset, yOffset;
        float rotationChangePerTick;
        int usedFadeInTicks, usedFadeOutTicks;
        int color = getColorFromSchool(spell.getSchoolType());
        float[] colorRGB = Utils.extractRGBFromHexColor(color);
        float[] brighterColor = Utils.brighten(colorRGB[0], colorRGB[1], colorRGB[2], 0.2f);

        animationManager.addPermanentRenderTransformation(RenderAnimations.getCasterBottomPositionRelativeWorldSpaceExecutable());

        if (sizeIndex > 2) {
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

            animationManager.addInitTransformation(DataTransformAnimations.getGradualOpacityChangeExecutable(1f, 6));
            animationManager.addInitTransformation(DataTransformAnimations.getGradualScalingExecutable(usedSize + 12f, UNDER_PLAYER_FADE_IN_TICKS));
            animationManager.addInitTransformation(DataTransformAnimations.getGradualRotationPerTick(2f, 6, UNDER_PLAYER_FADE_IN_TICKS));

            animationManager.addPermanentDataTransformation(DataTransformAnimations.getConstantRotatedCircleExecutable());

            animationManager.addPermanentRenderTransformation(RenderAnimations.getSyncedPositionedExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getGroundFacingExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentSizeScalingExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentRotationExecutable());

            animationManager.addFinalTransformation(DataTransformAnimations.getGradualOpacityChangeExecutable(0.35f, UNDER_PLAYER_FADE_OUT_TICKS));
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

            animationManager.addPermanentDataTransformation(DataTransformAnimations.getConstantRotatedCircleExecutable());

            animationManager.addPermanentRenderTransformation(RenderAnimations.getCasterBillboardBehaviorExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentSizeScalingExecutable());
            animationManager.addPermanentRenderTransformation(RenderAnimations.getCurrentRotationExecutable());

            animationManager.addFinalTransformation(DataTransformAnimations.getGradualOpacityChangeExecutable(0.35f, HAND_CIRCLE_FADE_OUT_TICKS));
        }

        return new MagicCircleData(animationManager,
                caster,
                spellName,
                brighterColor,
                usedRenderType,
                usedSize,
                rotationChangePerTick,
                xOffset,
                zOffset,
                yOffset,
                usedFadeInTicks,
                usedFadeOutTicks,
                light,
                overlay);
    }

    private static int getColorFromSchool(SchoolType school) {
        if (school == null) return 0xFFFFFF;

        // Get the Component (Name) -> Get Style -> Get TextColor
        Style style = school.getDisplayName().getStyle();
        TextColor textColor = style.getColor();

        return textColor != null
                ? textColor.getValue()
                : 0xFFFFFF;
    }
}
