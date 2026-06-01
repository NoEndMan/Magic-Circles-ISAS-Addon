package net.flameslight.magiccircles.datagen.events;

import net.flameslight.magiccircles.MagicCircles;
import net.flameslight.magiccircles.config.ConfigCache;
import net.flameslight.magiccircles.datagen.MagicCircleManager;
import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.flameslight.magiccircles.oculus.OculusCompact;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MagicCircles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof Player player) {
            MagicCircleManager.handlePlayerLeaving(player);
        }
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        ConfigCache.invalidateCache();
        ConfigCache.ensureCircleCacheBuilt();
        ConfigCache.ensureColorCacheBuilt();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        MagicCircleManager.handleClientLeaving();
        MagicCirclesRender.clearCache();
        ConfigCache.invalidateCache();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MagicCirclesRender.handleOnClientTick();
            MagicCircleManager.handleOnClientTick();
            OculusCompact.handleOnRenderUpdate();
        }
    }

    @SubscribeEvent
    public static void onEntityLeavingLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof LivingEntity livingEntity)
            MagicCircleManager.handleEntityLeavingLevel(livingEntity);
    }
}
