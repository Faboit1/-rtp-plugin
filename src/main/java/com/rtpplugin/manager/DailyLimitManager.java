package com.rtpplugin.manager;

import com.rtpplugin.RTPPlugin;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks how many times each player has used RTP today.
 * The daily counter resets at the configured hour (UTC).
 */
public class DailyLimitManager {

    private final RTPPlugin plugin;

    /** Maps player UUID → [useCount, dayEpoch] where dayEpoch is the reset-day index */
    private final Map<UUID, long[]> usages = new ConcurrentHashMap<>();

    public DailyLimitManager(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the current "day epoch" based on the configured reset hour (UTC).
     * This increments once per day at daily-limit.daily-reset-hour.
     */
    private long currentDayEpoch() {
        int resetHour = plugin.getConfigManager().getDailyResetHour();
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        // Shift so that the day boundary is at resetHour
        ZonedDateTime shifted = now.minusHours(resetHour);
        return shifted.toLocalDate().toEpochDay();
    }

    /** Returns how many times this player has used RTP today. */
    public int getUses(UUID playerId) {
        long[] data = usages.get(playerId);
        if (data == null) return 0;
        if (data[1] != currentDayEpoch()) return 0; // stale day
        return (int) data[0];
    }

    /** Increments the daily use counter for this player. */
    public void recordUse(UUID playerId) {
        long day = currentDayEpoch();
        usages.compute(playerId, (id, existing) -> {
            if (existing == null || existing[1] != day) {
                return new long[]{1, day};
            }
            existing[0]++;
            return existing;
        });
    }

    /**
     * Returns true if this player has reached the daily limit
     * (and does not have the bypass permission).
     */
    public boolean hasReachedLimit(org.bukkit.entity.Player player) {
        if (!plugin.getConfigManager().isDailyLimitEnabled()) return false;
        if (player.hasPermission(plugin.getConfigManager().getDailyLimitBypassPermission())) return false;
        return getUses(player.getUniqueId()) >= plugin.getConfigManager().getDailyLimit();
    }

    /** Purge entries whose day epoch is stale (called periodically to free memory). */
    public void cleanup() {
        long today = currentDayEpoch();
        usages.entrySet().removeIf(e -> e.getValue()[1] != today);
    }

    /** Clear all usage data (e.g. on reload). */
    public void clearAll() {
        usages.clear();
    }
}
