package com.rtpplugin.ui;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.config.MessageManager;
import com.rtpplugin.util.SmallCapsUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarmupUI {

    private final RTPPlugin plugin;
    private final Map<UUID, BossBar> activeBossBars = new HashMap<>();

    public WarmupUI(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void showWarmup(Player player, int remainingSeconds, int totalSeconds) {
        if (plugin.getConfigManager().isBossbarEnabled()) {
            showBossBar(player, remainingSeconds, totalSeconds);
        }
        if (plugin.getConfigManager().isTitleEnabled()) {
            showTitle(player, remainingSeconds);
        }
        if (plugin.getConfigManager().isActionbarEnabled()) {
            showActionBar(player, remainingSeconds);
        }
    }

    public void removeUI(Player player) {
        removeBossBar(player);
    }

    private void showBossBar(Player player, int remaining, int total) {
        UUID uuid = player.getUniqueId();
        BossBar bar = activeBossBars.get(uuid);

        String title = plugin.getConfigManager().getBossbarTitle()
                .replace("{time}", String.valueOf(remaining));
        title = SmallCapsUtil.convert(MessageManager.colorize(title));

        if (bar == null) {
            try {
                BarColor color = BarColor.valueOf(plugin.getConfigManager().getBossbarColor());
                BarStyle style = BarStyle.valueOf(plugin.getConfigManager().getBossbarStyle());
                bar = Bukkit.createBossBar(title, color, style);
                bar.addPlayer(player);
                activeBossBars.put(uuid, bar);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid bossbar color/style in config");
                return;
            }
        } else {
            bar.setTitle(title);
        }

        double progress = (double) remaining / total;
        bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
    }

    private void removeBossBar(Player player) {
        BossBar bar = activeBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
            bar.removeAll();
        }
    }

    private void showTitle(Player player, int remaining) {
        String main = plugin.getConfigManager().getTitleMain();
        String subtitle = plugin.getConfigManager().getTitleSubtitle()
                .replace("{time}", String.valueOf(remaining));

        main = SmallCapsUtil.convert(MessageManager.colorize(main));
        subtitle = SmallCapsUtil.convert(MessageManager.colorize(subtitle));

        int fadeIn = plugin.getConfigManager().getTitleFadeIn();
        int stay = plugin.getConfigManager().getTitleStay();
        int fadeOut = plugin.getConfigManager().getTitleFadeOut();

        player.sendTitle(main, subtitle, fadeIn, stay, fadeOut);
    }

    private void showActionBar(Player player, int remaining) {
        String message = plugin.getConfigManager().getActionbarMessage()
                .replace("{time}", String.valueOf(remaining));
        message = SmallCapsUtil.convert(MessageManager.colorize(message));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(message));
    }
}
