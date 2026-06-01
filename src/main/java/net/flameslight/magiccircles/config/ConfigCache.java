package net.flameslight.magiccircles.config;

import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.flameslight.magiccircles.datagen.MagicCircleManager;
import net.flameslight.magiccircles.datagen.Utils;
import net.flameslight.magiccircles.datagen.logger.ModLogger;
import net.minecraft.util.Mth;

import java.util.*;

public class ConfigCache {
    // ── Cache fields ──────────────────────────────────────────────────────────

    private static volatile Map<String, Integer> cachedSchoolColorOverrides = null;
    private static volatile Map<String, Integer> cachedSpellColorOverrides = null;

    private static volatile Map<String, Integer> cachedSchoolCircleOverrides = null;
    private static volatile Map<String, Integer> cachedSpellCircleOverrides = null;

    /**
     * Manually invalidates the cache.
     * Call this if you hot-reload the config at runtime from elsewhere.
     */
    public static void invalidateCache() {
        cachedSchoolColorOverrides = null;
        cachedSpellColorOverrides = null;
        cachedSchoolCircleOverrides = null;
        cachedSpellCircleOverrides = null;
    }

    // ── Public resolution API ─────────────────────────────────────────────────

    /**
     * Returns a configured color override (packed RGB int, same format as
     * {@code Utils.extractRGBFromHexColor}) for the given spell/school pair,
     * or the magic school color if no override is configured.
     *
     * @param spellName the full spell ResourceLocation string
     *                  (e.g. {@code "irons_spellbooks:fireball"})
     * @param school    the {@link SchoolType} of the spell
     */
    public static int resolveColorOverwrite(String spellName, SchoolType school) {
        ensureColorCacheBuilt();

        // Priority 1 – spell-name match
        if (spellName != null) {
            Integer color = cachedSpellColorOverrides.get(normalizeKey(spellName));
            if (color != null) return color;
        }

        // Priority 2 – school-name match
        if (school != null) {
            Integer color = cachedSchoolColorOverrides.get(schoolKey(school));
            if (color != null) return color;
        }

        return Utils.getColorFromSchool(school);
    }

    /**
     * Returns a configured sizeIndex override (0–4, i.e. the index into
     * {@code TEXTURES_PER_SIZE} / {@code TEXTURES_NEON_PER_SIZE}) for the given
     * spell/school pair, or time-type based as explained in the mod page if no override is configured.
     *
     * @param spellName the full spell ResourceLocation string
     * @param castInfo  the {@link MagicCircleManager.CastInfo} of the spell
     */
    public static int resolveCircleTypeOverwrite(String spellName, MagicCircleManager.CastInfo castInfo) {
        Integer idx;

        // Priority 1 – spell-name match
        if (spellName != null) {
            idx = cachedSpellCircleOverrides.get(normalizeKey(spellName));
            if (idx != null) return idx;
        }

        // Priority 2 – school-name match
        SchoolType school = castInfo.spell().getSchoolType();
        idx = cachedSchoolCircleOverrides.get(schoolKey(school));
        if (idx != null) return idx;

        return resolveCircleTypeByCastTimeAndType(castInfo.castTime(), castInfo.castType());
    }

    private static int resolveCircleTypeByCastTimeAndType(int totalCastTime, CastType castType) {
        int circleType = Mth.clamp((totalCastTime / 20 - 1), 0, 4);

        if (castType != CastType.LONG)
            circleType = Math.min(2, circleType);

        return circleType;
    }

    // ── Lazy cache builders ───────────────────────────────────────────────────

    private static void ensureColorCacheBuilt() {
        if (cachedSchoolColorOverrides != null) return; // fast path – already built

        Map<String, Integer> schoolMap = new HashMap<>();
        Map<String, Integer> spellMap = new HashMap<>();

        List<? extends String> entries = ClientConfig.COLOR_OVERWRITES.get();
        for (String entry : entries) {
            int commaIdx = entry.indexOf(',');
            if (commaIdx < 0) {
                // Passes String validator but has no ',' — warn and skip
                ModLogger.warn("[magiccircles-config] colorOverwrites: entry \"{}\" has no ',' separator, skipping.", entry);
                continue;
            }

            String key = normalizeKey(entry.substring(0, commaIdx));
            String rawValue = entry.substring(commaIdx + 1).trim();

            OptionalInt colorOpt = parseHexColor(rawValue, entry);
            if (colorOpt.isEmpty()) continue; // error already logged in parseHexColor

            // Later entries overwrite earlier ones for the same key → "last wins" behaviour
            if (isSpellKey(key)) spellMap.put(key, colorOpt.getAsInt());
            else schoolMap.put(key, colorOpt.getAsInt());
        }

        // Publish both maps atomically relative to each other.
        // Another thread may see null → build redundantly, which is harmless because
        // both builds read the same config and produce identical results.
        cachedSpellColorOverrides = Collections.unmodifiableMap(spellMap);
        cachedSchoolColorOverrides = Collections.unmodifiableMap(schoolMap);

/*        ModLogger.info("[magiccircles-config] Color overwrite cache built – {} school entries, {} spell entries.",
                schoolMap.size(), spellMap.size());*/
    }

