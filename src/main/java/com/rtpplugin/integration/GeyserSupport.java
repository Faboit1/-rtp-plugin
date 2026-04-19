package com.rtpplugin.integration;

import com.rtpplugin.RTPPlugin;
import org.bukkit.entity.Player;

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
        if (!floodgateAvailable) return false;
        try {
            return org.geysermc.floodgate.api.FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
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
