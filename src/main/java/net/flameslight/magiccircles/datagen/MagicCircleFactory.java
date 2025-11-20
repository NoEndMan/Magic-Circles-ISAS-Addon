package net.flameslight.magiccircles.datagen;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class MagicCircleFactory {
    private static final ResourceLocation[] TEXTURES_PER_SIZE = new ResourceLocation[]{
            new ResourceLocation("magiccircles", "textures/circle_1.png"),
            new ResourceLocation("magiccircles", "textures/circle_2.png"),
            new ResourceLocation("magiccircles", "textures/circle_3.png"),
            new ResourceLocation("magiccircles", "textures/circle_4.png"),
            new ResourceLocation("magiccircles", "textures/circle_5.png")
    };

    private static final float[] SIZES_BY_INDEX = new float[]{0.8f, 1.6f, 1.9f, 20f, 30f};

    private static final ResourceLocation STATIC_CIRCLE = new ResourceLocation("magiccircles", "textures/static_circle.png");

    public static MagicCircleData buildMagicCircleData(String spellName,
                                                       LivingEntity caster,
                                                       MagicCircleManager.CastInfo castInfo) {
        AbstractSpell spell = castInfo.spell();
        int totalCastTime = castInfo.castTime();

        // Calculate properties for the *new* circle
        int sizeIndex = Mth.clamp((totalCastTime / 20 - 1), 0, 4);

        int color = getColorFromSchool(spell.getSchoolType());

        if(sizeIndex < 0 || sizeIndex >= TEXTURES_PER_SIZE.length)
            return null;

        RenderType usedTexture = RenderType.entityTranslucent(TEXTURES_PER_SIZE[sizeIndex]);
        float usedSize = SIZES_BY_INDEX[sizeIndex];
        float xOffset, zOffset, yOffset;
        boolean isPlacedOnGroundElseViewFaced;

        if (sizeIndex > 2) {
            // --- Under Player ---
            isPlacedOnGroundElseViewFaced = true;
            xOffset = 0;
            zOffset = 0;
            yOffset = 0.05f;
        } else {
            // infront of entity ---
            isPlacedOnGroundElseViewFaced = false;
            zOffset = 1.3f;
            xOffset = -0.22f;
            yOffset = caster.getEyeHeight() - 0.2f;
        }

        return new MagicCircleData(spellName, color, usedTexture, isPlacedOnGroundElseViewFaced, usedSize, xOffset, zOffset, yOffset);
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
