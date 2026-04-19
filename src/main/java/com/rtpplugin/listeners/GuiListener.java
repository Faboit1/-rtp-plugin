package com.rtpplugin.listeners;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.config.MessageManager;
import com.rtpplugin.ui.RTPGui;
import com.rtpplugin.ui.RTPQGui;
import com.rtpplugin.util.SmallCapsUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class GuiListener implements Listener {

    private final RTPPlugin plugin;

    public GuiListener(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();

        // Handle RTP GUI
        if (title.equals(MessageManager.colorize("&5" + RTPGui.GUI_TITLE))) {
            event.setCancelled(true);
            handleRTPGuiClick(player, event);
            return;
        }

        // Handle RTPQ GUI
        if (title.equals(MessageManager.colorize("&5" + RTPQGui.GUI_TITLE))) {
            event.setCancelled(true);
            handleRTPQGuiClick(player, event);
        }
    }

    private void handleRTPGuiClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        int slot = event.getSlot();

        // Close button
        if (slot == 26 && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Random teleport button (center)
        if (slot == 13 && clicked.getType() == Material.ENDER_PEARL) {
            player.closeInventory();
            String defaultWorld = plugin.getConfigManager().getDefaultWorld();
            if (plugin.getConfigManager().isWorldEnabled(defaultWorld)) {
                plugin.getTeleportManager().initiateRTP(player, defaultWorld);
            }
            return;
        }

        // Compass = RTP Near (slot 22)
        if (slot == 22 && clicked.getType() == Material.COMPASS) {
            player.closeInventory();
            player.sendMessage(plugin.getMessageManager().getMessage("finding-location"));
            // Player needs to specify target via command
            player.sendMessage(MessageManager.colorize("&7" + SmallCapsUtil.convert("Use /rtp near <player> to teleport near someone.")));
            return;
        }

        // World buttons (slots 10-16, skip 13)
        if (slot >= 10 && slot <= 16 && slot != 13) {
            Material mat = clicked.getType();
            if (mat == Material.GRASS_BLOCK || mat == Material.NETHERRACK || mat == Material.END_STONE) {
                player.closeInventory();
                // Find the world name from enabled worlds matching this slot
                Set<String> worlds = plugin.getConfigManager().getEnabledWorlds();
                int targetSlot = 10;
                for (String worldName : worlds) {
                    if (targetSlot == 13) targetSlot = 14;
                    if (targetSlot == slot) {
                        plugin.getTeleportManager().initiateRTP(player, worldName);
                        return;
                    }
                    targetSlot++;
                }
            }
        }
    }

    private void handleRTPQGuiClick(Player player, InventoryClickEvent event) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        int slot = event.getSlot();

        // Close button
        if (slot == 49 && clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            return;
        }

        // Refill all queues
        if (slot == 37 && clicked.getType() == Material.GOLDEN_APPLE) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getLocationPreloader().refillAll();
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(MessageManager.colorize("&8[&5ʀᴛᴘ&8] &7" + SmallCapsUtil.convert("All queues are being refilled..."))));
            });
            return;
        }

        // Clear all queues
        if (slot == 39 && clicked.getType() == Material.TNT) {
            player.closeInventory();
            plugin.getLocationPreloader().clearAll();
            player.sendMessage(MessageManager.colorize("&8[&5ʀᴛᴘ&8] &7" + SmallCapsUtil.convert("All queues cleared.")));
            return;
        }

        // Instant RTP
        if (slot == 41 && clicked.getType() == Material.ENDER_PEARL) {
            player.closeInventory();
            String defaultWorld = plugin.getConfigManager().getDefaultWorld();
            plugin.getTeleportManager().initiateRTP(player, defaultWorld);
            return;
        }
    }
}
