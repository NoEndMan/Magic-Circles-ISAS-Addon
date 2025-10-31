package net.flameslight.animationcrushfixer.datagen;

import net.flameslight.animationcrushfixer.AnimationCrushFixer;
import org.slf4j.Logger;

import java.text.MessageFormat;

public class ModLogger {
    private static final Logger LOGGER = AnimationCrushFixer.LOGGER;

    public static void info(String message) {
        LOGGER.info(buildMessageLog(message));
    }

    public static void warn(String message) {
        LOGGER.warn(buildMessageLog(message));
    }

    public static void error(String message) {
        LOGGER.error(buildMessageLog(message));
    }

    public static void error(String message, Throwable error) {
        LOGGER.error("{}: {}", buildMessageLog(message), error);
    }

    private static String buildMessageLog(String message) {
        return MessageFormat.format("{0}:{1}", AnimationCrushFixer.MOD_ID, message);
    }
}
