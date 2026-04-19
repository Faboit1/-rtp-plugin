package com.rtpplugin.commands;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.ui.RTPGui;
import com.rtpplugin.util.SmallCapsUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class RTPCommand implements CommandExecutor {

    private final RTPPlugin plugin;
    private final RTPGui rtpGui;

    public RTPCommand(RTPPlugin plugin) {
        this.plugin = plugin;
        this.rtpGui = new RTPGui(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("rtp.use")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        // /rtp gui - open GUI
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("gui"))) {
            rtpGui.open(player);
            return true;
        }

        // /rtp reload
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!player.hasPermission("rtp.reload")) {
                player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                return true;
            }
            plugin.reload();
            player.sendMessage(plugin.getMessageManager().getMessage("reload"));
            return true;
        }

        // /rtp near <player>
        if (args.length >= 2 && args[0].equalsIgnoreCase("near")) {
            if (!player.hasPermission("rtp.near")) {
                player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                return true;
            }
            if (!plugin.getConfigManager().isRtpNearEnabled()) {
                player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.getMessageManager().getMessage("player-not-found",
                        Map.of("player", args[1])));
                return true;
            }
            plugin.getTeleportManager().initiateRTPNear(player, target);
            return true;
        }

        // /rtp <world> [player]
        String worldName = args[0];

        // Check if it's a player name being teleported by admin
        if (args.length >= 2) {
            if (!player.hasPermission("rtp.others")) {
                player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage(plugin.getMessageManager().getMessage("player-not-found",
                        Map.of("player", args[1])));
                return true;
            }

            // Validate world
            World world = Bukkit.getWorld(worldName);
            if (world == null || !plugin.getConfigManager().isWorldEnabled(worldName)) {
                player.sendMessage(plugin.getMessageManager().getMessage("world-disabled",
                        Map.of("world", worldName)));
                return true;
            }

            player.sendMessage(plugin.getMessageManager().getMessage("rtp-others",
                    Map.of("player", target.getName(), "world", worldName)));
            target.sendMessage(plugin.getMessageManager().getMessage("rtp-others-target",
                    Map.of("sender", player.getName())));
            plugin.getTeleportManager().initiateRTP(target, worldName);
            return true;
        }

        // /rtp <world>
        World world = Bukkit.getWorld(worldName);
        if (world == null || !plugin.getConfigManager().isWorldEnabled(worldName)) {
            player.sendMessage(plugin.getMessageManager().getMessage("world-disabled",
                    Map.of("world", worldName)));
            return true;
        }

        plugin.getTeleportManager().initiateRTP(player, worldName);
        return true;
    }
}
