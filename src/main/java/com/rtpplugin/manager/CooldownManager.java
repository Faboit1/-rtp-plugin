package com.rtpplugin.manager;

import com.rtpplugin.RTPPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private final RTPPlugin plugin;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void setCooldown(UUID playerId, String world) {
        String key = getKey(playerId, world);
        cooldowns.put(key, System.currentTimeMillis());
    }

    public boolean isOnCooldown(UUID playerId, String world) {
        if (!plugin.getConfigManager().isCooldownEnabled()) return false;
        String key = getKey(playerId, world);
        Long lastUse = cooldowns.get(key);
        if (lastUse == null) return false;
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        if (elapsed >= plugin.getConfigManager().getCooldownTime()) {
            cooldowns.remove(key);
            return false;
        }
        return true;
    }

    public int getRemainingCooldown(UUID playerId, String world) {
        String key = getKey(playerId, world);
        Long lastUse = cooldowns.get(key);
        if (lastUse == null) return 0;
        long elapsed = (System.currentTimeMillis() - lastUse) / 1000;
        int remaining = plugin.getConfigManager().getCooldownTime() - (int) elapsed;
        return Math.max(0, remaining);
    }

    private String getKey(UUID playerId, String world) {
        if (plugin.getConfigManager().isCooldownPerWorld()) {
            return playerId.toString() + ":" + world;
        }
        return playerId.toString();
    }

    public void clearCooldown(UUID playerId) {
        cooldowns.entrySet().removeIf(entry -> entry.getKey().startsWith(playerId.toString()));
    }

    /**
     * Remove all expired cooldown entries to prevent unbounded memory growth.
     * Called periodically by the plugin's maintenance task.
     */
    public void cleanup() {
        long cooldownMillis = plugin.getConfigManager().getCooldownTime() * 1000L;
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> (now - entry.getValue()) >= cooldownMillis);
    }
}
