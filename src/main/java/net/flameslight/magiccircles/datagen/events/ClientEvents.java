package net.flameslight.magiccircles.datagen.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.flameslight.magiccircles.MagicCircles;
import net.flameslight.magiccircles.datagen.MagicCircleManager;
import net.flameslight.magiccircles.datagen.render.MagicCirclesRender;
import net.flameslight.magiccircles.oculus.OculusCompact;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MagicCircles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof Player player) {
            MagicCircleManager.handlePlayerLeaving(player);
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        MagicCircleManager.handleClientLeaving();
        MagicCirclesRender.clearCache();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        // Only render during the translucent stage — matches the render type
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        // Skip Oculus shadow/reflection passes — this is what causes the ghost duplicate
        if (OculusCompact.isRenderingShadowPass()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Use the PoseStack provided by the event
        PoseStack poseStack = event.getPoseStack();

        // Use the main buffer source
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render for all players
        MagicCircleManager.renderMagicCircleForClient(poseStack, bufferSource, event.getPartialTick());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MagicCircleManager.handleOnClientTick();
        }
    }

    @SubscribeEvent
    public static void onEntityLeavingLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();

        if(entity instanceof LivingEntity livingEntity)
            MagicCircleManager.handleEntityLeavingLevel(livingEntity);
    }
}
