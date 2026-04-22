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

    // ── General ──────────────────────────────────────────────────────────────

    public String getDefaultWorld() { return config.getString("general.default-world", "world"); }

    /** Per-world max-attempts override, falls back to global. */
    public int getMaxAttempts(String world) {
        int perWorld = config.getInt("worlds." + world + ".max-attempts", -1);
        return perWorld > 0 ? perWorld : config.getInt("general.max-attempts", 30);
    }

    public boolean isDebug() { return config.getBoolean("general.debug", false); }

    // ── Performance ───────────────────────────────────────────────────────────

    /** Max concurrent async location searches that may be in-flight per world at once. */
    public int getMaxConcurrentSearches() { return config.getInt("performance.max-concurrent-searches", 2); }

    /** Size of the dedicated finder thread pool. */
    public int getLocationFinderThreads() { return config.getInt("performance.location-finder-threads", 2); }

    // ── World settings ────────────────────────────────────────────────────────

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

    // ── Safety ────────────────────────────────────────────────────────────────

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

    // ── Biomes ────────────────────────────────────────────────────────────────

    public String getBiomeMode() { return config.getString("biomes.mode", "blacklist"); }

    public List<String> getBiomeBlacklist() {
        return config.getStringList("biomes.blacklist");
    }

    public List<String> getBiomeWhitelist() {
        return config.getStringList("biomes.whitelist");
    }

    // ── Cooldown ──────────────────────────────────────────────────────────────

    public boolean isCooldownEnabled() { return config.getBoolean("cooldown.enabled", true); }
    public int getCooldownTime() { return config.getInt("cooldown.time", 300); }
    public boolean isCooldownPerWorld() { return config.getBoolean("cooldown.per-world", false); }
    public int getCooldownCleanupIntervalMinutes() { return config.getInt("cooldown.cleanup-interval-minutes", 10); }

    // ── Warmup ────────────────────────────────────────────────────────────────

    public boolean isWarmupEnabled() { return config.getBoolean("warmup.enabled", true); }
    public int getWarmupTime() { return config.getInt("warmup.time", 5); }
    public boolean cancelOnMove() { return config.getBoolean("warmup.cancel-on-move", true); }
    public boolean cancelOnDamage() { return config.getBoolean("warmup.cancel-on-damage", true); }
    public boolean cancelOnCommand() { return config.getBoolean("warmup.cancel-on-command", false); }
    public double getMoveThreshold() { return config.getDouble("warmup.move-threshold", 0.5); }

    // Warmup potion effects
    public boolean isWarmupPotionEffectsEnabled() { return config.getBoolean("warmup.potion-effects.enabled", true); }
    public boolean isWarmupSlownessEnabled() { return config.getBoolean("warmup.potion-effects.slowness.enabled", true); }
    public int getWarmupSlownessLevel() { return config.getInt("warmup.potion-effects.slowness.level", 2); }
    public boolean isWarmupBlindnessEnabled() { return config.getBoolean("warmup.potion-effects.blindness.enabled", false); }
    public boolean isWarmupGlowingEnabled() { return config.getBoolean("warmup.potion-effects.glowing.enabled", true); }

    // ── Economy ───────────────────────────────────────────────────────────────

    public boolean isEconomyEnabled() { return config.getBoolean("economy.enabled", false); }
    public double getCost(String world) {
        double perWorld = config.getDouble("economy.per-world-cost." + world, -1);
        return perWorld >= 0 ? perWorld : config.getDouble("economy.cost", 100.0);
    }

    // ── Combat ────────────────────────────────────────────────────────────────

    public boolean isCombatEnabled() { return config.getBoolean("combat.enabled", true); }
    public int getCombatTagDuration() { return config.getInt("combat.tag-duration", 15); }
    public int getCombatCleanupIntervalMinutes() { return config.getInt("combat.cleanup-interval-minutes", 5); }

    // ── Effects – per-event helpers ───────────────────────────────────────────

    // Particles
    public boolean isEffectParticleEnabled(String event) {
        return config.getBoolean("effects." + event + ".particles.enabled", false);
    }
    public String getEffectParticleType(String event) {
        return config.getString("effects." + event + ".particles.type", "PORTAL");
    }
    public int getEffectParticleCount(String event) {
        return config.getInt("effects." + event + ".particles.count", 30);
    }
    public double getEffectParticleSpreadX(String event) {
        return config.getDouble("effects." + event + ".particles.spread-x", 0.3);
    }
    public double getEffectParticleSpreadY(String event) {
        return config.getDouble("effects." + event + ".particles.spread-y", 0.5);
    }
    public double getEffectParticleSpreadZ(String event) {
        return config.getDouble("effects." + event + ".particles.spread-z", 0.3);
    }
    public double getEffectParticleSpeed(String event) {
        return config.getDouble("effects." + event + ".particles.speed", 0.05);
    }

    // Sounds
    public boolean isEffectSoundEnabled(String event) {
        return config.getBoolean("effects." + event + ".sound.enabled", false);
    }
    public String getEffectSoundName(String event) {
        return config.getString("effects." + event + ".sound.name", "UI_BUTTON_CLICK");
    }
    public float getEffectSoundVolume(String event) {
        return (float) config.getDouble("effects." + event + ".sound.volume", 1.0);
    }
    public float getEffectSoundPitch(String event) {
        return (float) config.getDouble("effects." + event + ".sound.pitch", 1.0);
    }
    public float getEffectSoundPitchVariation(String event) {
        return (float) config.getDouble("effects." + event + ".sound.pitch-variation", 0.0);
    }

    // Arrival-only extras
    public boolean isArrivalFakeLightningEnabled() { return config.getBoolean("effects.arrival.fake-lightning", false); }
    public boolean isArrivalFireworkEnabled() { return config.getBoolean("effects.arrival.firework.enabled", false); }
    public String getArrivalFireworkType() { return config.getString("effects.arrival.firework.type", "BALL_LARGE"); }
    public List<String> getArrivalFireworkColors() { return config.getStringList("effects.arrival.firework.colors"); }
    public List<String> getArrivalFireworkFadeColors() { return config.getStringList("effects.arrival.firework.fade-colors"); }
    public boolean isArrivalFireworkTrail() { return config.getBoolean("effects.arrival.firework.trail", true); }
    public boolean isArrivalFireworkFlicker() { return config.getBoolean("effects.arrival.firework.flicker", true); }

    // ── UI ────────────────────────────────────────────────────────────────────

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

    // ── Preload ───────────────────────────────────────────────────────────────

    public boolean isPreloadEnabled() { return config.getBoolean("preload.enabled", true); }

    /** Returns the preload queue size, with per-world override support. */
    public int getPreloadQueueSize(String world) {
        int perWorld = config.getInt("worlds." + world + ".preload-queue-size", -1);
        return perWorld > 0 ? perWorld : config.getInt("preload.queue-size", 5);
    }

    public int getPreloadRefillInterval() { return config.getInt("preload.refill-interval", 60); }
    public boolean isPreloadPerWorld() { return config.getBoolean("preload.per-world", true); }

    // ── Permission radius ─────────────────────────────────────────────────────

    public boolean isPermissionRadiusEnabled() { return config.getBoolean("permission-radius.enabled", false); }

    // ── RTP Near ─────────────────────────────────────────────────────────────

    public boolean isRtpNearEnabled() { return config.getBoolean("rtp-near.enabled", true); }
    public int getRtpNearMinDistance() { return config.getInt("rtp-near.min-distance", 100); }
    public int getRtpNearMaxDistance() { return config.getInt("rtp-near.max-distance", 500); }

    // ── Chunk preload ─────────────────────────────────────────────────────────

    public boolean isChunkPreloadEnabled() { return config.getBoolean("chunk-preload.enabled", true); }
    public int getChunkPreloadRadius() { return config.getInt("chunk-preload.radius", 2); }
    public int getChunkPreloadTimeout() { return config.getInt("chunk-preload.timeout", 10); }

    // ── Announce ─────────────────────────────────────────────────────────────

    public boolean isAnnounceEnabled() { return config.getBoolean("announce.enabled", false); }
    public String getAnnounceMode() { return config.getString("announce.mode", "world"); }
    public int getAnnounceNearbyRadius() { return config.getInt("announce.nearby-radius", 100); }
    public String getAnnounceMessage() { return config.getString("announce.message", "&6{player} &ehas been randomly teleported!"); }
    public String getAnnounceSeePermission() { return config.getString("announce.see-permission", ""); }

    // ── Daily limit ───────────────────────────────────────────────────────────

    public boolean isDailyLimitEnabled() { return config.getBoolean("daily-limit.enabled", false); }
    public int getDailyLimit() { return config.getInt("daily-limit.limit", 5); }
    public int getDailyResetHour() { return config.getInt("daily-limit.daily-reset-hour", 0); }
    public String getDailyLimitBypassPermission() { return config.getString("daily-limit.bypass-permission", "rtp.bypass.daily"); }

    // ── History ───────────────────────────────────────────────────────────────

    public boolean isHistoryEnabled() { return config.getBoolean("history.enabled", true); }
    public int getHistoryMaxEntries() { return config.getInt("history.max-entries", 10); }

    // ── Enabled worlds ────────────────────────────────────────────────────────

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
