package com.rtpplugin.ui;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.util.SmallCapsUtil;
import com.rtpplugin.config.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RTPGui {

    private final RTPPlugin plugin;
    public static final String GUI_TITLE = SmallCapsUtil.convert("Random Teleport");

    public RTPGui(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, MessageManager.colorize("&5" + GUI_TITLE));

        // Random teleport button (center)
        gui.setItem(13, createItem(Material.ENDER_PEARL,
                "&d" + SmallCapsUtil.convert("Random Teleport"),
                "&7" + SmallCapsUtil.convert("Click to teleport to a"),
                "&7" + SmallCapsUtil.convert("random location!")));

        // World buttons
        Set<String> worlds = plugin.getConfigManager().getEnabledWorlds();
        int slot = 10;
        for (String worldName : worlds) {
            Material mat = getWorldMaterial(worldName);
            gui.setItem(slot, createItem(mat,
                    "&a" + SmallCapsUtil.convert(worldName),
                    "&7" + SmallCapsUtil.convert("Click to RTP in"),
                    "&7" + SmallCapsUtil.convert(worldName),
                    "",
                    "&8" + SmallCapsUtil.convert("Radius: " + plugin.getConfigManager().getMinRadius(worldName)
                            + " - " + plugin.getConfigManager().getMaxRadius(worldName))));
            slot++;
            if (slot == 13) slot = 14; // skip center
            if (slot > 16) break;
        }

        // RTP Near button
        if (plugin.getConfigManager().isRtpNearEnabled() && player.hasPermission("rtp.near")) {
            gui.setItem(22, createItem(Material.COMPASS,
                    "&e" + SmallCapsUtil.convert("RTP Near Player"),
                    "&7" + SmallCapsUtil.convert("Teleport near another"),
                    "&7" + SmallCapsUtil.convert("player")));
        }

        // Info item
        gui.setItem(0, createItem(Material.BOOK,
                "&6" + SmallCapsUtil.convert("Information"),
                "&7" + SmallCapsUtil.convert("Cooldown: " + plugin.getConfigManager().getCooldownTime() + "s"),
                "&7" + SmallCapsUtil.convert("Warmup: " + plugin.getConfigManager().getWarmupTime() + "s"),
                plugin.getConfigManager().isEconomyEnabled()
                        ? "&7" + SmallCapsUtil.convert("Cost: $" + plugin.getConfigManager().getCost(plugin.getConfigManager().getDefaultWorld()))
                        : "&7" + SmallCapsUtil.convert("Cost: Free")));

        // Close button
        gui.setItem(26, createItem(Material.BARRIER,
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
}
