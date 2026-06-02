package net.flameslight.magiccircles.datagen.logger;

import net.flameslight.magiccircles.MagicCircles;
import org.slf4j.Logger;

import java.text.MessageFormat;

public class ModLogger {
    private static final Logger LOGGER = MagicCircles.LOGGER;

    public static void info(String message, Object ... args) {
        LOGGER.info(buildMessageLog(message), args);
    }

    public static void warn(String message, Object ... args) {
        LOGGER.warn(buildMessageLog(message), args);
    }

    public static void error(String message) {
        LOGGER.error(buildMessageLog(message));
    }

    public static void error(String message, Throwable error) {
        LOGGER.error("{}: {}", buildMessageLog(message), error);
    }

    private static String buildMessageLog(String message) {
        return MessageFormat.format("{0}: {1}", "MagicCircles logger", message);
    }
}
