package com.rtpplugin.integration;

import com.rtpplugin.RTPPlugin;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final RTPPlugin plugin;
    private Economy economy;
    private boolean enabled;

    public EconomyManager(RTPPlugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    public void setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found! Economy features disabled.");
            enabled = false;
            return;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No economy provider found! Economy features disabled.");
            enabled = false;
            return;
        }

        economy = rsp.getProvider();
        enabled = true;
        plugin.getLogger().info("Economy hooked successfully via Vault!");
    }

    public boolean isEnabled() {
        return enabled && economy != null && plugin.getConfigManager().isEconomyEnabled();
    }

    public boolean hasEnough(Player player, double amount) {
        if (!isEnabled()) return true;
        return economy.has(player, amount);
    }

    public boolean charge(Player player, double amount) {
        if (!isEnabled()) return true;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    public double getBalance(Player player) {
        if (!isEnabled()) return 0;
        return economy.getBalance(player);
    }

    public String formatAmount(double amount) {
        if (!isEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }
}
