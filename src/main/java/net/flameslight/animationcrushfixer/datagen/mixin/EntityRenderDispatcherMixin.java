package net.flameslight.animationcrushfixer.datagen.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.flameslight.animationcrushfixer.datagen.ModLogger;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Guard the entire per-entity render call. This catches anything thrown from
 * model parts / keyframe animations / render layers, etc., and prevents a client crash.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {


    private static final Set<String> LOGGED_RENDER = Collections.newSetFromMap(new ConcurrentHashMap<>());
    @Redirect(
            method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private <T extends Entity> void animfix$safeRender(EntityRenderer<T> renderer,
                                                       T entity,
                                                       float yaw,
                                                       float partialTicks,
                                                       PoseStack pose,
                                                       MultiBufferSource buffer,
                                                       int packedLight) {
        try {
            renderer.render(entity, yaw, partialTicks, pose, buffer, packedLight);
        } catch (Throwable t) {
            String key = renderer.getClass().getName();
            if (LOGGED_RENDER.add(key)) {
                ModLogger.warn(MessageFormat.format(
                        "Suppressed exception in setupAnim of {0}. Exception caught: {1}", key, t));
            }
// Swallow: skip drawing this one bad entity instance this frame.
        }
    }
}
