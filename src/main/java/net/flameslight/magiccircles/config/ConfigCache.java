package net.flameslight.magiccircles.config;

import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.flameslight.magiccircles.datagen.Utils;
import net.flameslight.magiccircles.datagen.logger.ModLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.*;

public class ConfigCache {
    private static final String IRONS_NAMESPACE = "irons_spellbooks";

    // ── Cache fields ──────────────────────────────────────────────────────────
    private static volatile Map<String, Integer> cachedColorOverrides = null;
    private static volatile Map<String, Integer> cachedCircleOverrides = null;

    /**
     * Manually invalidates the cache.
     * Call this if you hot-reload the config at runtime from elsewhere.
     */
    public static void invalidateCache() {
        cachedColorOverrides = null;
        cachedCircleOverrides = null;
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
        // Priority 1 – spell-name match
        if (spellName != null) {
            Integer color = cachedColorOverrides.get(normalizeKey(spellName));
            if (color != null) return color;
        }

        // Priority 2 – school-name match
        if (school != null) {
            Integer color = cachedColorOverrides.get(schoolKey(school));
            if (color != null) return color;
        }

        return Utils.getColorFromSchool(school);
    }

    /**
     * Returns a configured sizeIndex override (0–4, i.e. the index into
     * {@code TEXTURES_PER_SIZE} / {@code TEXTURES_NEON_PER_SIZE}) for the given
     * spell/school pair, or time-type based as explained in the mod page if no override is configured.
     *
     */
    public static int resolveCircleTypeOverwrite(String spellId,
                                                 SchoolType school,
                                                 int castTime,
                                                 CastType castType) {
        Integer idx;

        // Priority 1 – spell-name match
        if (spellId != null) {
            idx = cachedCircleOverrides.get(normalizeKey(spellId));
            if (idx != null) return idx;
        }

        // Priority 2 – school-name match
        idx = cachedCircleOverrides.get(schoolKey(school));
        if (idx != null) return idx;

        return resolveCircleTypeByCastTimeAndType(castTime, castType);
    }

    private static int resolveCircleTypeByCastTimeAndType(int totalCastTime, CastType castType) {
        int circleType = Mth.clamp((totalCastTime / 20 + 1), 1, 5);

        if (castType != CastType.LONG)
            circleType = Math.min(3, circleType);

        return circleType;
    }

    // ── Lazy cache builders ───────────────────────────────────────────────────

    public static void ensureColorCacheBuilt() {
        if (cachedColorOverrides != null) return; // fast path – already built

        List<? extends String> originalEntries = ClientConfig.COLOR_OVERWRITES.get();
        List<String> correctedEntries = new ArrayList<>(originalEntries);
        boolean anyKeyCorrection = false;
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < originalEntries.size(); i++) {
            String entry = originalEntries.get(i);
            int commaIdx = entry.indexOf(',');
            if (commaIdx < 0) {
                ModLogger.warn("[magiccircles-config] colorOverwrites: entry \"{}\" has no ',' separator, skipping.", entry);
                continue;
            }

            String key = normalizeKey(entry.substring(0, commaIdx));
            String rawValue = entry.substring(commaIdx + 1).trim();

            // Auto-correct bare names (no namespace) by prepending the Iron's Spells namespace.
            // The corrected value is written back to the config file so the user sees the full RL.
            if (!isValidResourceLocation(key)) {
                String correctedKey = IRONS_NAMESPACE + ":" + key;
                String correctedEntry = correctedKey + "," + rawValue;
                ModLogger.info("[magiccircles-config] colorOverwrites: entry \"{}\" — key '{}' has no namespace; "
                                + "auto-correcting to \"{}\". The config file will be updated.",
                        entry, key, correctedEntry);
                correctedEntries.set(i, correctedEntry);
                key = correctedKey;
                anyKeyCorrection = true;
            }

            OptionalInt colorOpt = parseHexColor(rawValue, entry);
            if (colorOpt.isEmpty()) continue; // error already logged in parseHexColor

            map.put(key, colorOpt.getAsInt()); // later entries overwrite earlier → last wins
        }

