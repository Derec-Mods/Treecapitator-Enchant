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

    private boolean enabled = true;
    private boolean requireEnchantment = false;
    private DetectionMode detectionMode = DetectionMode.SMART;
    private int maxLogsPerChop = 500;
    private boolean strictSpeciesMatch = false;
    private boolean includeGiantMushrooms = true;
    private boolean showAnimation = true;
    private boolean consumeDurability = true;
    private long cooldownMs = 0;
    private final Set<String> worldAllowlist = new HashSet<>();
    private final Set<String> worldDenylist = new HashSet<>();

    public void load(FileConfiguration config) {
        this.enabled = config.getBoolean("enabled", true);
        this.requireEnchantment = config.getBoolean("require-enchantment", false);

        String detectStr = config.getString("detection-mode", "SMART").toUpperCase();
        try {
            this.detectionMode = DetectionMode.valueOf(detectStr);
        } catch (IllegalArgumentException e) {
            this.detectionMode = DetectionMode.SMART;
        }

        this.maxLogsPerChop = config.getInt("max-logs-per-chop", 500);
        this.strictSpeciesMatch = config.getBoolean("strict-species-match", false);
        this.includeGiantMushrooms = config.getBoolean("include-giant-mushrooms", true);
        this.showAnimation = config.getBoolean("show-animation", true);
        this.consumeDurability = config.getBoolean("consume-durability", true);
        this.cooldownMs = config.getLong("cooldown-ms", 0);

        this.worldAllowlist.clear();
        List<String> allow = config.getStringList("world-allowlist");
        this.worldAllowlist.addAll(allow);

        this.worldDenylist.clear();
        List<String> deny = config.getStringList("world-denylist");
        this.worldDenylist.addAll(deny);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isRequireEnchantment() {
        return requireEnchantment;
    }

    public DetectionMode getDetectionMode() {
        return detectionMode;
    }

    public int getMaxLogsPerChop() {
        return maxLogsPerChop;
    }

    public boolean isStrictSpeciesMatch() {
        return strictSpeciesMatch;
    }

    public boolean isIncludeGiantMushrooms() {
        return includeGiantMushrooms;
    }

    public boolean isShowAnimation() {
        return showAnimation;
    }

    public boolean isConsumeDurability() {
        return consumeDurability;
    }

    public long getCooldownMs() {
        return cooldownMs;
    }

    public boolean isWorldAllowed(String worldName) {
        if (!worldDenylist.isEmpty() && worldDenylist.contains(worldName)) {
            return false;
        }
        if (!worldAllowlist.isEmpty() && !worldAllowlist.contains(worldName)) {
            return false;
        }
        return true;
    }
}
