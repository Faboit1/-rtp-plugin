# ʀᴛᴘ ᴘʟᴜɢɪɴ — ʜɪɢʜ-ᴘᴇʀғᴏʀᴍᴀɴᴄᴇ ʀᴀɴᴅᴏᴍ ᴛᴇʟᴇᴘᴏʀᴛ

A high-performance Minecraft RTP (Random Teleport) plugin for Paper/Spigot 1.20+, designed for large SMP servers (100+ players). All player-facing text uses **Unicode small capitals** for a unique aesthetic.

## ✨ Features

- **Fully Async Teleportation** — No main-thread lag, uses Paper's async teleport API
- **Smart Location Finding** — Avoids oceans, lava, caves, air drops with safe Y-level detection
- **World Border & Custom Region Support** — Respects world borders and configurable center/radius
- **Per-World RTP Settings** — Individual radius, center, shape (circle/square) per world
- **Cooldowns, Delays & Costs** — Configurable cooldown, warmup countdown, Vault economy support
- **BossBar/Title/ActionBar UI** — Beautiful countdown display during warmup
- **Location Queue Preloading** — Pre-generates safe locations for instant RTP
- **Chunk Preloading** — Loads chunks before teleporting for smooth arrival
- **Biome Blacklist/Whitelist** — Filter which biomes players can land in
- **Geyser/Floodgate Compatibility** — Full Bedrock player support via reflection
- **Combat Tag Check** — Prevents escaping fights with RTP
- **Movement Cancel System** — Cancels teleport if player moves during warmup
- **Particle & Sound Effects** — Configurable visual/audio effects
- **Permission-Based Radius** — Tiered radius limits by permission
- **RTP Near Player** — `/rtp near <player>` to teleport near another player
- **RTP GUI** — Inventory menu for easy world selection and RTP
- **RTPQ GUI** — Queue management GUI for admins (`/rtpq`)
- **Small Capitals Text** — All player-facing text rendered in ᴜɴɪᴄᴏᴅᴇ sᴍᴀʟʟ ᴄᴀᴘs

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/rtp` | Open RTP GUI | `rtp.use` |
| `/rtp gui` | Open RTP GUI | `rtp.use` |
| `/rtp <world>` | RTP in specific world | `rtp.use` |
| `/rtp <world> <player>` | RTP another player | `rtp.others` |
| `/rtp near <player>` | RTP near a player | `rtp.near` |
| `/rtp reload` | Reload configuration | `rtp.reload` |
| `/rtpq` | Open queue management GUI | `rtp.admin` |

**Aliases:** `/randomtp`, `/wild`, `/randomteleport`

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `rtp.use` | Use /rtp command | Everyone |
| `rtp.admin` | Admin access (queue GUI) | OP |
| `rtp.bypass.cooldown` | Bypass cooldown | OP |
| `rtp.bypass.cost` | Bypass economy cost | OP |
| `rtp.bypass.delay` | Bypass warmup delay | OP |
| `rtp.bypass.combat` | Bypass combat check | OP |
| `rtp.near` | Use /rtp near | OP |
| `rtp.others` | RTP other players | OP |
| `rtp.reload` | Reload config | OP |
| `rtp.radius.vip` | VIP radius tier | - |
| `rtp.radius.mvp` | MVP radius tier | - |
| `rtp.radius.elite` | Elite radius tier | - |

## ⚙️ Configuration

All settings are in `config.yml`:
- **Per-world settings** — radius, center, shape, enable/disable
- **Safety** — avoid lava/water/void, safe/unsafe blocks, Y-level range
- **Biomes** — blacklist or whitelist mode
- **Cooldown** — global or per-world, bypass permission
- **Warmup** — countdown time, cancel on move/damage
- **Economy** — Vault integration, per-world costs
- **Combat** — tag duration, prevent RTP in combat
- **Effects** — particles, sounds, volumes
- **UI** — BossBar, Title, ActionBar configuration
- **Preloading** — queue size, refill interval, per-world
- **Permission radius** — tiered radius by permission node

Messages are in `messages.yml` and fully customizable.

## 🏗️ Building

```bash
mvn clean package
```

The plugin JAR will be in `target/rtp-plugin-1.0.0.jar`.

## 📦 Dependencies

- **Paper/Spigot 1.20+** (required)
- **Vault** (optional — for economy features)
- **Geyser/Floodgate** (optional — for Bedrock support)

## 📁 Project Structure

```
src/main/java/com/rtpplugin/
├── RTPPlugin.java              # Main plugin class
├── commands/
│   ├── RTPCommand.java         # /rtp command handler
│   ├── RTPQCommand.java        # /rtpq command handler
│   └── RTPTabCompleter.java    # Tab completion
├── config/
│   ├── ConfigManager.java      # Configuration management
│   └── MessageManager.java     # Messages with small caps
├── effects/
│   └── EffectsManager.java     # Particles & sounds
├── integration/
│   ├── EconomyManager.java     # Vault economy integration
│   └── GeyserSupport.java      # Bedrock/Floodgate support
├── listeners/
│   ├── CombatListener.java     # Combat tagging
│   ├── GuiListener.java        # GUI click handler
│   └── MovementListener.java   # Movement cancel
├── location/
│   ├── LocationFinder.java     # Async safe location finding
│   └── LocationPreloader.java  # Location queue preloading
├── manager/
│   ├── CombatTagManager.java   # Combat tag tracking
│   └── CooldownManager.java    # Cooldown tracking
├── teleport/
│   └── TeleportManager.java    # Async teleportation
├── ui/
│   ├── RTPGui.java             # RTP inventory GUI
│   ├── RTPQGui.java            # RTPQ inventory GUI
│   └── WarmupUI.java           # BossBar/Title/ActionBar
└── util/
    ├── LocationUtils.java      # Location utilities
    └── SmallCapsUtil.java      # Unicode small caps converter
```
