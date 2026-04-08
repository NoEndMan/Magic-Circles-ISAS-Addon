package net.flameslight.magiccircles.datagen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.flameslight.magiccircles.datagen.logger.ModLogger;
import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleData;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class MagicCircleManager {
    public record CastInfo(AbstractSpell spell, int castTime, CastType castType) {
    }

    private static final Map<LivingEntity, MagicCircleData> CIRCLES_BY_ENTITY = new HashMap<>();
    private static final ArrayDeque<MagicCircleData> FADE_OUT_CIRCLES = new ArrayDeque<>();

    public static void handlePlayerLeaving(Player player) {
        CIRCLES_BY_ENTITY.remove(player);
    }

    public static void handleClientLeaving() {
        CIRCLES_BY_ENTITY.clear();
    }

    public static void handleEntityLeavingLevel(LivingEntity livingEntity) {
        MagicCircleData magicCircle = CIRCLES_BY_ENTITY.get(livingEntity);

        if (magicCircle != null)
            MagicCircleManager.startMagicCircleTermination(livingEntity, magicCircle);
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
    }

    public static void renderMagicCircleForClient(PoseStack poseStack, MultiBufferSource bufferSource, float partialTick) {
        CIRCLES_BY_ENTITY.forEach((caster, magicCircleData) -> {
            magicCircleData.caster.capture(caster);

            // Only render if the circle is visible (not concealed)
            if (!magicCircleData.isConcealed()) {
                int ticks = magicCircleData.getTicks();

                MagicCirclesRender.renderCircleForClient(magicCircleData, poseStack, bufferSource, ticks, partialTick);
            }
        });

        FADE_OUT_CIRCLES.forEach(magicCircleData -> {
            if (!magicCircleData.isConcealed()) {
                int ticks = magicCircleData.getTicks();

                MagicCirclesRender.renderCircleForClient(magicCircleData, poseStack, bufferSource, ticks, partialTick);
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

            if (isShortCastSpell(castDuration, castType))
                return null;

            return new CastInfo(spell, ClientMagicData.getCastDuration(), castType);
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

        return new CastInfo(spell, castDuration, castType);
    }

    private static boolean isShortCastSpell(int castDuration, CastType castType) {
        return castType == CastType.INSTANT || castDuration <= MagicCircleFactory.HAND_CIRCLE_FADE_IN_TICKS;
    }

    private static void updateMagicCircleState(LivingEntity entity) {
        if (entity == null) return;

        CastInfo castInfo = getCastingInfo(entity);

        // Get the circle currently at the top of the stack (the most recent one)
        MagicCircleData currentActiveCircle = CIRCLES_BY_ENTITY.get(entity);

        if (castInfo != null) {
            AbstractSpell spell = castInfo.spell();
            String usedSpellName = spell.getSpellId();

            boolean isMagicCircleRenderedForSpell = currentActiveCircle != null
                    && currentActiveCircle.castedSpellName.equals(usedSpellName);

            // Is the cast belong to currently displayed magic circle
            if (!isMagicCircleRenderedForSpell) {
                // New cast or properties changed.

                // 1. Fade out the old active circle (if one exists and isn't already fading out)
                if (currentActiveCircle != null) {
                    MagicCircleManager.startMagicCircleTermination(entity, currentActiveCircle);
                }

                // 2. Create the new circle and start fade in
                MagicCircleData newCircle = MagicCircleFactory.buildMagicCircleData(
                        usedSpellName,
                        entity,
                        castInfo
                );

                // newCircle should never be null, if so then this is a code bug
                assert newCircle != null;

                newCircle.startInitElseTermination(true);
                CIRCLES_BY_ENTITY.put(entity, newCircle);
            }
        } else {
            if(currentActiveCircle != null)
                MagicCircleManager.startMagicCircleTermination(entity, currentActiveCircle);
        }
    }

    private static void updateMagicCirclesPerTick() {
        CIRCLES_BY_ENTITY.values().forEach(MagicCircleData::updatePerTick);

        FADE_OUT_CIRCLES.removeIf((magicCircleData -> {
            magicCircleData.updatePerTick();

            return magicCircleData.isConcealed();
        }));
    }

    private static void startMagicCircleTermination(LivingEntity caster, MagicCircleData magicCircleData) {
        CIRCLES_BY_ENTITY.remove(caster);
        magicCircleData.startInitElseTermination(false);
        FADE_OUT_CIRCLES.add(magicCircleData);
    }
}
