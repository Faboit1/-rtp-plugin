package com.rtpplugin;

import com.rtpplugin.commands.RTPCommand;
import com.rtpplugin.commands.RTPTabCompleter;
import com.rtpplugin.config.ConfigManager;
import com.rtpplugin.config.MessageManager;
import com.rtpplugin.integration.EconomyManager;
import com.rtpplugin.integration.GeyserSupport;
import com.rtpplugin.listeners.CombatListener;
import com.rtpplugin.listeners.MovementListener;
import com.rtpplugin.location.LocationPreloader;
import com.rtpplugin.manager.CooldownManager;
import com.rtpplugin.manager.CombatTagManager;
import com.rtpplugin.teleport.TeleportManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class RTPPlugin extends JavaPlugin {

    private static RTPPlugin instance;
    private ConfigManager configManager;
    private MessageManager messageManager;
    private CooldownManager cooldownManager;
    private CombatTagManager combatTagManager;
    private TeleportManager teleportManager;
    private LocationPreloader locationPreloader;
    private EconomyManager economyManager;
    private GeyserSupport geyserSupport;

    @Override
    public void onEnable() {
        instance = this;

        // Load configurations
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);

        // Initialize managers
        cooldownManager = new CooldownManager(this);
        combatTagManager = new CombatTagManager(this);
        teleportManager = new TeleportManager(this);

        // Initialize economy
        economyManager = new EconomyManager(this);
        if (configManager.isEconomyEnabled()) {
            economyManager.setup();
        }

        // Initialize Geyser support
        geyserSupport = new GeyserSupport(this);

        // Initialize location preloader
        locationPreloader = new LocationPreloader(this);
        if (configManager.isPreloadEnabled()) {
            locationPreloader.start();
        }

        // Register commands
        RTPCommand rtpCommand = new RTPCommand(this);
        getCommand("rtp").setExecutor(rtpCommand);
        getCommand("rtp").setTabCompleter(new RTPTabCompleter(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);

        getLogger().info("RTPPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (locationPreloader != null) {
            locationPreloader.stop();
        }
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }
        getLogger().info("RTPPlugin has been disabled!");
    }

    public void reload() {
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        cooldownManager = new CooldownManager(this);

        if (locationPreloader != null) {
            locationPreloader.stop();
        }
        if (configManager.isPreloadEnabled()) {
            locationPreloader = new LocationPreloader(this);
            locationPreloader.start();
        }

        if (configManager.isEconomyEnabled()) {
            economyManager.setup();
        }
    }

    public static RTPPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public CombatTagManager getCombatTagManager() { return combatTagManager; }
    public TeleportManager getTeleportManager() { return teleportManager; }
    public LocationPreloader getLocationPreloader() { return locationPreloader; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public GeyserSupport getGeyserSupport() { return geyserSupport; }

    public void debug(String message) {
        if (configManager.isDebug()) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }
}
