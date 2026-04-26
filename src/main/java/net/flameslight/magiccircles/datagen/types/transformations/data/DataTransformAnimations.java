package net.flameslight.magiccircles.datagen.types.transformations.data;

/**
 * Changes the magic circle data over time (called per tick)
 */
public class DataTransformAnimations {
    public static DataTransformExecutable getGradualScalingExecutable(float targetScaling, int totalTicks) {
        return (entitySnapshot, magicCircleData, tickDifference, passedTransformFullTicks) -> {
            int transformPassedTicks = (int) passedTransformFullTicks;
            if(transformPassedTicks <= totalTicks) {
                float currentSize = magicCircleData.getCurrentSize();
                float leftScaling = targetScaling - currentSize;
                float neededScalingPerTick = totalTicks == transformPassedTicks ? 0 : leftScaling / (totalTicks - transformPassedTicks);
                float change = neededScalingPerTick * tickDifference;
                float newScale = change + currentSize;

                magicCircleData.setCurrentSize(newScale);
            }
        };
    }

    public static DataTransformExecutable getGradualOpacityChangeExecutable(float targetOpacity, int totalTicks) {
        return (entitySnapshot, magicCircleData, tickDifference, passedTransformFullTicks) -> {
            int transformPassedTicks = (int) passedTransformFullTicks;
            if(transformPassedTicks <= totalTicks) {
                float currentOpacity = magicCircleData.getOpacity();
                float leftOpacity = targetOpacity - currentOpacity;

                float newOpacity;
                if(totalTicks == transformPassedTicks) {
                    newOpacity = targetOpacity;
                } else {
                    float neededOpacityChangePerTick = leftOpacity / (totalTicks - transformPassedTicks);
                    float change = neededOpacityChangePerTick * tickDifference;
                    newOpacity = change + currentOpacity;
                }

                magicCircleData.setOpacity(newOpacity);
            }
        };
    }

    public static DataTransformExecutable getGradualRotationPerTick(float targetRotationChangePerTick,
                                                                        int startingTick,
                                                                        int totalTicks) {
        return (entitySnapshot, magicCircleData, tickDifference, passedTransformFullTicks) -> {
            int transformPassedTicks = (int) passedTransformFullTicks;
            if(transformPassedTicks <= totalTicks && transformPassedTicks > startingTick) {
                float currentRotationChange = magicCircleData.getRotationChange();
                float leftRotation = targetRotationChangePerTick - currentRotationChange;
                float neededRotationPerTick = totalTicks == transformPassedTicks ? 0 : leftRotation / (totalTicks - transformPassedTicks);
                float change = neededRotationPerTick * tickDifference;
                float newRotationChange = change + currentRotationChange;

                magicCircleData.setRotationChange(newRotationChange);
            }
        };
    }

    public static DataTransformExecutable getConstantRotatedCircleExecutable() {
        return (entitySnapshot, magicCircleData, tickDifference, passedTransformFullTicks) -> {
            float rotationChangeInThisTick =  tickDifference * magicCircleData.getRotationChange();
            float newRotation = (magicCircleData.getRotation() + rotationChangeInThisTick) % 360;

            magicCircleData.setRotation(newRotation);
        };
    }

    public static DataTransformExecutable getFacingCasterViewExecutable() {
        return (entitySnapshot, magicCircleData, tickDifference, passedTransformFullTicks) -> {
            magicCircleData.setYRotation(-entitySnapshot.yRot);
            magicCircleData.setXRotation(entitySnapshot.xRot);
        };
    }

    public static DataTransformExecutable getGroundFacingExecutable() {
        return (entitySnapshot, magicCircleData, tickDifference, passedTransformFullTicks) ->
            magicCircleData.setXRotation(90);
    }
}
