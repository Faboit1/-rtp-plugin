package com.rtpplugin.integration;

import com.rtpplugin.RTPPlugin;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class GeyserSupport {

    private final RTPPlugin plugin;
    private boolean floodgateAvailable;

    public GeyserSupport(RTPPlugin plugin) {
        this.plugin = plugin;
        checkAvailability();
    }

    private void checkAvailability() {
        floodgateAvailable = plugin.getServer().getPluginManager().getPlugin("floodgate") != null;
        if (floodgateAvailable) {
            plugin.getLogger().info("Floodgate detected! Bedrock player support enabled.");
        }
    }

    public boolean isBedrockPlayer(Player player) {
        if (!floodgateAvailable) return isBedrockUUID(player.getUniqueId());
        try {
            // Use reflection to avoid hard compile-time dependency on Floodgate API
            Class<?> floodgateApiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Method getInstanceMethod = floodgateApiClass.getMethod("getInstance");
            Object apiInstance = getInstanceMethod.invoke(null);
            Method isFloodgateMethod = floodgateApiClass.getMethod("isFloodgatePlayer", UUID.class);
            return (boolean) isFloodgateMethod.invoke(apiInstance, player.getUniqueId());
        } catch (Exception e) {
            // Fallback: check for Bedrock UUID prefix
            return isBedrockUUID(player.getUniqueId());
        }
    }

    private boolean isBedrockUUID(UUID uuid) {
        // Floodgate prefixes Bedrock UUIDs with 00000000-0000-0000
        return uuid.toString().startsWith("00000000-0000-0000");
    }

    public boolean isFloodgateAvailable() {
        return floodgateAvailable;
    }
}
