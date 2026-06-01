package net.flameslight.magiccircles.config;

import net.flameslight.magiccircles.datagen.types.CirclesStyle;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class ClientConfig {
    public static final String FILE_NAME = "magiccircles-client.toml";
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> CIRCLES_STYLE;

    // Hand positioned circle offsets
    public static final ForgeConfigSpec.DoubleValue X_OFFSET_FROM_CROSS;
    public static final ForgeConfigSpec.DoubleValue Y_OFFSET_FROM_CROSS;
    public static final ForgeConfigSpec.DoubleValue Z_OFFSET_FROM_CROSS;

    public static final ForgeConfigSpec.DoubleValue X_OFFSET_FROM_VIEW;
    public static final ForgeConfigSpec.DoubleValue Y_OFFSET_FROM_VIEW;
    public static final ForgeConfigSpec.DoubleValue Z_OFFSET_FROM_VIEW;

    // Under entity wheels
    public static final ForgeConfigSpec.DoubleValue Y_OFFSET_FROM_PLAYER_BOTTOM;
    public static final ForgeConfigSpec.DoubleValue Y_OFFSET_FROM_ENTITY_BOTTOM;

    // All circles config
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> COLOR_OVERWRITES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CIRCLE_TYPE_OVERWRITES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment(
                "OLD - old circle visuals\nNEON - new circle visuals, glowing and transparent" );
        CIRCLES_STYLE = builder
                .define("circlesStyle", CirclesStyle.NEON.name);

        builder.comment("\nParameters for hand positioned circles for client casting:");
        builder.comment("Positive is left direction from client cursor" );
        X_OFFSET_FROM_CROSS = builder
                .defineInRange("xOffsetFromCross", 0, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is up direction from client cursor" );
        Y_OFFSET_FROM_CROSS = builder
                .defineInRange("yOffsetFromCross", 0, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is forwards direction from client screen" );
        Z_OFFSET_FROM_CROSS = builder
                .defineInRange("zOffsetFromCross", 1.45, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("\nParameters for hand positioned circles for other entities casting" );
        builder.comment("Positive is left direction when looking from entity view direction" );
        X_OFFSET_FROM_VIEW = builder
                .defineInRange("xOffsetFromView", 0, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is up direction when looking from entity view direction" );
        Y_OFFSET_FROM_VIEW = builder
                .defineInRange("yOffsetFromView", 0, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("Positive is forwards direction when looking from entity view direction" );
        Z_OFFSET_FROM_VIEW = builder
                .defineInRange("zOffsetFromView", 1.3, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("\nParameters for under entity circles for client casting:");
        builder.comment("Positive is upwards");
        Y_OFFSET_FROM_PLAYER_BOTTOM = builder
                .defineInRange("yOffsetFromPlayerBottom", 0.01, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment("\nParameters for under entity circles for other entities casting:");
        builder.comment("Positive is upwards");
        Y_OFFSET_FROM_ENTITY_BOTTOM = builder
                .defineInRange("yOffsetFromEntityBottom", 0.01, -Float.MAX_VALUE, Float.MAX_VALUE);

        builder.comment(
                "",
                "Color-color overwrite list.",
                "Overrides the default school-based circle color for specific spells or magic schools.",
                "Cache when entering or leaving a world. Requires re-entering world for changes to take effect.",
                "",
                "FORMAT: Each entry is a string with the pattern  \"key,#RRGGBB\"  where:",
                "  key   = a magic-school resource location (e.g. \"irons_spellbooks:fire\") OR a spell name ResourceLocation",
                "          (e.g. \"irons_spellbooks:fireball\").",
                "          School names are matched against the school's English display name (case-insensitive),",
                "          e.g.  fire, ice, lightning, holy, blood, ender, nature, eldritch.",
                "  value = a 6-digit hexadecimal color.",
                "          The '#' prefix and '0x' prefix are both accepted.",
                "          Examples: FF4400  |  #FF4400  |  0xFF4400",
                "",
                "PRIORITY (highest to lowest):",
                "  1. Spell-name match  -- wins over any school rule for that spell.",
                "  2. School-name match -- applies to all spells of that school with no spell-specific override.",
                "  Within the same priority tier, the LAST matching entry in the list wins.",
                "",
                "VALID entry examples:",
                "  \"fire,#FF4400\"                      -> all fire-school spells use orange-red",
                "  \"irons_spellbooks:fireball,FF8800\"  -> fireball specifically uses a different orange (overrides the school rule above)",
                "  \"ice,0x00CCFF\"                      -> ice school uses light-blue (0x prefix is fine)",
                "",
                "INVALID color value examples (an ERROR is logged and the entry is skipped; the rest of the list is unaffected):",
                "  \"fire,red\"    -> 'red' is not a hex color",
                "  \"fire,ZZZZZZ\" -> contains invalid hex characters",
                "  \"fire,FF000\"  -> must be exactly 6 hex digits",
                "",
                "NOTE: If any list element is not a string, the ENTIRE list is rejected by the",
                "      config system and reverts to its default (empty list)."
        );
        COLOR_OVERWRITES = builder.defineList(
                "colorOverwrites",
                Collections.emptyList(),
                obj -> obj instanceof String   // non-String elements are rejected at config load; format is validated at runtime
        );

        builder.comment(
                "",
                "Circle-type overwrite list.",
                "Overrides the default cast-time-based circle type for specific spells or magic schools.",
                "Cache when entering or leaving a world. Requires re-entering world for changes to take effect.",
                "",
                "FORMAT: Each entry is a string with the pattern  \"key,N\"  where:",
                "  key = a magic-school resource location (e.g. \"irons_spellbooks:fire\") OR a spell name ResourceLocation",
                "        (e.g. \"irons_spellbooks:fireball\").",
                "        School names are matched against the school's English display name (case-insensitive),",
                "        e.g.  fire, ice, lightning, holy, blood, ender, nature, eldritch.",
                "  N   = a whole, integer number from 1 to 5 representing the circle type.",
                "        1 = smallest circle  ...  5 = largest circle.",
                "        Values outside [1-5] would not have an affect and 0 would stop the circle from being rendered.",
                "        Non-numeric values cause the entry to be skipped.",
                "",
                "PRIORITY (highest to lowest):",
                "  1. Spell-name match  -- wins over any school rule for that spell.",
                "  2. School-name match -- applies to all spells of that school with no spell-specific override.",
                "  Within the same priority tier, the LAST matching entry in the list wins.",
                "",
                "VALID entry examples:",
                "  \"fire,3\"                          -> all fire-school spells use circle type 3",
                "  \"irons_spellbooks:fireball,5\"     -> fireball always uses the largest circle",
                "  \"ice,1\"                           -> ice-school spells always use the smallest circle",
                "",
                "INVALID value examples:",
                "  \"fire,big\" -> non-numeric; entry is skipped with a WARNING",
                "  \"fire,6\"   -> out of range; clamped to 5 with a WARNING",
                "  \"fire,0\"   -> out of range; clamped to 1 with a WARNING",
                "",
                "NOTE: If any list element is not a string, the ENTIRE list is rejected by the",
                "      config system and reverts to its default (empty list)."
        );
        CIRCLE_TYPE_OVERWRITES = builder.defineList(
                "circleTypeOverwrites",
                Collections.emptyList(),
                obj -> obj instanceof String
        );

        SPEC = builder.build();
    }

    public static CirclesStyle getCircleStyle() {
        String styleName = CIRCLES_STYLE.get();

        if(styleName.equals(CirclesStyle.OLD.name))
            return CirclesStyle.OLD;
        else {
            if(!styleName.equals(CirclesStyle.NEON.name))
                CIRCLES_STYLE.set(CirclesStyle.NEON.name);

            return CirclesStyle.NEON;
        }
    }
}
