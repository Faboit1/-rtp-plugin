package com.rtpplugin.listeners;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.teleport.TeleportManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    private final RTPPlugin plugin;

    public MovementListener(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().cancelOnMove()) return;

        Player player = event.getPlayer();
        if (!plugin.getTeleportManager().isWarmingUp(player.getUniqueId())) return;

        TeleportManager.TeleportSession session = plugin.getTeleportManager().getSession(player.getUniqueId());
        if (session == null) return;

        Location from = session.getStartLocation();
        Location to = event.getTo();
        if (to == null) return;

        double threshold = plugin.getConfigManager().getMoveThreshold();
        double distSquared = Math.pow(from.getX() - to.getX(), 2)
                + Math.pow(from.getY() - to.getY(), 2)
                + Math.pow(from.getZ() - to.getZ(), 2);

        if (distSquared > threshold * threshold) {
            plugin.getTeleportManager().cancelSession(player.getUniqueId());
            player.sendMessage(plugin.getMessageManager().getMessage("warmup-cancelled-move"));
        }
    }
}
