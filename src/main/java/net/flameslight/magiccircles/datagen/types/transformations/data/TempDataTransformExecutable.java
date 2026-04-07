package net.flameslight.magiccircles.datagen.types.transformations.data;

import net.flameslight.magiccircles.datagen.types.EntitySnapshot;
import net.flameslight.magiccircles.datagen.types.magicCircle.MagicCircleData;

@FunctionalInterface
public interface TempDataTransformExecutable {
    void execute(EntitySnapshot entitySnapshot,
                 MagicCircleData data,
                 int currentTicks,
                 int totalTicks);
}
