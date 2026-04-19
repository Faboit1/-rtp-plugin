package com.rtpplugin.config;

import com.rtpplugin.RTPPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigManager {

    private final RTPPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(RTPPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    // General
    public String getDefaultWorld() { return config.getString("general.default-world", "world"); }
    public int getMaxAttempts() { return config.getInt("general.max-attempts", 30); }
    public boolean isDebug() { return config.getBoolean("general.debug", false); }

    // World settings
    public boolean isWorldEnabled(String world) {
        return config.getBoolean("worlds." + world + ".enabled", false);
    }

    public int getMinRadius(String world) {
        return config.getInt("worlds." + world + ".min-radius", 500);
    }

    public int getMaxRadius(String world) {
        return config.getInt("worlds." + world + ".max-radius", 10000);
    }

    public int getCenterX(String world) {
        return config.getInt("worlds." + world + ".center-x", 0);
    }

    public int getCenterZ(String world) {
        return config.getInt("worlds." + world + ".center-z", 0);
    }

    public boolean useWorldBorder(String world) {
        return config.getBoolean("worlds." + world + ".use-world-border", true);
    }

    public String getShape(String world) {
        return config.getString("worlds." + world + ".shape", "circle");
    }

    public int getMaxRadiusForPlayer(org.bukkit.entity.Player player, String world) {
        if (isPermissionRadiusEnabled()) {
            ConfigurationSection tiers = config.getConfigurationSection("permission-radius.tiers");
            if (tiers != null) {
                int highest = getMaxRadius(world);
                for (String tier : tiers.getKeys(false)) {
                    String perm = tiers.getString(tier + ".permission", "");
                    int radius = tiers.getInt(tier + ".max-radius", highest);
                    if (player.hasPermission(perm) && radius > highest) {
                        highest = radius;
                    }
                }
                return highest;
            }
        }
        return getMaxRadius(world);
    }

    // Safety
    public boolean avoidLava() { return config.getBoolean("safety.avoid-lava", true); }
    public boolean avoidWater() { return config.getBoolean("safety.avoid-water", true); }
    public boolean avoidVoid() { return config.getBoolean("safety.avoid-void", true); }
    public int getMinY() { return config.getInt("safety.min-y", 1); }
    public int getMaxY() { return config.getInt("safety.max-y", 320); }

    public List<String> getSafeBlocks() {
        return config.getStringList("safety.safe-blocks");
    }

    public List<String> getUnsafeBlocks() {
        return config.getStringList("safety.unsafe-blocks");
    }

    // Biomes
    public String getBiomeMode() { return config.getString("biomes.mode", "blacklist"); }

    public List<String> getBiomeBlacklist() {
        return config.getStringList("biomes.blacklist");
    }

    public List<String> getBiomeWhitelist() {
        return config.getStringList("biomes.whitelist");
    }

    // Cooldown
    public boolean isCooldownEnabled() { return config.getBoolean("cooldown.enabled", true); }
    public int getCooldownTime() { return config.getInt("cooldown.time", 300); }
    public boolean isCooldownPerWorld() { return config.getBoolean("cooldown.per-world", false); }

    // Warmup
    public boolean isWarmupEnabled() { return config.getBoolean("warmup.enabled", true); }
    public int getWarmupTime() { return config.getInt("warmup.time", 5); }
    public boolean cancelOnMove() { return config.getBoolean("warmup.cancel-on-move", true); }
    public boolean cancelOnDamage() { return config.getBoolean("warmup.cancel-on-damage", true); }
    public double getMoveThreshold() { return config.getDouble("warmup.move-threshold", 0.5); }

    // Economy
    public boolean isEconomyEnabled() { return config.getBoolean("economy.enabled", false); }
    public double getCost(String world) {
        double perWorld = config.getDouble("economy.per-world-cost." + world, -1);
        return perWorld >= 0 ? perWorld : config.getDouble("economy.cost", 100.0);
    }

    // Combat
    public boolean isCombatEnabled() { return config.getBoolean("combat.enabled", true); }
    public int getCombatTagDuration() { return config.getInt("combat.tag-duration", 15); }

    // Effects
    public boolean isParticlesEnabled() { return config.getBoolean("effects.particles.enabled", true); }
    public String getParticleType() { return config.getString("effects.particles.type", "PORTAL"); }
    public int getParticleCount() { return config.getInt("effects.particles.count", 50); }
    public boolean isSoundEnabled() { return config.getBoolean("effects.sound.enabled", true); }
    public String getTeleportSound() { return config.getString("effects.sound.on-teleport", "ENTITY_ENDERMAN_TELEPORT"); }
    public String getWarmupStartSound() { return config.getString("effects.sound.on-warmup-start", "BLOCK_NOTE_BLOCK_PLING"); }
    public String getWarmupTickSound() { return config.getString("effects.sound.on-warmup-tick", "BLOCK_NOTE_BLOCK_HAT"); }
    public float getSoundVolume() { return (float) config.getDouble("effects.sound.volume", 1.0); }
    public float getSoundPitch() { return (float) config.getDouble("effects.sound.pitch", 1.0); }

    // UI
    public boolean isBossbarEnabled() { return config.getBoolean("ui.bossbar.enabled", true); }
    public String getBossbarColor() { return config.getString("ui.bossbar.color", "PURPLE"); }
    public String getBossbarStyle() { return config.getString("ui.bossbar.style", "SOLID"); }
    public String getBossbarTitle() { return config.getString("ui.bossbar.title", "&dTeleporting in &f{time}s&d..."); }
    public boolean isTitleEnabled() { return config.getBoolean("ui.title.enabled", true); }
    public int getTitleFadeIn() { return config.getInt("ui.title.fade-in", 5); }
    public int getTitleStay() { return config.getInt("ui.title.stay", 20); }
    public int getTitleFadeOut() { return config.getInt("ui.title.fade-out", 5); }
    public String getTitleMain() { return config.getString("ui.title.main", "&5RTP"); }
    public String getTitleSubtitle() { return config.getString("ui.title.subtitle", "&dTeleporting in &f{time}s&d..."); }
    public boolean isActionbarEnabled() { return config.getBoolean("ui.actionbar.enabled", true); }
    public String getActionbarMessage() { return config.getString("ui.actionbar.message", "&dRTP Warmup: &f{time}s &dremaining..."); }

    // Preload
    public boolean isPreloadEnabled() { return config.getBoolean("preload.enabled", true); }
    public int getPreloadQueueSize() { return config.getInt("preload.queue-size", 5); }
    public int getPreloadRefillInterval() { return config.getInt("preload.refill-interval", 60); }
    public boolean isPreloadPerWorld() { return config.getBoolean("preload.per-world", true); }

    // Permission radius
    public boolean isPermissionRadiusEnabled() { return config.getBoolean("permission-radius.enabled", false); }

    // RTP Near
    public boolean isRtpNearEnabled() { return config.getBoolean("rtp-near.enabled", true); }
    public int getRtpNearMinDistance() { return config.getInt("rtp-near.min-distance", 100); }
    public int getRtpNearMaxDistance() { return config.getInt("rtp-near.max-distance", 500); }

    // Chunk preload
    public boolean isChunkPreloadEnabled() { return config.getBoolean("chunk-preload.enabled", true); }
    public int getChunkPreloadRadius() { return config.getInt("chunk-preload.radius", 2); }
    public int getChunkPreloadTimeout() { return config.getInt("chunk-preload.timeout", 10); }

    public Set<String> getEnabledWorlds() {
        ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
        Set<String> enabled = new HashSet<>();
        if (worldsSection != null) {
            for (String world : worldsSection.getKeys(false)) {
                if (config.getBoolean("worlds." + world + ".enabled", false)) {
                    enabled.add(world);
                }
            }
        }
        return enabled;
    }
}
