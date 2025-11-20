package net.flameslight.magiccircles.datagen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.flameslight.magiccircles.datagen.logger.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicCircleRenderer {
    // Toggle this to FALSE when you want to stop seeing the debug circle
    private static final boolean DEBUG_FORCE_RENDER = false;

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            new ResourceLocation("magiccircles", "textures/circle_1.png"),
            new ResourceLocation("magiccircles", "textures/circle_2.png"),
            new ResourceLocation("magiccircles", "textures/circle_3.png"),
            new ResourceLocation("magiccircles", "textures/circle_4.png"),
            new ResourceLocation("magiccircles", "textures/circle_5.png")
    };

    private static final Map<UUID, CircleState> PLAYER_STATES = new HashMap<>();

    private static class CircleState {
        float opacity = 0.0f;
        int lastSizeIndex = 0;
        ResourceLocation lastSchool = null;

        void update(boolean isCasting) {
            float fadeSpeed = 0.1f;
            if (isCasting || (DEBUG_FORCE_RENDER && isLocalPlayer())) {
                opacity = Math.min(1.0f, opacity + fadeSpeed);
            } else {
                opacity = Math.max(0.0f, opacity - fadeSpeed);
            }
        }

        // Helper to identify if this state belongs to the person sitting at the computer
        boolean isLocalPlayer() {
            // This is a loose check, context dependent, handled better in the main loop
            return true;
        }
    }

    public static void handlePlayerLeaving(Player player) {
        PLAYER_STATES.remove(player.getUUID());
    }

    public static void handleClientLeaving() {
        PLAYER_STATES.clear();
    }

    public static void renderMagicCircleForPlayer(Player player, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        boolean isLocalPlayer = player.equals(Minecraft.getInstance().player);

        boolean isCasting;
        int totalCastTime = 0;
        AbstractSpell spell = null;

        // --- DATA FETCHING ---
        if (isLocalPlayer) {
            isCasting = ClientMagicData.isCasting();
            if (isCasting) {
                String spellId = ClientMagicData.getCastingSpellId();
                spell = SpellRegistry.getSpell(spellId);
                totalCastTime = ClientMagicData.getCastDuration();
            }
        } else {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            isCasting = magicData.isCasting();

            if (isCasting) {
                SpellData spellData = magicData.getCastingSpell();
                if (spellData != null) {
                    spell = spellData.getSpell();
                    totalCastTime = magicData.getCastDuration();
                } else {
                    isCasting = false;
                }
            }
        }

        // --- FIX 4: Instant Spell Logic ---
        // If cast time is 0 or less (Instant), do NOT show circle.
        // Previously we forced it to 20. Now we explicitly disable casting state.
        if (spell != null && totalCastTime <= 0 && !DEBUG_FORCE_RENDER) {
            isCasting = false;
        }

        // --- DEBUG OVERRIDE ---
        if (DEBUG_FORCE_RENDER && isLocalPlayer) {
            if (!isCasting) {
                isCasting = true;
                totalCastTime = 60;
                long time = System.currentTimeMillis() / 1000;
                if (time % 2 == 0) spell = SpellRegistry.getSpell("irons_spellbooks:fireball");
                else spell = SpellRegistry.getSpell("irons_spellbooks:ice_block");
            }
        }

        CircleState state = PLAYER_STATES.computeIfAbsent(player.getUUID(), k -> new CircleState());
        state.update(isCasting);

        if (state.opacity <= 0.01f) return;

        // --- DETERMINE VISUALS ---
        int sizeIndex;
        int color = 0xFFFFFF;;

        if (isCasting) {
            // Clamp logic for size
            // Safety: Ensure we don't divide by zero if something slipped through
            if (totalCastTime <= 0) totalCastTime = 1;

            sizeIndex = Mth.clamp((totalCastTime / 20), 0, 4);

            // --- FIX 1: Dynamic Color for Addons ---
            if (spell != null) {
                color = getColorFromSchool(spell.getSchoolType());
            }

            state.lastSizeIndex = sizeIndex;
        } else {
            sizeIndex = state.lastSizeIndex;
        }

        // --- RENDERING ---
        poseStack.pushPose();

        double lerpX = Mth.lerp(partialTick, player.xo, player.getX());
        double lerpY = Mth.lerp(partialTick, player.yo, player.getY());
        double lerpZ = Mth.lerp(partialTick, player.zo, player.getZ());

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(lerpX - cameraPos.x, lerpY - cameraPos.y, lerpZ - cameraPos.z);

        boolean isBig = sizeIndex >= 3;
        float scale = 1.5f + (sizeIndex * 0.5f);
        float gameTime = (float)player.level().getGameTime() + partialTick;
        float rotation = gameTime * 3.0f;

        if (isBig) {
            // --- FIX 3: Under Player (Bigger) ---
            poseStack.translate(0, 0.05, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
            // Was 2.0f, now 4.0f as requested ("twice as bigger")
            scale *= 4.0f;
        } else {
            // --- FIX 2: In Front (Closer & Smaller) ---
            float yRot = Mth.lerp(partialTick, player.yRotO, player.getYRot());
            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));

            // Moved closer: Z was 1.5, now 0.8
            // Adjusted height slightly to align with chest/hands better
            poseStack.translate(0, 1.2, 0.8);

            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

            // Made smaller
            scale *= 0.8f;
        }

        poseStack.scale(scale, scale, scale);

        // Extract RGB
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        RenderType renderType = RenderType.entityTranslucent(TEXTURES[sizeIndex]);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);

        drawDoubleSidedQuad(poseStack, vertexConsumer, r, g, b, state.opacity);

        poseStack.popPose();

        if (bufferSource instanceof MultiBufferSource.BufferSource batchSource) {
            batchSource.endBatch(renderType);
        }
    }

    private static void drawDoubleSidedQuad(PoseStack ps, VertexConsumer builder, float r, float g, float b, float alpha) {
        Matrix4f matrix = ps.last().pose();
        float size = 0.5f;
        int overlay = OverlayTexture.NO_OVERLAY;
        int light = 0xF000F0;

        // Front
        builder.vertex(matrix, -size, -size, 0).color(r, g, b, alpha).uv(0, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, -size, size, 0).color(r, g, b, alpha).uv(0, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, size, size, 0).color(r, g, b, alpha).uv(1, 1).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
        builder.vertex(matrix, size, -size, 0).color(r, g, b, alpha).uv(1, 0).overlayCoords(overlay).uv2(light).normal(0, 1, 0).endVertex();
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
