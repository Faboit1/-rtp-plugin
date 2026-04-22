package com.rtpplugin.manager;

import com.rtpplugin.RTPPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatTagManager {

    private final RTPPlugin plugin;
    private final Map<UUID, Long> combatTags = new ConcurrentHashMap<>();

    public CombatTagManager(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void tag(UUID playerId) {
        if (!plugin.getConfigManager().isCombatEnabled()) return;
        combatTags.put(playerId, System.currentTimeMillis());
    }

    public boolean isTagged(UUID playerId) {
        if (!plugin.getConfigManager().isCombatEnabled()) return false;
        Long tagTime = combatTags.get(playerId);
        if (tagTime == null) return false;
        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        if (elapsed >= plugin.getConfigManager().getCombatTagDuration()) {
            combatTags.remove(playerId);
            return false;
        }
        return true;
    }

    public int getRemainingTime(UUID playerId) {
        Long tagTime = combatTags.get(playerId);
        if (tagTime == null) return 0;
        long elapsed = (System.currentTimeMillis() - tagTime) / 1000;
        int remaining = plugin.getConfigManager().getCombatTagDuration() - (int) elapsed;
        return Math.max(0, remaining);
    }

    /**
     * Remove all expired combat tags to prevent unbounded memory growth.
     * Called periodically by the plugin's maintenance task.
     */
    public void cleanup() {
        long tagDurationMillis = plugin.getConfigManager().getCombatTagDuration() * 1000L;
        long now = System.currentTimeMillis();
        combatTags.entrySet().removeIf(entry -> (now - entry.getValue()) >= tagDurationMillis);
    }

    public void untag(UUID playerId) {
        combatTags.remove(playerId);
    }
}
