package net.flameslight.animationcrushfixer.datagen.mixin;

import net.flameslight.animationcrushfixer.datagen.ModLogger;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(value = ModelPart.class, priority = 2001)
public abstract class ModelPartMixin {
    @Shadow
    private Map<String, ModelPart> children;

    /**
     * @author YourName
     * @reason Safely handle stream concatenation to prevent crashes
     */
    @Overwrite
    public Stream<ModelPart> getAllParts() {
        ModelPart self = (ModelPart) (Object) this;

        try {
            List<ModelPart> all = new ArrayList<>();
            all.add(self);
            if (children != null && !children.isEmpty()) {
                for (ModelPart child : children.values()) {
                    try {
                        child.getAllParts().forEach(all::add);
                    } catch (Throwable e) {
                        ModLogger.warn("Suppressed bad child part in getAllParts" + e.getMessage());
                    }
                }
            }
            return all.stream();
        } catch (Throwable e) {
            ModLogger.warn("Suppressed exception in ModelPart.getAllParts(). Exception caught: "  + e.getMessage());
            return Stream.empty();
        }
    }
}
