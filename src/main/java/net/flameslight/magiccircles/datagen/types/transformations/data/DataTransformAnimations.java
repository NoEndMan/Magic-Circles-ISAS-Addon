package net.flameslight.magiccircles.datagen.types.transformations.data;

/**
 * Changes the magic circle data over time (called per tick)
 */
public class DataTransformAnimations {
    public static TempDataTransformExecutable getGradualScalingExecutable(float targetScaling) {
        return (entitySnapshot, magicCircleData, newFullTicks, totalTicks) -> {
            float progressPercentage = Math.min(newFullTicks / totalTicks, 1);
            float currentSize = magicCircleData.getCurrentSize();
            float scalingDifference = targetScaling - currentSize;
            float scaleChange = progressPercentage * scalingDifference;
            float newScale = scaleChange + currentSize;

            magicCircleData.setCurrentSize(newScale);
        };
    }

    public static TempDataTransformExecutable getGradualRotationPerTick(float targetRotationChangePerTick,
                                                                        int startingTick) {
        return (entitySnapshot, magicCircleData, newFullTicks, totalTicks) -> {
            float progressPercentage = Math.max(0, newFullTicks - startingTick) / (totalTicks - startingTick);
            float currentRotationChange = magicCircleData.getRotationChange();
            float changeDifference = targetRotationChangePerTick - currentRotationChange;
            float change = progressPercentage * changeDifference;
            float newRotationChange = change + currentRotationChange;

            magicCircleData.setRotationChange(newRotationChange);
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
