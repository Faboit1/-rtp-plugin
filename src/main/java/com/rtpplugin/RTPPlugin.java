package com.rtpplugin;

import com.rtpplugin.commands.RTPCommand;
import com.rtpplugin.commands.RTPQCommand;
import com.rtpplugin.commands.RTPTabCompleter;
import com.rtpplugin.config.ConfigManager;
import com.rtpplugin.config.MessageManager;
import com.rtpplugin.integration.EconomyManager;
import com.rtpplugin.integration.GeyserSupport;
import com.rtpplugin.listeners.CombatListener;
import com.rtpplugin.listeners.GuiListener;
import com.rtpplugin.listeners.MovementListener;
import com.rtpplugin.location.LocationPreloader;
import com.rtpplugin.manager.CombatTagManager;
import com.rtpplugin.manager.CooldownManager;
import com.rtpplugin.manager.DailyLimitManager;
import com.rtpplugin.manager.TeleportHistoryManager;
import com.rtpplugin.teleport.TeleportManager;
import org.bukkit.command.PluginCommand;
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
    private DailyLimitManager dailyLimitManager;
    private TeleportHistoryManager teleportHistoryManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load configurations
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);

        // Initialize managers
        cooldownManager = new CooldownManager(this);
        combatTagManager = new CombatTagManager(this);
        dailyLimitManager = new DailyLimitManager(this);
        teleportHistoryManager = new TeleportHistoryManager(configManager.getHistoryMaxEntries());
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

        // Schedule periodic memory cleanup tasks
        scheduleCleanupTasks();

        // Register commands
        RTPCommand rtpCommand = new RTPCommand(this);
        PluginCommand rtpCmd = getCommand("rtp");
        if (rtpCmd != null) {
            rtpCmd.setExecutor(rtpCommand);
            rtpCmd.setTabCompleter(new RTPTabCompleter(this));
        }

        RTPQCommand rtpqCommand = new RTPQCommand(this);
        PluginCommand rtpqCmd = getCommand("rtpq");
        if (rtpqCmd != null) {
            rtpqCmd.setExecutor(rtpqCommand);
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        getLogger().info("RTPPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (locationPreloader != null) {
            locationPreloader.stop();
        }
        if (teleportManager != null) {
            teleportManager.cancelAll();
            teleportManager.getLocationFinder().shutdown();
        }
        getLogger().info("RTPPlugin has been disabled!");
    }

    public void reload() {
        configManager = new ConfigManager(this);
        messageManager = new MessageManager(this);
        cooldownManager = new CooldownManager(this);
        dailyLimitManager.clearAll();
        teleportHistoryManager = new TeleportHistoryManager(configManager.getHistoryMaxEntries());

        // Reload location finder thread pool
        teleportManager.reloadFinder();

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

    /**
     * Schedule periodic cleanup tasks to purge expired entries and prevent
     * unbounded memory growth in the manager maps.
     */
    private void scheduleCleanupTasks() {
        // Cooldown cleanup
        int cooldownInterval = configManager.getCooldownCleanupIntervalMinutes();
        if (cooldownInterval > 0) {
            long ticks = cooldownInterval * 60L * 20L;
            getServer().getScheduler().runTaskTimerAsynchronously(this,
                    () -> cooldownManager.cleanup(), ticks, ticks);
        }

        // Combat tag cleanup
        int combatInterval = configManager.getCombatCleanupIntervalMinutes();
        if (combatInterval > 0) {
            long ticks = combatInterval * 60L * 20L;
            getServer().getScheduler().runTaskTimerAsynchronously(this,
                    () -> combatTagManager.cleanup(), ticks, ticks);
        }

        // Daily limit cleanup (once per hour)
        long hourTicks = 60L * 60L * 20L;
        getServer().getScheduler().runTaskTimerAsynchronously(this,
                () -> dailyLimitManager.cleanup(), hourTicks, hourTicks);
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
    public DailyLimitManager getDailyLimitManager() { return dailyLimitManager; }
    public TeleportHistoryManager getTeleportHistoryManager() { return teleportHistoryManager; }

    public void debug(String message) {
        if (configManager.isDebug()) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }
}
