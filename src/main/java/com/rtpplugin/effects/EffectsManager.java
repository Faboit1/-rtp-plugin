package com.rtpplugin.effects;

import com.rtpplugin.RTPPlugin;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class EffectsManager {

    private final RTPPlugin plugin;

    public EffectsManager(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    public void playTeleportEffects(Player player, Location from, Location to) {
        if (plugin.getConfigManager().isParticlesEnabled()) {
            spawnParticles(player, from);
            spawnParticles(player, to);
        }
        if (plugin.getConfigManager().isSoundEnabled()) {
            playSound(player, plugin.getConfigManager().getTeleportSound());
        }
    }

    public void playWarmupStartEffects(Player player) {
        if (plugin.getConfigManager().isSoundEnabled()) {
            playSound(player, plugin.getConfigManager().getWarmupStartSound());
        }
    }

    public void playWarmupTickEffects(Player player) {
        if (plugin.getConfigManager().isSoundEnabled()) {
            playSound(player, plugin.getConfigManager().getWarmupTickSound());
        }
        if (plugin.getConfigManager().isParticlesEnabled()) {
            spawnParticles(player, player.getLocation());
        }
    }

    private void spawnParticles(Player player, Location location) {
        try {
            Particle particle = Particle.valueOf(plugin.getConfigManager().getParticleType());
            int count = plugin.getConfigManager().getParticleCount();
            player.getWorld().spawnParticle(particle, location, count, 0.5, 1.0, 0.5, 0.1);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type: " + plugin.getConfigManager().getParticleType());
        }
    }

    private void playSound(Player player, String soundName) {
        try {
            Sound sound = Sound.valueOf(soundName);
            float volume = plugin.getConfigManager().getSoundVolume();
            float pitch = plugin.getConfigManager().getSoundPitch();
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound: " + soundName);
        }
    }
}
