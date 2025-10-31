package net.flameslight.animationcrushfixer.datagen.mixin;

import net.minecraft.client.model.HierarchicalModel;
import org.joml.Vector3f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HierarchicalModel.class)
public abstract class HierarchicalModelMixin {
    @Redirect(
            method = "animate(Lnet/minecraft/world/entity/AnimationState;Lnet/minecraft/client/animation/AnimationDefinition;FF)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/minecraft/client/model/HierarchicalModel;ANIMATION_VECTOR_CACHE:Lorg/joml/Vector3f;"
            )
    )
    private Vector3f redirectAnimationVectorCache() {
        // return a safe Vector3f instead
        return new Vector3f();
    }
}
