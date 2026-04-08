package net.flameslight.magiccircles.datagen.types.transformations.data;

/**
 * Changes the magic circle data over time (called per tick)
 */
public class DataTransformAnimations {
    public static TempDataTransformExecutable getGradualScalingExecutable(float targetScaling) {
        return (entitySnapshot, magicCircleData, newFullTicks, totalTicks) -> {
            float tickDifference = newFullTicks - magicCircleData.getLastFullTicks();
            int fullPassedTicks = (int) newFullTicks;
            float currentSize = magicCircleData.getCurrentSize();
            float leftScaling = targetScaling - currentSize;
            float neededScalingPerTick = totalTicks == fullPassedTicks ? 0 : leftScaling / (totalTicks - fullPassedTicks);
            float change = neededScalingPerTick * tickDifference;
            float newScale = change + currentSize;

            magicCircleData.setCurrentSize(newScale);
        };
    }

    public static TempDataTransformExecutable getGradualRotationPerTick(float targetRotationChangePerTick,
                                                                        int startingTick) {
        return (entitySnapshot, magicCircleData, newFullTicks, totalTicks) -> {
            if(newFullTicks > startingTick) {
                float tickDifference = newFullTicks - magicCircleData.getLastFullTicks();
                int fullPassedTicks = (int) newFullTicks;
                float currentRotationChange = magicCircleData.getRotationChange();
                float leftRotation = targetRotationChangePerTick - currentRotationChange;
                float neededRotationPerTick = totalTicks == fullPassedTicks ? 0 : leftRotation / (totalTicks - fullPassedTicks);
                float change = neededRotationPerTick * tickDifference;
                float newRotationChange = change + currentRotationChange;

                magicCircleData.setRotationChange(newRotationChange);
            }
        };
    }

    public static DataTransformExecutable getConstantRotatedCircleExecutable() {
        return (entitySnapshot, magicCircleData, newFullTicks) -> {
            float tickDifference = newFullTicks - magicCircleData.getLastFullTicks();
            float rotationChangeInThisTick =  tickDifference * magicCircleData.getRotationChange();
            float newRotation = (magicCircleData.getRotation() + rotationChangeInThisTick) % 360;

            magicCircleData.setRotation(newRotation);
        };
    }
}
