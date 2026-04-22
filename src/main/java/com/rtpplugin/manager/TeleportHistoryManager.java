package com.rtpplugin.manager;

import org.bukkit.Location;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a short in-memory history of where each player has been teleported.
 */
public class TeleportHistoryManager {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM");

    private final int maxEntries;
    private final Map<UUID, LinkedList<HistoryEntry>> history = new ConcurrentHashMap<>();

    public TeleportHistoryManager(int maxEntries) {
        this.maxEntries = Math.max(1, maxEntries);
    }

    /** Record a teleport destination for a player. */
    public void record(UUID playerId, Location destination) {
        LinkedList<HistoryEntry> entries = history.computeIfAbsent(playerId, k -> new LinkedList<>());
        synchronized (entries) {
            entries.addFirst(new HistoryEntry(destination, LocalDateTime.now()));
            while (entries.size() > maxEntries) {
                entries.removeLast();
            }
        }
    }

    /**
     * Returns a copy of the player's history list, most recent first.
     * Returns an empty list if the player has no history.
     */
    public List<HistoryEntry> getHistory(UUID playerId) {
        LinkedList<HistoryEntry> entries = history.get(playerId);
        if (entries == null) return Collections.emptyList();
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    /** Clear history for a specific player. */
    public void clearPlayer(UUID playerId) {
        history.remove(playerId);
    }

    /** Clear all history. */
    public void clearAll() {
        history.clear();
    }

    // ── Inner record ──────────────────────────────────────────────────────────

    public static class HistoryEntry {
        private final Location location;
        private final String timestamp;

        public HistoryEntry(Location location, LocalDateTime time) {
            this.location = location.clone();
            this.timestamp = time.format(FORMATTER);
        }

        public Location getLocation() { return location; }
        public String getTimestamp() { return timestamp; }
    }
}
