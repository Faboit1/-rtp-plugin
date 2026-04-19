package com.rtpplugin.util;

import com.rtpplugin.RTPPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class LocationUtils {

    private LocationUtils() {}

    public static int[] getRandomCoordinates(int centerX, int centerZ, int minRadius, int maxRadius, String shape) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x, z;

        if ("square".equalsIgnoreCase(shape)) {
            // Pick random point in square ring
            do {
                x = centerX + random.nextInt(-maxRadius, maxRadius + 1);
                z = centerZ + random.nextInt(-maxRadius, maxRadius + 1);
            } while (Math.abs(x - centerX) < minRadius && Math.abs(z - centerZ) < minRadius);
        } else {
            // Circle shape - pick random angle and distance
            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = minRadius + random.nextDouble() * (maxRadius - minRadius);
            x = centerX + (int) (Math.cos(angle) * distance);
            z = centerZ + (int) (Math.sin(angle) * distance);
        }

        return new int[]{x, z};
    }

    public static boolean isWithinWorldBorder(World world, int x, int z) {
        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        double size = border.getSize() / 2.0;
        return Math.abs(x - center.getX()) < size && Math.abs(z - center.getZ()) < size;
    }

    public static boolean isBiomeAllowed(Biome biome, String mode, List<String> blacklist, List<String> whitelist) {
        String biomeName = biome.name();
        if ("whitelist".equalsIgnoreCase(mode)) {
            return whitelist.contains(biomeName);
        } else {
            return !blacklist.contains(biomeName);
        }
    }

    public static Location findSafeY(World world, int x, int z, int minY, int maxY,
                                      Set<Material> safeBlocks, Set<Material> unsafeBlocks,
                                      boolean avoidLava, boolean avoidWater) {
        // Search from top down for highest safe location
        int highestY = world.getHighestBlockYAt(x, z);
        if (highestY < minY) return null;
        if (highestY > maxY) highestY = maxY;

        Block block = world.getBlockAt(x, highestY, z);
        Block above1 = world.getBlockAt(x, highestY + 1, z);
        Block above2 = world.getBlockAt(x, highestY + 2, z);

        // Check if ground block is safe
        Material groundMat = block.getType();
        if (unsafeBlocks.contains(groundMat)) return null;
        if (avoidLava && groundMat == Material.LAVA) return null;
        if (avoidWater && groundMat == Material.WATER) return null;
        if (!groundMat.isSolid()) return null;

        // Check if space above is clear
        if (above1.getType().isSolid() || above2.getType().isSolid()) return null;
        if (unsafeBlocks.contains(above1.getType()) || unsafeBlocks.contains(above2.getType())) return null;

        return new Location(world, x + 0.5, highestY + 1, z + 0.5);
    }

    public static Set<Material> parseMaterialList(List<String> materialNames) {
        return materialNames.stream()
                .map(name -> {
                    try {
                        return Material.valueOf(name.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
