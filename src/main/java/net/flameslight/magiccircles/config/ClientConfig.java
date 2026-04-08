package net.flameslight.magiccircles.config;

import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final String FILE_NAME = "magiccircles-client.toml";
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.EnumValue<CirclesStyle> CIRCLES_STYLE;

    // Hand positioned circle offsets
    public static final ForgeConfigSpec.DoubleValue X_OFFSET_FROM_CROSS;
    public static final ForgeConfigSpec.DoubleValue Y_OFFSET_FROM_CROSS;
    public static final ForgeConfigSpec.DoubleValue Z_OFFSET_FROM_CROSS;

    public static final ForgeConfigSpec.DoubleValue X_OFFSET_FROM_VIEW;
    public static final ForgeConfigSpec.DoubleValue Y_OFFSET_FROM_VIEW;
    public static final ForgeConfigSpec.DoubleValue Z_OFFSET_FROM_VIEW;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "OLD - old circle visuals\nNEON - new circle visuals, glowing and transparent" );
        CIRCLES_STYLE = builder
                .defineEnum("circlesStyle", CirclesStyle.NEON);

        builder.comment("\nParameters for hand positioned circles for client casting:");
        builder.comment("Positive is left direction from client cross" );
        X_OFFSET_FROM_CROSS = builder
                .defineInRange("xOffsetFromCross", -0.22, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is up direction from client cross" );
        Y_OFFSET_FROM_CROSS = builder
                .defineInRange("yOffsetFromCross", -0.2, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is forwards direction from client screen" );
        Z_OFFSET_FROM_CROSS = builder
                .defineInRange("zOffsetFromCross", 1.5, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("\nParameters for hand positioned circles for other entities, viewed in client side" );
        builder.comment("Positive is left direction when looking from entity view direction" );
        X_OFFSET_FROM_VIEW = builder
                .defineInRange("xOffsetFromView", -0.22, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is up direction when looking from entity view direction" );
        Y_OFFSET_FROM_VIEW = builder
                .defineInRange("yOffsetFromView", -0.2, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is forwards direction when looking from entity view direction" );
        Z_OFFSET_FROM_VIEW = builder
                .defineInRange("zOffsetFromView", 1.3, -Float.MAX_VALUE, Float.MAX_VALUE);

        SPEC = builder.build();
    }
}
