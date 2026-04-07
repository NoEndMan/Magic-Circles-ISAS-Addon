package net.flameslight.magiccircles.datagen.types.transformations.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleData;

public interface RenderTransformExecutable {
    void execute(PoseStack poseStack,
                 EntitySnapshot entitySnapshot,
                 MagicCircleData data,
                 float partialTick);
}
