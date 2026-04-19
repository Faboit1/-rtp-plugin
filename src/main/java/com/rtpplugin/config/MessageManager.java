package com.rtpplugin.config;

import com.rtpplugin.RTPPlugin;
import com.rtpplugin.util.SmallCapsUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;

public class MessageManager {

    private final RTPPlugin plugin;
    private FileConfiguration messagesConfig;
    private String prefix;

    public MessageManager(RTPPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
        prefix = colorize(SmallCapsUtil.convert(messagesConfig.getString("prefix", "&8[&5RTP&8] &7")));
    }

    public String getMessage(String key) {
        String msg = messagesConfig.getString(key, "Missing message: " + key);
        return prefix + colorize(SmallCapsUtil.convert(msg));
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String msg = messagesConfig.getString(key, "Missing message: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return prefix + colorize(SmallCapsUtil.convert(msg));
    }

    public String getRawMessage(String key) {
        String msg = messagesConfig.getString(key, "Missing message: " + key);
        return colorize(SmallCapsUtil.convert(msg));
    }

    public String getRawMessage(String key, Map<String, String> placeholders) {
        String msg = messagesConfig.getString(key, "Missing message: " + key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return colorize(SmallCapsUtil.convert(msg));
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
