package net.flameslight.magiccircles.datagen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MagicCircleManager {
    public record CastInfo(AbstractSpell spell, int castTime) { }

    private static final Map<LivingEntity, ArrayList<MagicCircleData>> CIRCLES_BY_ENTITY = new HashMap<>();
    private static final int FADE_TICKS = 5;
    private static final int TICKS_DELAY_FOR_CHANGE = 2;
    private static final int MIN_TICKS_FOR_CIRCLE = 8;
    private static int ticksCounter = TICKS_DELAY_FOR_CHANGE;

    public static void handlePlayerLeaving(Player player) {
        CIRCLES_BY_ENTITY.remove(player);
    }

    public static void handleClientLeaving() {
        CIRCLES_BY_ENTITY.clear();
    }

    public static void handleEntityLeavingLevel(LivingEntity livingEntity) {
        ArrayList<MagicCircleData> magicCircles = CIRCLES_BY_ENTITY.get(livingEntity);

        if (magicCircles != null)
            magicCircles.forEach(magicCircle -> magicCircle.startFadeInElseOut(false, FADE_TICKS));
    }

    public static void handleOnClientTick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;

        if (level == null)
            return;

        updateMagicCirclesPerTick();

        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof LivingEntity livingEntity) {
                updateMagicCircleState(livingEntity);
            }
        }

        if (CIRCLES_BY_ENTITY.isEmpty())
            return;

        ticksCounter--;

        if (ticksCounter < 1) {
            ticksCounter = TICKS_DELAY_FOR_CHANGE;

            applyCleaningForMagicCirclesByTick();
        }
    }

    public static void renderMagicCircleForClient(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        CIRCLES_BY_ENTITY.forEach((livingEntity, magicCircleDataList) -> {
            for (MagicCircleData magicCircleData : magicCircleDataList) {

                // Only render if the circle is visible (not concealed)
                if (!magicCircleData.isConcealed()) {
                    renderMagicCircleForClient(livingEntity, magicCircleData, poseStack, bufferSource, partialTick);
                }
            }
        });
    }

    private static @Nullable CastInfo getCastingInfo(LivingEntity entity) {
        // 1. Handle Local Player
        if (entity instanceof Player player && player.equals(Minecraft.getInstance().player)) {
            if (!ClientMagicData.isCasting())
                return null;

            String spellId = ClientMagicData.getCastingSpellId();
            AbstractSpell spell = SpellRegistry.getSpell(spellId);
            return new CastInfo(spell, ClientMagicData.getCastDuration());
        }

        // 2. Handle Remote Entities
        SyncedSpellData syncedSpellData = ClientMagicData.getSyncedSpellData(entity);

        if (syncedSpellData == null || !syncedSpellData.isCasting())
            return null;

        String spellId = syncedSpellData.getCastingSpellId();

        if (spellId == null || spellId.isEmpty())
            return null;

        AbstractSpell spell = SpellRegistry.getSpell(spellId);

        // unite later with condition below
        if (spell == null || SpellRegistry.none() == spell)
            return null;

        int spellLevel = syncedSpellData.getCastingSpellLevel();
        CastType castType = spell.getCastType();
        int castDuration = spell.getCastTime(spellLevel);

        // Filter spells that are cast immediately
        if (castType == CastType.INSTANT || castDuration <= FADE_TICKS)
            return null;

        return new CastInfo(spell, castDuration);
    }

    private static void updateMagicCircleState(LivingEntity entity) {
        if (entity == null) return;

        CastInfo castInfo = getCastingInfo(entity);
        ArrayList<MagicCircleData> entityCircles = getEntityMagicCircles(entity);

        // Get the circle currently at the top of the stack (the most recent one)
        MagicCircleData currentActiveCircle = entityCircles.isEmpty() ? null : entityCircles.get(entityCircles.size() - 1);

        if (castInfo != null) {
            AbstractSpell spell = castInfo.spell();
            String usedSpellName = spell.getSpellId();

            boolean isMagicCircleRenderedForSpell = currentActiveCircle != null
                    && currentActiveCircle.castedSpellName.equals(usedSpellName);

            // Is the cast belong to currently displayed magic circle
            if (!isMagicCircleRenderedForSpell) {
                // Case B: New cast or properties changed.

                // 1. Fade out the old active circle (if one exists and isn't already fading out)
                if (currentActiveCircle != null && !currentActiveCircle.isFadingOut()) {
                    currentActiveCircle.startFadeInElseOut(false, FADE_TICKS);
                }

                // 2. Create the new circle and start fade in
                MagicCircleData newCircle = MagicCircleFactory.buildMagicCircleData(
                        usedSpellName,
                        entity,
                        castInfo
                );

                if (newCircle == null)
                    return;

                newCircle.startFadeInElseOut(true, FADE_TICKS);
                entityCircles.add(newCircle);
            }
        } else {
            // Case C: Casting finished/interrupted. Fade out ALL circles that are not concealed.
            for (MagicCircleData circle : entityCircles) {
                if (!circle.isConcealed() && !circle.isFadingOut()) {
                    circle.startFadeInElseOut(false, FADE_TICKS);
                }
            }
        }
    }

    private static void renderMagicCircleForClient(LivingEntity livingEntity, MagicCircleData magicCircleData, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        poseStack.pushPose();

        double lerpX = Mth.lerp(partialTick, livingEntity.xo, livingEntity.getX());
        double lerpY = Mth.lerp(partialTick, livingEntity.yo, livingEntity.getY());
        double lerpZ = Mth.lerp(partialTick, livingEntity.zo, livingEntity.getZ());

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(lerpX - cameraPos.x, lerpY - cameraPos.y, lerpZ - cameraPos.z);

        float gameTime = (float) livingEntity.level().getGameTime() + partialTick;
        float rotation = gameTime * 3.0f;
        float scale = magicCircleData.getSize();
        int color = magicCircleData.getColor();
        RenderType renderType = magicCircleData.renderType;

        poseStack.translate(0, magicCircleData.getYOffset(), 0);

        if (magicCircleData.isPlacedOnGroundElseViewFaced()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90));
        } else {
            // Apply Rotation to match entity view
            float yRot = Mth.lerp(partialTick, livingEntity.yRotO, livingEntity.getYRot());
            float xRot = Mth.lerp(partialTick, livingEntity.xRotO, livingEntity.getXRot());

            poseStack.mulPose(Axis.YP.rotationDegrees(-yRot)); // Yaw
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));  // Pitch

            // Move Forward (In the direction of the view)
            poseStack.translate(0, 0, magicCircleData.getZOffset());
            poseStack.translate(magicCircleData.getXOffset(), 0, 0);
        }

        // Apply Circle Spin
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

        poseStack.scale(scale, scale, 1);

        // Extract RGB
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        drawQuad(poseStack, vertexConsumer, r, g, b, magicCircleData.getOpacity());

        poseStack.popPose();

        /*if (bufferSource instanceof MultiBufferSource.BufferSource batchSource) {
            batchSource.endBatch(renderType);
        }*/
    }

    private static void drawQuad(PoseStack ps, VertexConsumer builder, float r, float g, float b, float alpha) {
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

    private static void updateMagicCirclesPerTick() {
        CIRCLES_BY_ENTITY.values().forEach(magicCircles -> magicCircles.forEach(MagicCircleData::updatePerTick));
    }

    private static void applyCleaningForMagicCirclesByTick() {
        ArrayList<LivingEntity> toRemove = new ArrayList<>();

        for (Map.Entry<LivingEntity, ArrayList<MagicCircleData>> entry : CIRCLES_BY_ENTITY.entrySet()) {
            ArrayList<MagicCircleData> magicCircles = entry.getValue();

            magicCircles.removeIf(MagicCircleData::isConcealed);

            if (magicCircles.isEmpty()) {
                LivingEntity uuid = entry.getKey();
                toRemove.add(uuid);
            }
        }

        for (LivingEntity remove : toRemove)
            CIRCLES_BY_ENTITY.remove(remove);
    }

    private static ArrayList<MagicCircleData> getEntityMagicCircles(LivingEntity e) {
        return CIRCLES_BY_ENTITY.computeIfAbsent(e, x -> new ArrayList<>());
    }
}
