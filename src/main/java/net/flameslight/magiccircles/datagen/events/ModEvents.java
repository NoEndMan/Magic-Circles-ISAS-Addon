package net.flameslight.magiccircles.datagen.events;

import net.flameslight.magiccircles.MagicCircles;
import net.flameslight.magiccircles.datagen.entity.MagicCircleEntityRenderer;
import net.flameslight.magiccircles.datagen.registery.ModEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MagicCircles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEvents {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.MAGIC_CIRCLE.get(),
                MagicCircleEntityRenderer::new
        );
    }
}
