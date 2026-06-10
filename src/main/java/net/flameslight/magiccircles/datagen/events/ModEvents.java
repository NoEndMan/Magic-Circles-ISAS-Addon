package net.flameslight.magiccircles.datagen.events;

import net.flameslight.magiccircles.MagicCircles;
import net.flameslight.magiccircles.datagen.entity.MagicCircleEntityRenderer;
import net.flameslight.magiccircles.datagen.registery.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MagicCircles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Grab the object out of the registry while it is still valid and cache it
        ModEntities.CACHED_MAGIC_CIRCLE = ModEntities.MAGIC_CIRCLE.get();

        // Register the renderer using our cached version
        event.registerEntityRenderer(
                ModEntities.CACHED_MAGIC_CIRCLE,
                MagicCircleEntityRenderer::new
        );
    }
}
