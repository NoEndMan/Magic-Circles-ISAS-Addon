package net.flameslight.animationcrushfixer.datagen.mixin;

import net.flameslight.animationcrushfixer.datagen.ModLogger;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Wrap the call to EntityModel#setupAnim inside LivingEntityRenderer#render.
 * If a mod's model throws here, we log once and keep the last safe pose.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow protected M model;

    private static final Set<String> LOGGED_SETUP = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"
            )
    )
    private void animfix$wrapSetupAnim(EntityModel<Entity> instance,
                                       Entity entity,
                                       float limbSwing,
                                       float limbSwingAmount,
                                       float ageInTicks,
                                       float netHeadYaw,
                                       float headPitch) {
        try {
            instance.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        } catch (Throwable t) {
            String key = instance.getClass().getName();
            if (LOGGED_SETUP.add(key)) {
                ModLogger.warn(MessageFormat.format(
                        "Suppressed exception in setupAnim of {0}. Exception caught: {1}", key, t));
            }
            // Swallow: model remains in its last valid pose.
        }
    }
}
