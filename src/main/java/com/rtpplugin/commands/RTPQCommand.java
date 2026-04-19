package com.rtpplugin.commands;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.ui.RTPQGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RTPQCommand implements CommandExecutor {

    private final RTPPlugin plugin;
    private final RTPQGui rtpqGui;

    public RTPQCommand(RTPPlugin plugin) {
        this.plugin = plugin;
        this.rtpqGui = new RTPQGui(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("rtp.admin")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no-permission"));
            return true;
        }

        rtpqGui.open(player);
        return true;
    }
}