        // Write corrected entries back so the config file on disk reflects the full RLs.
        // Only called when at least one key was missing a namespace.
        if (anyKeyCorrection)
            ClientConfig.COLOR_OVERWRITES.set(correctedEntries);

        cachedColorOverrides = Collections.unmodifiableMap(map);

/*        ModLogger.info("[magiccircles-config] Color overwrite cache built – {} school entries, {} spell entries.",
                schoolMap.size(), spellMap.size());*/
    }

    public static void ensureCircleCacheBuilt() {
        if (cachedCircleOverrides != null) return;

        List<? extends String> originalEntries = ClientConfig.CIRCLE_TYPE_OVERWRITES.get();
        List<String> correctedEntries = new ArrayList<>(originalEntries);
        boolean anyKeyCorrection = false;
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < originalEntries.size(); i++) {
            String entry = originalEntries.get(i);
            int commaIdx = entry.indexOf(',');
            if (commaIdx < 0) {
                ModLogger.warn("[magiccircles-config] circleTypeOverwrites: entry \"{}\" has no ',' separator, skipping.", entry);
                continue;
            }

            String key = normalizeKey(entry.substring(0, commaIdx));
            String rawValue = entry.substring(commaIdx + 1).trim();

            // Auto-correct bare names (no namespace) by prepending the Iron's Spells namespace.
            // The corrected value is written back to the config file so the user sees the full RL.
            if (!isValidResourceLocation(key)) {
                String correctedKey = IRONS_NAMESPACE + ":" + key;
                String correctedEntry = correctedKey + "," + rawValue;
                ModLogger.warn("[magiccircles-config] circleTypeOverwrites: entry \"{}\" — key '{}' has no namespace; "
                                + "auto-correcting to \"{}\". The config file will be updated.",
                        entry, key, correctedEntry);
                correctedEntries.set(i, correctedEntry);
                key = correctedKey;
                anyKeyCorrection = true;
            }

            int circleType;
            try {
                circleType = Integer.parseInt(rawValue);
            } catch (NumberFormatException e) {
                ModLogger.warn("[magiccircles-config] circleTypeOverwrites: entry \"{}\" has non-numeric value '{}', skipping.",
                        entry, rawValue);
                continue;
            }

            map.put(key, circleType);
        }

        // Write corrected entries back so the config file on disk reflects the full RLs.
        // Only called when at least one key was missing a namespace.
        if (anyKeyCorrection)
            ClientConfig.CIRCLE_TYPE_OVERWRITES.set(correctedEntries);

        cachedCircleOverrides = Collections.unmodifiableMap(map);

/*        ModLogger.info("[magiccircles-config] Circle-type overwrite cache built – {} school entries, {} spell entries.",
                schoolMap.size(), spellMap.size());*/
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Returns {@code true} when {@code normalizedKey} is a full ResourceLocation
     * (contains {@code ':'}), i.e. has both a namespace and a path.
     * Bare names like {@code "fire"} fail this check and will be auto-corrected.
     */
    private static boolean isValidResourceLocation(String normalizedKey) {
        return normalizedKey.contains(":");
    }

    /**
     * Strips surrounding whitespace and lower-cases a raw key/value string.
     */
    private static String normalizeKey(String raw) {
        return raw.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the normalised ResourceLocation string for a {@link SchoolType},
     * used as the cache lookup key for school-based overwrites
     * (e.g. {@code "irons_spellbooks:fire"}).
     *
     * <p>Uses {@code school.getRegistryName()}.  If your version of Iron's Spells
     * does not expose that method, try {@code school.getId()} as an alternative.</p>
     */
    private static String schoolKey(SchoolType school) {
        ResourceLocation rl = school.getId();
        if (rl != null) {
            return rl.toString().toLowerCase(Locale.ROOT);
        }
        // Should never be reached for a properly registered SchoolType.
        ModLogger.warn("[magiccircles-config] SchoolType '{}' has no registry name; school-based overwrites will not match for this school.",
                school.getDisplayName().getString());
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