    public static void ensureCircleCacheBuilt() {
        if (cachedSchoolCircleOverrides != null) return;

        Map<String, Integer> schoolMap = new HashMap<>();
        Map<String, Integer> spellMap = new HashMap<>();

        List<? extends String> entries = ClientConfig.CIRCLE_TYPE_OVERWRITES.get();
        for (String entry : entries) {
            int commaIdx = entry.indexOf(',');
            if (commaIdx < 0) {
                ModLogger.warn("[magiccircles-config] circleTypeOverwrites: entry \"{}\" has no ',' separator, skipping.", entry);
                continue;
            }

            String key = normalizeKey(entry.substring(0, commaIdx));
            String rawValue = entry.substring(commaIdx + 1).trim();

            int circleType;
            try {
                circleType = Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                ModLogger.warn("[magiccircles-config] circleTypeOverwrites: entry \"{}\" has non-numeric value '{}', skipping.",
                        entry, rawValue);
                continue;
            }

            if (circleType < 1 || circleType > 5) {
                ModLogger.warn("[magiccircles-config] circleTypeOverwrites: circle type {} in entry \"{}\" is outside [1-5]; clamping.",
                        circleType, entry);
                circleType = Mth.clamp(circleType, 1, 5);
            }

            circleType = circleType - 1; // user-visible 1-5  →  internal 0-4

            if (isSpellKey(key)) spellMap.put(key, circleType);
            else schoolMap.put(key, circleType);
        }

        cachedSpellCircleOverrides = Collections.unmodifiableMap(spellMap);
        cachedSchoolCircleOverrides = Collections.unmodifiableMap(schoolMap);

/*        ModLogger.info("[magiccircles-config] Circle-type overwrite cache built – {} school entries, {} spell entries.",
                schoolMap.size(), spellMap.size());*/
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * A key is treated as a spell ResourceLocation when it contains {@code ':'}
     * (e.g. {@code "irons_spellbooks:fireball"}).  A key without {@code ':'}
     * (e.g. {@code "fire"}) is treated as a school name.
     */
    private static boolean isSpellKey(String normalizedKey) {
        return normalizedKey.contains(":");
    }

    /**
     * Strips surrounding whitespace and lower-cases a raw key/value string.
     */
    private static String normalizeKey(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the cache key for a {@link SchoolType}.
     *
     * <p>Uses the school's English display name in lower-case (e.g. {@code "fire"},
     * {@code "ice"}, {@code "lightning"}).  Config entries must match the
     * <em>English</em> display name regardless of the active game language.</p>
     *
     * <p><strong>Iron's Spells tip:</strong> if {@code SchoolType} exposes a
     * locale-independent ID accessor — e.g. {@code school.getId().getPath()} or
     * {@code school.getRegistryName().getPath()} — prefer that over
     * {@code getDisplayName().getString()} to avoid any translation dependency.</p>
     */
    private static String schoolKey(SchoolType school) {
        return school.getDisplayName().getString().trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses a raw hex-color string into a packed RGB int.
     *
     * <p>Accepted formats: {@code FF4400}, {@code #FF4400}, {@code 0xFF4400}.
     * Must be exactly 6 hex digits after stripping any prefix.</p>
     *
     * @param rawValue  the value portion of the config entry (after the {@code ','})
     * @param fullEntry the complete entry string, used only in error messages
     * @return the packed RGB color, or {@link OptionalInt#empty()} on failure
     * (an ERROR is already logged before returning empty)
     */
    private static OptionalInt parseHexColor(String rawValue, String fullEntry) {
        String hex = rawValue;

        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        } else if (hex.toLowerCase(Locale.ROOT).startsWith("0x")) {
            hex = hex.substring(2);
        }

        if (hex.length() != 6) {
            ModLogger.warn(
                    "[magiccircles-config] colorOverwrites: entry \"{}\" — '{}' is not a valid 6-digit hex color " +
                            "(expected RRGGBB, #RRGGBB, or 0xRRGGBB). Entry will be ignored.",
                    fullEntry, rawValue);
            return OptionalInt.empty();
        }

        try {
            return OptionalInt.of(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            ModLogger.warn(
                    "[magiccircles-config] colorOverwrites: entry \"{}\" — '{}' contains invalid hex characters. " +
                            "Entry will be ignored.",
                    fullEntry, rawValue);
            return OptionalInt.empty();
        }
    }
}
