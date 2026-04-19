package com.rtpplugin.listeners;

import com.rtpplugin.RTPPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {

    private final RTPPlugin plugin;

    public CombatListener(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isCombatEnabled()) return;

        // Tag attacker
        if (event.getDamager() instanceof Player attacker) {
            plugin.getCombatTagManager().tag(attacker.getUniqueId());

            // Cancel warmup if they're being teleported
            if (plugin.getConfigManager().cancelOnDamage() && plugin.getTeleportManager().isWarmingUp(attacker.getUniqueId())) {
                plugin.getTeleportManager().cancelSession(attacker.getUniqueId());
                attacker.sendMessage(plugin.getMessageManager().getMessage("warmup-cancelled-damage"));
            }
        }

        // Tag victim
        if (event.getEntity() instanceof Player victim) {
            plugin.getCombatTagManager().tag(victim.getUniqueId());

            // Cancel warmup if they're being teleported
            if (plugin.getConfigManager().cancelOnDamage() && plugin.getTeleportManager().isWarmingUp(victim.getUniqueId())) {
                plugin.getTeleportManager().cancelSession(victim.getUniqueId());
                victim.sendMessage(plugin.getMessageManager().getMessage("warmup-cancelled-damage"));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getCombatTagManager().untag(event.getPlayer().getUniqueId());
        plugin.getTeleportManager().cancelSession(event.getPlayer().getUniqueId());
    }
}
