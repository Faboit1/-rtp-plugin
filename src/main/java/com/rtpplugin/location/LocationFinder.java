package com.rtpplugin.location;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.util.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LocationFinder {

    private final RTPPlugin plugin;

    public LocationFinder(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Location> findLocation(World world, int centerX, int centerZ, int minRadius, int maxRadius, String shape) {
        return CompletableFuture.supplyAsync(() -> {
            int maxAttempts = plugin.getConfigManager().getMaxAttempts();
            Set<Material> unsafeBlocks = LocationUtils.parseMaterialList(plugin.getConfigManager().getUnsafeBlocks());
            Set<Material> safeBlocks = LocationUtils.parseMaterialList(plugin.getConfigManager().getSafeBlocks());
            boolean avoidLava = plugin.getConfigManager().avoidLava();
            boolean avoidWater = plugin.getConfigManager().avoidWater();
            int minY = plugin.getConfigManager().getMinY();
            int maxY = plugin.getConfigManager().getMaxY();
            String biomeMode = plugin.getConfigManager().getBiomeMode();
            List<String> blacklist = plugin.getConfigManager().getBiomeBlacklist();
            List<String> whitelist = plugin.getConfigManager().getBiomeWhitelist();

            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                int[] coords = LocationUtils.getRandomCoordinates(centerX, centerZ, minRadius, maxRadius, shape);
                int x = coords[0];
                int z = coords[1];

                // Check world border
                if (plugin.getConfigManager().useWorldBorder(world.getName())) {
                    if (!LocationUtils.isWithinWorldBorder(world, x, z)) {
                        plugin.debug("Attempt " + attempt + ": Outside world border");
                        continue;
                    }
                }

                // These need to run on main thread
                try {
                    Location result = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
                        // Check biome
                        Biome biome = world.getBiome(x, world.getHighestBlockYAt(x, z), z);
                        if (!LocationUtils.isBiomeAllowed(biome, biomeMode, blacklist, whitelist)) {
                            return null;
                        }

                        // Find safe Y
                        return LocationUtils.findSafeY(world, x, z, minY, maxY, safeBlocks, unsafeBlocks, avoidLava, avoidWater);
                    }).get();

                    if (result != null) {
                        plugin.debug("Found safe location at attempt " + (attempt + 1) + ": " + result);
                        return result;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error finding location: " + e.getMessage());
                }
            }
            return null;
        });
    }

    public CompletableFuture<Location> findNearPlayer(org.bukkit.entity.Player target) {
        int minDist = plugin.getConfigManager().getRtpNearMinDistance();
        int maxDist = plugin.getConfigManager().getRtpNearMaxDistance();
        Location targetLoc = target.getLocation();
        return findLocation(targetLoc.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockZ(), minDist, maxDist, "circle");
    }
}
