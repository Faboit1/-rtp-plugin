package com.rtpplugin.location;

import com.rtpplugin.RTPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationPreloader {

    private final RTPPlugin plugin;
    private final LocationFinder finder;
    private final Map<String, Queue<Location>> queues = new ConcurrentHashMap<>();
    /**
     * Tracks how many async find-location futures are currently in-flight per world.
     * This prevents the refill loop from spawning unlimited concurrent futures,
     * which was the root cause of the memory leak / server timeout issue.
     */
    private final Map<String, AtomicInteger> inFlight = new ConcurrentHashMap<>();
    private BukkitTask refillTask;

    public LocationPreloader(RTPPlugin plugin) {
        this.plugin = plugin;
        this.finder = new LocationFinder(plugin);
    }

    public void start() {
        for (String worldName : plugin.getConfigManager().getEnabledWorlds()) {
            queues.putIfAbsent(worldName, new ConcurrentLinkedQueue<>());
            inFlight.putIfAbsent(worldName, new AtomicInteger(0));
        }

        refillAll();

        int interval = plugin.getConfigManager().getPreloadRefillInterval() * 20;
        refillTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::refillAll, interval, interval);

        plugin.getLogger().info("Location preloader started!");
    }

    public void stop() {
        if (refillTask != null) {
            refillTask.cancel();
            refillTask = null;
        }
        finder.shutdown();
        queues.clear();
        inFlight.clear();
    }

    public void refillAll() {
        for (String worldName : plugin.getConfigManager().getEnabledWorlds()) {
            refillWorld(worldName);
        }
    }

    public void refillWorld(String worldName) {
        Queue<Location> queue = queues.computeIfAbsent(worldName, k -> new ConcurrentLinkedQueue<>());
        AtomicInteger flight = inFlight.computeIfAbsent(worldName, k -> new AtomicInteger(0));

        int maxSize = plugin.getConfigManager().getPreloadQueueSize(worldName);
        int maxConcurrent = plugin.getConfigManager().getMaxConcurrentSearches();

        // Calculate exactly how many new searches to start, capped by maxConcurrent
        int currentTotal = queue.size() + flight.get();
        int needed = Math.min(maxSize - currentTotal, maxConcurrent - flight.get());
        if (needed <= 0) return;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        int centerX = plugin.getConfigManager().getCenterX(worldName);
        int centerZ = plugin.getConfigManager().getCenterZ(worldName);
        int minRadius = plugin.getConfigManager().getMinRadius(worldName);
        int maxRadius = plugin.getConfigManager().getMaxRadius(worldName);
        String shape = plugin.getConfigManager().getShape(worldName);

        for (int i = 0; i < needed; i++) {
            flight.incrementAndGet();
            finder.findLocation(world, centerX, centerZ, minRadius, maxRadius, shape)
                    .whenComplete((location, ex) -> {
                        flight.decrementAndGet();
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
        int maxSize = plugin.getConfigManager().getPreloadQueueSize(worldName);
        if (queue.size() < maxSize / 2) {
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
