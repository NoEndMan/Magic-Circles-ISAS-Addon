package net.flameslight.magiccircles.datagen.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.flameslight.magiccircles.MagicCircles;
import net.flameslight.magiccircles.datagen.MagicCircleRenderer;
import net.flameslight.magiccircles.datagen.logger.ModLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MagicCircles.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide && event.getEntity() instanceof Player player) {
            MagicCircleRenderer.handlePlayerLeaving(player);
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        MagicCircleRenderer.handleClientLeaving();
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        ModLogger.info("onRenderLevel called");

        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Render for all players
        for (Player player : mc.level.players()) {
            MagicCircleRenderer.renderMagicCircleForPlayer(player, poseStack, bufferSource, event.getPartialTick());
        }
    }
}
