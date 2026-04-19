package com.rtpplugin.location;

import com.rtpplugin.RTPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LocationPreloader {

    private final RTPPlugin plugin;
    private final LocationFinder finder;
    private final Map<String, Queue<Location>> queues = new ConcurrentHashMap<>();
    private BukkitTask refillTask;

    public LocationPreloader(RTPPlugin plugin) {
        this.plugin = plugin;
        this.finder = new LocationFinder(plugin);
    }

    public void start() {
        // Initialize queues for enabled worlds
        for (String worldName : plugin.getConfigManager().getEnabledWorlds()) {
            queues.putIfAbsent(worldName, new ConcurrentLinkedQueue<>());
        }

        // Initial fill
        refillAll();

        // Schedule periodic refill
        int interval = plugin.getConfigManager().getPreloadRefillInterval() * 20; // Convert to ticks
        refillTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::refillAll, interval, interval);

        plugin.getLogger().info("Location preloader started!");
    }

    public void stop() {
        if (refillTask != null) {
            refillTask.cancel();
            refillTask = null;
        }
        queues.clear();
    }

    public void refillAll() {
        for (String worldName : plugin.getConfigManager().getEnabledWorlds()) {
            refillWorld(worldName);
        }
    }

    public void refillWorld(String worldName) {
        Queue<Location> queue = queues.computeIfAbsent(worldName, k -> new ConcurrentLinkedQueue<>());
        int maxSize = plugin.getConfigManager().getPreloadQueueSize();

        while (queue.size() < maxSize) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) break;

            int centerX = plugin.getConfigManager().getCenterX(worldName);
            int centerZ = plugin.getConfigManager().getCenterZ(worldName);
            int minRadius = plugin.getConfigManager().getMinRadius(worldName);
            int maxRadius = plugin.getConfigManager().getMaxRadius(worldName);
            String shape = plugin.getConfigManager().getShape(worldName);

            finder.findLocation(world, centerX, centerZ, minRadius, maxRadius, shape)
                    .thenAccept(location -> {
                        if (location != null && queue.size() < maxSize) {
                            queue.offer(location);
                            plugin.debug("Preloaded location for " + worldName + ": " + location);
                        }
                    });
        }
    }

    public Location pollLocation(String worldName) {
        Queue<Location> queue = queues.get(worldName);
        if (queue == null) return null;
        Location location = queue.poll();
        // Trigger refill if getting low
        if (queue.size() < plugin.getConfigManager().getPreloadQueueSize() / 2) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> refillWorld(worldName));
        }
        return location;
    }

    public int getQueueSize(String worldName) {
        Queue<Location> queue = queues.get(worldName);
        return queue != null ? queue.size() : 0;
    }

    public Map<String, Queue<Location>> getAllQueues() {
        return Collections.unmodifiableMap(queues);
    }

    public void clearAll() {
        queues.values().forEach(Queue::clear);
    }
}
