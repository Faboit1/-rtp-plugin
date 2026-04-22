package com.rtpplugin.effects;

import com.rtpplugin.RTPPlugin;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class EffectsManager {

    // Event-key constants used as config path segments under "effects.<event>"
    public static final String DEPARTURE    = "departure";
    public static final String ARRIVAL      = "arrival";
    public static final String WARMUP_START = "warmup-start";
    public static final String WARMUP_TICK  = "warmup-tick";
    public static final String WARMUP_CANCEL = "warmup-cancel";
    public static final String DENIED       = "denied";
    public static final String NO_LOCATION  = "no-location";

    private static final Map<String, Color> COLOR_MAP;
    static {
        COLOR_MAP = new java.util.HashMap<>();
        COLOR_MAP.put("WHITE",   Color.WHITE);
        COLOR_MAP.put("SILVER",  Color.SILVER);
        COLOR_MAP.put("GRAY",    Color.GRAY);
        COLOR_MAP.put("BLACK",   Color.BLACK);
        COLOR_MAP.put("RED",     Color.RED);
        COLOR_MAP.put("MAROON",  Color.MAROON);
        COLOR_MAP.put("YELLOW",  Color.YELLOW);
        COLOR_MAP.put("OLIVE",   Color.OLIVE);
        COLOR_MAP.put("LIME",    Color.LIME);
        COLOR_MAP.put("GREEN",   Color.GREEN);
        COLOR_MAP.put("AQUA",    Color.AQUA);
        COLOR_MAP.put("TEAL",    Color.TEAL);
        COLOR_MAP.put("BLUE",    Color.BLUE);
        COLOR_MAP.put("NAVY",    Color.NAVY);
        COLOR_MAP.put("FUCHSIA", Color.FUCHSIA);
        COLOR_MAP.put("PURPLE",  Color.PURPLE);
        COLOR_MAP.put("ORANGE",  Color.ORANGE);
    }

    private final RTPPlugin plugin;
    /** Players that currently have our warmup potion effects applied */
    private final Set<UUID> warmupEffectPlayers = Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<UUID, Boolean>());

    public EffectsManager(RTPPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Called when the teleport executes (player disappears from their old spot). */
    public void playDepartureEffects(Player player) {
        playParticleEffect(player, player.getLocation(), DEPARTURE);
        playSoundEffect(player, DEPARTURE);
    }

    /** Called after the player has arrived at the destination. */
    public void playArrivalEffects(Player player, Location to) {
        playParticleEffect(player, to, ARRIVAL);
        playSoundEffect(player, ARRIVAL);

        if (plugin.getConfigManager().isArrivalFakeLightningEnabled()) {
            to.getWorld().strikeLightningEffect(to);
        }
        if (plugin.getConfigManager().isArrivalFireworkEnabled()) {
            launchFirework(to);
        }
    }

    /** Called when the warmup countdown starts. */
    public void playWarmupStartEffects(Player player) {
        playParticleEffect(player, player.getLocation(), WARMUP_START);
        playSoundEffect(player, WARMUP_START);
        applyWarmupPotionEffects(player);
    }

    /** Called every second during the warmup countdown. */
    public void playWarmupTickEffects(Player player) {
        playParticleEffect(player, player.getLocation(), WARMUP_TICK);
        playSoundEffect(player, WARMUP_TICK);
    }

    /** Called when the warmup is cancelled (move / damage / command). */
    public void playWarmupCancelEffects(Player player) {
        playParticleEffect(player, player.getLocation(), WARMUP_CANCEL);
        playSoundEffect(player, WARMUP_CANCEL);
        removeWarmupPotionEffects(player);
    }

    /** Called when the player is denied by cooldown / combat / economy / permission. */
    public void playDeniedEffects(Player player) {
        playSoundEffect(player, DENIED);
    }

    /** Called when no safe location could be found. */
    public void playNoLocationEffects(Player player) {
        playSoundEffect(player, NO_LOCATION);
    }

    // ── Potion effects ────────────────────────────────────────────────────────

    public void applyWarmupPotionEffects(Player player) {
        if (!plugin.getConfigManager().isWarmupPotionEffectsEnabled()) return;
        int warmupTicks = plugin.getConfigManager().getWarmupTime() * 20 + 20;

        if (plugin.getConfigManager().isWarmupSlownessEnabled()) {
            int level = Math.max(1, plugin.getConfigManager().getWarmupSlownessLevel()) - 1;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, warmupTicks, level, false, true, true));
        }
        if (plugin.getConfigManager().isWarmupBlindnessEnabled()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, warmupTicks, 0, false, true, true));
        }
        if (plugin.getConfigManager().isWarmupGlowingEnabled()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, warmupTicks, 0, false, false, false));
        }
        warmupEffectPlayers.add(player.getUniqueId());
    }

    public void removeWarmupPotionEffects(Player player) {
        if (!warmupEffectPlayers.remove(player.getUniqueId())) return;
        if (plugin.getConfigManager().isWarmupSlownessEnabled()) {
            player.removePotionEffect(PotionEffectType.SLOW);
        }
        if (plugin.getConfigManager().isWarmupBlindnessEnabled()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        if (plugin.getConfigManager().isWarmupGlowingEnabled()) {
            player.removePotionEffect(PotionEffectType.GLOWING);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void playParticleEffect(Player player, Location location, String event) {
        if (!plugin.getConfigManager().isEffectParticleEnabled(event)) return;
        try {
            Particle particle = Particle.valueOf(plugin.getConfigManager().getEffectParticleType(event));
            int count = plugin.getConfigManager().getEffectParticleCount(event);
            double sx = plugin.getConfigManager().getEffectParticleSpreadX(event);
            double sy = plugin.getConfigManager().getEffectParticleSpreadY(event);
            double sz = plugin.getConfigManager().getEffectParticleSpreadZ(event);
            double speed = plugin.getConfigManager().getEffectParticleSpeed(event);
            player.getWorld().spawnParticle(particle, location, count, sx, sy, sz, speed);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid particle type for effect '" + event + "': "
                    + plugin.getConfigManager().getEffectParticleType(event));
        }
    }

    private void playSoundEffect(Player player, String event) {
        if (!plugin.getConfigManager().isEffectSoundEnabled(event)) return;
        String soundName = plugin.getConfigManager().getEffectSoundName(event);
        try {
            Sound sound = Sound.valueOf(soundName);
            float volume = plugin.getConfigManager().getEffectSoundVolume(event);
            float pitch  = plugin.getConfigManager().getEffectSoundPitch(event);
            float variation = plugin.getConfigManager().getEffectSoundPitchVariation(event);
            if (variation > 0f) {
                pitch += (float) (ThreadLocalRandom.current().nextDouble(-variation, variation));
                pitch = Math.max(0.1f, Math.min(2.0f, pitch));
            }
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound for effect '" + event + "': " + soundName);
        }
    }

    private void launchFirework(Location location) {
        try {
            Firework fw = location.getWorld().spawn(location, Firework.class);
            FireworkMeta meta = fw.getFireworkMeta();

            FireworkEffect.Type type = FireworkEffect.Type.valueOf(
                    plugin.getConfigManager().getArrivalFireworkType());

            List<Color> colors = parseColors(plugin.getConfigManager().getArrivalFireworkColors());
            List<Color> fades  = parseColors(plugin.getConfigManager().getArrivalFireworkFadeColors());

            FireworkEffect effect = FireworkEffect.builder()
                    .with(type)
                    .withColor(colors.isEmpty() ? List.of(Color.PURPLE) : colors)
                    .withFade(fades.isEmpty() ? List.of(Color.WHITE) : fades)
                    .trail(plugin.getConfigManager().isArrivalFireworkTrail())
                    .flicker(plugin.getConfigManager().isArrivalFireworkFlicker())
                    .build();

            meta.addEffect(effect);
            meta.setPower(1);
            fw.setFireworkMeta(meta);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid firework type: " + plugin.getConfigManager().getArrivalFireworkType());
        }
    }

    private List<Color> parseColors(List<String> names) {
        List<Color> result = new ArrayList<>();
        for (String name : names) {
            Color color = COLOR_MAP.get(name.toUpperCase());
            if (color != null) {
                result.add(color);
            }
        }
        return result;
    }
}
