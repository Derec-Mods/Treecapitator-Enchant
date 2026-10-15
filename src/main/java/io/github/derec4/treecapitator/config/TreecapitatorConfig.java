package io.github.derec4.treecapitator.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreecapitatorConfig {

    public enum DetectionMode {
        SMART,
        BASIC
    }

    public static boolean enabled = true;
    public static boolean requireEnchantment = false;
    public static DetectionMode detectionMode = DetectionMode.SMART;
    public static int maxLogsPerChop = 500;
    public static boolean strictSpeciesMatch = false;
    public static boolean includeGiantMushrooms = true;
    public static boolean showAnimation = true;
    public static boolean consumeDurability = true;
    public static long cooldownMs = 0;
    public static final Set<String> worldAllowlist = new HashSet<>();
    public static final Set<String> worldDenylist = new HashSet<>();

    public static void load(FileConfiguration config) {
        enabled = config.getBoolean("enabled", true);
        requireEnchantment = config.getBoolean("require-enchantment", false);

        String detectStr = config.getString("detection-mode", "SMART").toUpperCase();
        try {
            detectionMode = DetectionMode.valueOf(detectStr);
        } catch (IllegalArgumentException e) {
            detectionMode = DetectionMode.SMART;
        }

        maxLogsPerChop = config.getInt("max-logs-per-chop", 500);
        strictSpeciesMatch = config.getBoolean("strict-species-match", false);
        includeGiantMushrooms = config.getBoolean("include-giant-mushrooms", true);
        showAnimation = config.getBoolean("show-animation", true);
        consumeDurability = config.getBoolean("consume-durability", true);
        cooldownMs = config.getLong("cooldown-ms", 0);

        worldAllowlist.clear();
        List<String> allow = config.getStringList("world-allowlist");
        worldAllowlist.addAll(allow);

        worldDenylist.clear();
        List<String> deny = config.getStringList("world-denylist");
        worldDenylist.addAll(deny);
    }

    public static boolean isWorldAllowed(String worldName) {
        if (!worldDenylist.isEmpty() && worldDenylist.contains(worldName)) {
            return false;
        }
        if (!worldAllowlist.isEmpty() && !worldAllowlist.contains(worldName)) {
            return false;
        }
        return true;
    }
}
