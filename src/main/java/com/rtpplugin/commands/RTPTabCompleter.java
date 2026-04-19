package com.rtpplugin.commands;

import com.rtpplugin.RTPPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RTPTabCompleter implements TabCompleter {

    private final RTPPlugin plugin;

    public RTPTabCompleter(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First arg: world names, "near", "reload", "gui"
            completions.addAll(plugin.getConfigManager().getEnabledWorlds());
            completions.add("near");
            completions.add("gui");
            if (sender.hasPermission("rtp.reload")) {
                completions.add("reload");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("near")) {
                // Player names
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
            } else {
                // Could be player name for /rtp <world> <player>
                if (sender.hasPermission("rtp.others")) {
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                }
            }
        }

        // Filter by current input
        String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}
