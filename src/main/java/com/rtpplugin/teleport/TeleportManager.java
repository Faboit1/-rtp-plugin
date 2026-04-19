package com.rtpplugin.teleport;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.effects.EffectsManager;
import com.rtpplugin.location.LocationFinder;
import com.rtpplugin.ui.WarmupUI;
import com.rtpplugin.util.SmallCapsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportManager {

    private final RTPPlugin plugin;
    private final LocationFinder locationFinder;
    private final EffectsManager effectsManager;
    private final WarmupUI warmupUI;
    private final Map<UUID, TeleportSession> activeSessions = new ConcurrentHashMap<>();

    public TeleportManager(RTPPlugin plugin) {
        this.plugin = plugin;
        this.locationFinder = new LocationFinder(plugin);
        this.effectsManager = new EffectsManager(plugin);
        this.warmupUI = new WarmupUI(plugin);
    }

    public void initiateRTP(Player player, String worldName) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("already-teleporting"));
            return;
        }

        // Check combat tag
        if (plugin.getConfigManager().isCombatEnabled()
                && !player.hasPermission("rtp.bypass.combat")
                && plugin.getCombatTagManager().isTagged(player.getUniqueId())) {
            int remaining = plugin.getCombatTagManager().getRemainingTime(player.getUniqueId());
            player.sendMessage(plugin.getMessageManager().getMessage("combat-tagged",
                    Map.of("time", String.valueOf(remaining))));
            return;
        }

        // Check cooldown
        if (plugin.getConfigManager().isCooldownEnabled()
                && !player.hasPermission("rtp.bypass.cooldown")
                && plugin.getCooldownManager().isOnCooldown(player.getUniqueId(), worldName)) {
            int remaining = plugin.getCooldownManager().getRemainingCooldown(player.getUniqueId(), worldName);
            player.sendMessage(plugin.getMessageManager().getMessage("cooldown",
                    Map.of("time", String.valueOf(remaining))));
            return;
        }

        // Check economy
        if (plugin.getEconomyManager().isEnabled() && !player.hasPermission("rtp.bypass.cost")) {
            double cost = plugin.getConfigManager().getCost(worldName);
            if (!plugin.getEconomyManager().hasEnough(player, cost)) {
                player.sendMessage(plugin.getMessageManager().getMessage("economy-insufficient",
                        Map.of("cost", String.valueOf(cost),
                                "balance", String.valueOf(plugin.getEconomyManager().getBalance(player)))));
                return;
            }
        }

        // Check Bedrock
        if (plugin.getGeyserSupport().isBedrockPlayer(player)) {
            player.sendMessage(plugin.getMessageManager().getMessage("bedrock-notice"));
        }

        player.sendMessage(plugin.getMessageManager().getMessage("finding-location"));

        // Try preloaded location first
        Location preloaded = plugin.getLocationPreloader().pollLocation(worldName);
        if (preloaded != null) {
            plugin.debug("Using preloaded location for " + player.getName());
            startTeleport(player, preloaded, worldName);
        } else {
            // Find new location async
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                player.sendMessage(plugin.getMessageManager().getMessage("world-not-found",
                        Map.of("world", worldName)));
                return;
            }

            int centerX = plugin.getConfigManager().getCenterX(worldName);
            int centerZ = plugin.getConfigManager().getCenterZ(worldName);
            int minRadius = plugin.getConfigManager().getMinRadius(worldName);
            int maxRadius = plugin.getConfigManager().getMaxRadiusForPlayer(player, worldName);
            String shape = plugin.getConfigManager().getShape(worldName);

            locationFinder.findLocation(world, centerX, centerZ, minRadius, maxRadius, shape)
                    .thenAccept(location -> {
                        if (location != null) {
                            Bukkit.getScheduler().runTask(plugin, () -> startTeleport(player, location, worldName));
                        } else {
                            Bukkit.getScheduler().runTask(plugin, () ->
                                    player.sendMessage(plugin.getMessageManager().getMessage("no-safe-location")));
                        }
                    });
        }
    }

    public void initiateRTPNear(Player player, Player target) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("already-teleporting"));
            return;
        }

        player.sendMessage(plugin.getMessageManager().getMessage("rtp-near",
                Map.of("player", target.getName())));

        locationFinder.findNearPlayer(target)
                .thenAccept(location -> {
                    if (location != null) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                                startTeleport(player, location, target.getWorld().getName()));
                    } else {
                        Bukkit.getScheduler().runTask(plugin, () ->
                                player.sendMessage(plugin.getMessageManager().getMessage("no-safe-location")));
                    }
                });
    }

    private void startTeleport(Player player, Location destination, String worldName) {
        if (!player.isOnline()) return;

        // Skip warmup if player has bypass
        if (!plugin.getConfigManager().isWarmupEnabled() || player.hasPermission("rtp.bypass.delay")) {
            executeTeleport(player, destination, worldName);
            return;
        }

        int warmupTime = plugin.getConfigManager().getWarmupTime();
        Location startLocation = player.getLocation().clone();

        effectsManager.playWarmupStartEffects(player);

        TeleportSession session = new TeleportSession(player.getUniqueId(), destination, startLocation, worldName);

        // Warmup countdown
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int remaining = warmupTime;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelSession(player.getUniqueId());
                    return;
                }

                if (remaining <= 0) {
                    cancelSession(player.getUniqueId());
                    executeTeleport(player, destination, worldName);
                    return;
                }

                warmupUI.showWarmup(player, remaining, warmupTime);
                effectsManager.playWarmupTickEffects(player);
                remaining--;
            }
        }, 0L, 20L); // Every second

        session.setTask(task);
        activeSessions.put(player.getUniqueId(), session);
    }

    private void executeTeleport(Player player, Location destination, String worldName) {
        if (!player.isOnline()) return;

        // Charge economy
        if (plugin.getEconomyManager().isEnabled() && !player.hasPermission("rtp.bypass.cost")) {
            double cost = plugin.getConfigManager().getCost(worldName);
            if (plugin.getEconomyManager().charge(player, cost)) {
                player.sendMessage(plugin.getMessageManager().getMessage("economy-charged",
                        Map.of("cost", String.valueOf(cost))));
            }
        }

        player.sendMessage(plugin.getMessageManager().getMessage("chunk-loading"));

        // Preload chunks
        if (plugin.getConfigManager().isChunkPreloadEnabled()) {
            preloadChunks(destination).thenRun(() ->
                    Bukkit.getScheduler().runTask(plugin, () -> performTeleport(player, destination, worldName)));
        } else {
            performTeleport(player, destination, worldName);
        }
    }

    private void performTeleport(Player player, Location destination, String worldName) {
        if (!player.isOnline()) return;

        Location from = player.getLocation().clone();

        // Use Paper async teleport if available, otherwise sync
        try {
            player.teleportAsync(destination).thenAccept(success -> {
                if (success) {
                    onTeleportComplete(player, from, destination, worldName);
                } else {
                    player.sendMessage(plugin.getMessageManager().getMessage("no-safe-location"));
                }
            });
        } catch (NoSuchMethodError e) {
            // Fallback for Spigot (no async teleport)
            player.teleport(destination);
            onTeleportComplete(player, from, destination, worldName);
        }
    }

    private void onTeleportComplete(Player player, Location from, Location to, String worldName) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            effectsManager.playTeleportEffects(player, from, to);
            warmupUI.removeUI(player);
            player.sendMessage(plugin.getMessageManager().getMessage("teleported",
                    Map.of("x", String.valueOf(to.getBlockX()),
                            "y", String.valueOf(to.getBlockY()),
                            "z", String.valueOf(to.getBlockZ()))));

            // Set cooldown
            plugin.getCooldownManager().setCooldown(player.getUniqueId(), worldName);
        });
    }

    private CompletableFuture<Void> preloadChunks(Location location) {
        return CompletableFuture.runAsync(() -> {
            int radius = plugin.getConfigManager().getChunkPreloadRadius();
            int chunkX = location.getBlockX() >> 4;
            int chunkZ = location.getBlockZ() >> 4;
            World world = location.getWorld();

            List<CompletableFuture<Chunk>> futures = new ArrayList<>();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    futures.add(world.getChunkAtAsync(chunkX + dx, chunkZ + dz));
                }
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        });
    }

    public void cancelSession(UUID playerId) {
        TeleportSession session = activeSessions.remove(playerId);
        if (session != null) {
            session.cancel();
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                warmupUI.removeUI(player);
            }
        }
    }

    public boolean isWarmingUp(UUID playerId) {
        return activeSessions.containsKey(playerId);
    }

    public TeleportSession getSession(UUID playerId) {
        return activeSessions.get(playerId);
    }

    public void cancelAll() {
        for (UUID playerId : new HashSet<>(activeSessions.keySet())) {
            cancelSession(playerId);
        }
    }

    // Inner class for teleport session
    public static class TeleportSession {
        private final UUID playerId;
        private final Location destination;
        private final Location startLocation;
        private final String worldName;
        private BukkitTask task;

        public TeleportSession(UUID playerId, Location destination, Location startLocation, String worldName) {
            this.playerId = playerId;
            this.destination = destination;
            this.startLocation = startLocation;
            this.worldName = worldName;
        }

        public UUID getPlayerId() { return playerId; }
        public Location getDestination() { return destination; }
        public Location getStartLocation() { return startLocation; }
        public String getWorldName() { return worldName; }

        public void setTask(BukkitTask task) { this.task = task; }

        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }
    }
}
