package com.rtpplugin.ui;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.util.SmallCapsUtil;
import com.rtpplugin.config.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class RTPQGui {

    private final RTPPlugin plugin;
    public static final String GUI_TITLE = SmallCapsUtil.convert("RTP Queue Manager");

    public RTPQGui(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, MessageManager.colorize("&5" + GUI_TITLE));

        // Header info
        gui.setItem(4, createItem(Material.ENDER_EYE,
                "&d" + SmallCapsUtil.convert("RTP Queue Status"),
                "&7" + SmallCapsUtil.convert("Preloaded locations ready"),
                "&7" + SmallCapsUtil.convert("for instant teleport")));

        // Per-world queue display
        Map<String, Queue<Location>> queues = plugin.getLocationPreloader().getAllQueues();
        int slot = 9;

        for (Map.Entry<String, Queue<Location>> entry : queues.entrySet()) {
            String worldName = entry.getKey();
            Queue<Location> queue = entry.getValue();
            int queueSize = queue.size();
            int maxSize = plugin.getConfigManager().getPreloadQueueSize();

            Material mat = getWorldMaterial(worldName);

            // World queue item
            List<String> lore = new ArrayList<>();
            lore.add(MessageManager.colorize("&7" + SmallCapsUtil.convert("Queued: " + queueSize + "/" + maxSize)));
            lore.add(MessageManager.colorize("&7" + SmallCapsUtil.convert("Status: " + (queueSize > 0 ? "&aReady" : "&cEmpty"))));
            lore.add("");
            lore.add(MessageManager.colorize("&7" + SmallCapsUtil.convert("Min radius: " + plugin.getConfigManager().getMinRadius(worldName))));
            lore.add(MessageManager.colorize("&7" + SmallCapsUtil.convert("Max radius: " + plugin.getConfigManager().getMaxRadius(worldName))));
            lore.add("");

            // Show queued locations
            int locIndex = 1;
            for (Location loc : queue) {
                if (locIndex > 5) {
                    lore.add(MessageManager.colorize("&8" + SmallCapsUtil.convert("... and " + (queueSize - 5) + " more")));
                    break;
                }
                lore.add(MessageManager.colorize("&8" + SmallCapsUtil.convert("#" + locIndex + ": "
                        + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())));
                locIndex++;
            }

            gui.setItem(slot, createItemWithLore(mat,
                    "&a" + SmallCapsUtil.convert(worldName + " (" + queueSize + "/" + maxSize + ")"),
                    lore));
            slot++;
            if (slot == 18) slot = 18; // next row
            if (slot > 26) break;
        }

        // Action buttons row (bottom)
        // Refresh queue button
        gui.setItem(37, createItem(Material.GOLDEN_APPLE,
                "&a" + SmallCapsUtil.convert("Refill All Queues"),
                "&7" + SmallCapsUtil.convert("Click to refill all"),
                "&7" + SmallCapsUtil.convert("location queues")));

        // Clear queue button
        gui.setItem(39, createItem(Material.TNT,
                "&c" + SmallCapsUtil.convert("Clear All Queues"),
                "&7" + SmallCapsUtil.convert("Click to clear all"),
                "&7" + SmallCapsUtil.convert("preloaded locations")));

        // Instant RTP button
        gui.setItem(41, createItem(Material.ENDER_PEARL,
                "&d" + SmallCapsUtil.convert("Instant RTP"),
                "&7" + SmallCapsUtil.convert("Use a preloaded location"),
                "&7" + SmallCapsUtil.convert("for instant teleport")));

        // Stats item
        int totalQueued = queues.values().stream().mapToInt(Queue::size).sum();
        gui.setItem(43, createItem(Material.PAPER,
                "&6" + SmallCapsUtil.convert("Queue Statistics"),
                "&7" + SmallCapsUtil.convert("Total queued: " + totalQueued),
                "&7" + SmallCapsUtil.convert("Worlds: " + queues.size()),
                "&7" + SmallCapsUtil.convert("Refill interval: " + plugin.getConfigManager().getPreloadRefillInterval() + "s")));

        // Close button
        gui.setItem(49, createItem(Material.BARRIER,
                "&c" + SmallCapsUtil.convert("Close"),
                "&7" + SmallCapsUtil.convert("Click to close")));

        player.openInventory(gui);
    }

    private Material getWorldMaterial(String worldName) {
        if (worldName.contains("nether")) return Material.NETHERRACK;
        if (worldName.contains("end")) return Material.END_STONE;
        return Material.GRASS_BLOCK;
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageManager.colorize(name));
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(MessageManager.colorize(line));
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createItemWithLore(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(MessageManager.colorize(name));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
