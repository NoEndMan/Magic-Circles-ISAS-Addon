package net.flameslight.magiccircles.datagen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MagicCircleManager {
    public record CastInfo(AbstractSpell spell, int castTime) { }

    private static final Map<LivingEntity, ArrayList<MagicCircleData>> CIRCLES_BY_ENTITY = new HashMap<>();
    private static final int FADE_TICKS = 5;
    private static final int TICKS_DELAY_FOR_CHANGE = 2;
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
                    MagicCirclesRender.renderCircleForClient(livingEntity, magicCircleData, poseStack, bufferSource, partialTick);
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
            int spellLevel = ClientMagicData.getCastingSpellLevel();
            int castDuration = spell.getCastTime(spellLevel);
            CastType castType = spell.getCastType();

            if(isShortCastSpell(castDuration, castType))
                return null;

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

        //Debug
//        ModLogger.info("castType: ", castType);
//        ModLogger.info("castDuration: ", castDuration);

        // Filter spells that are cast immediately
        if (isShortCastSpell(castDuration, castType))
            return null;

        return new CastInfo(spell, castDuration);
    }

    private static boolean isShortCastSpell(int castDuration, CastType castType) {
        return castType == CastType.INSTANT || castDuration <= FADE_TICKS;
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
                // New cast or properties changed.

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
            // Casting finished/interrupted. Fade out ALL circles that are not concealed.
            for (MagicCircleData circle : entityCircles) {
                if (!circle.isConcealed() && !circle.isFadingOut()) {
                    circle.startFadeInElseOut(false, FADE_TICKS);
                }
            }
        }
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
