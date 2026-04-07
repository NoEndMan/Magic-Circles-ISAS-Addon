package net.flameslight.magiccircles.datagen.types.transformations.data;

/**
 * Changes the magic circle data over time (called per tick)
 */
public class DataTransformAnimations {
    public static TempDataTransformExecutable getGradualScalingExecutable(float targetScaling) {
        return (entitySnapshot, magicCircleData, currentTicks, totalTicks) -> {
            float progressPercentage = (float) currentTicks / totalTicks;
            float currentSize = magicCircleData.getCurrentSize();
            float scalingDifference = targetScaling - currentSize;
            float scaleChange = progressPercentage * scalingDifference;
            float newScale = scaleChange + currentSize;

            magicCircleData.setCurrentSize(newScale);
        };
    }

    public static TempDataTransformExecutable getGradualRotationPerTick(float targetRotationChangePerTick,
                                                                        int startingTick) {
        return (entitySnapshot, magicCircleData, currentTicks, totalTicks) -> {
            float progressPercentage = (float) (Math.max(0, currentTicks - startingTick)) / (totalTicks - startingTick);
            float currentRotationChange = magicCircleData.getRotationChange();
            float changeDifference = targetRotationChangePerTick - currentRotationChange;
            float change = progressPercentage * changeDifference;
            float newRotationChange = change + currentRotationChange;

            magicCircleData.setRotationChange(newRotationChange);
        };
    }

    public static DataTransformExecutable getRotatedCircleExecutable() {
        return (entitySnapshot, magicCircleData, currentTicks) -> {
            float newRotation = (magicCircleData.getRotation() + magicCircleData.getRotationChange()) % 360;

            magicCircleData.setRotation(newRotation);
        };
    }
}
