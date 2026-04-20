<div align="center">

# 🔗 Link

**Bridge your Minecraft players to Discord — seamlessly.**

[![Version](https://img.shields.io/badge/version-1.0.1-6C63FF?style=for-the-badge&logo=github)](https://github.com/huskydreaming/link/releases)
[![Java](https://img.shields.io/badge/java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Velocity](https://img.shields.io/badge/velocity-3.5.0-0080FF?style=for-the-badge)](https://papermc.io/software/velocity)
[![Spigot](https://img.shields.io/badge/spigot-1.21.4-FF6B35?style=for-the-badge)](https://www.spigotmc.org/)
[![Folia](https://img.shields.io/badge/folia-1.21.4-8A2BE2?style=for-the-badge)](https://papermc.io/software/folia)
[![License](https://img.shields.io/badge/license-MIT-2ECC71?style=for-the-badge)](LICENSE)

*Link Minecraft accounts to Discord, assign roles automatically, and run reward commands — all driven by a single Discord embed.*

</div>

---

## ✨ Features

- 🤖 **Built-in Discord bot** — no separate bot process needed
- 🔒 **Secure code-based linking** — time-limited 6-character codes
- 🎁 **Reward commands** — run console commands on link & unlink per server
- ♻️ **Re-link aware** — separate messages and no duplicate rewards
- ⏱️ **Configurable cooldown** — prevent abuse after unlinking
- 🎨 **Full MiniMessage support** — every player message is customisable
- 🌐 **Two deployment modes** — Velocity proxy or standalone Spigot / Folia
- 🍃 **Folia support** — region-threaded safe scheduling out of the box
- 🔄 **Live reload** — `/link reload` reloads all config files without restarting

---

## 📋 Requirements

| Requirement | Version |
|---|---|
| Java | 21+ |
| Velocity *(proxy mode)* | 3.5.0+ |
| Spigot *(standalone mode)* | 1.21.4+ |
| Folia *(standalone mode)* | 1.21.4+ |
| SQLite *(default, built-in)* | — |
| MySQL *(optional)* | 8.0+ |
| MariaDB *(optional)* | 10.6+ |
| PostgreSQL *(optional)* | 14+ |

---

## 🤖 Creating a Discord Bot

Before installing the plugin you need a Discord application.

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications) and click **New Application**
2. Name it (e.g. `Link`) and hit **Create**
3. Open the **Bot** tab → click **Add Bot**
4. Under **Privileged Gateway Intents** enable **Server Members Intent**
5. Click **Reset Token** and copy your token — you'll need it in `discord.yml`
6. Open the **OAuth2 → URL Generator** tab
   - Scopes: `bot`, `applications.commands`
   - Bot Permissions: `Manage Roles`, `Send Messages`, `Read Messages/View Channels`
7. Copy the generated URL, open it in your browser and invite the bot to your server
8. In your Discord server: make sure the bot's role is **above** any role it needs to assign

---

## 📦 Installation

### Option A — Velocity Proxy *(recommended for networks)*

This is the standard setup. The bot and database live on the **Velocity proxy** only. Spigot/Folia servers are lightweight receivers.

**Step 1 — Velocity proxy**
1. Drop `Link-velocity.jar` into your Velocity `plugins/` folder
2. Start the server once to generate `plugins/link/config.yml`, `database.yml`, `discord.yml`, and `messages.yml`
3. Fill in `database.yml` with your database credentials
4. Fill in `discord.yml` with your Discord token, guild ID, and role ID
5. Restart Velocity

**Step 2 — Each Spigot / Folia backend**
1. Drop the appropriate JAR into the backend `plugins/` folder
2. Start the server once to generate config files
3. Ensure `mode` is set to `velocity-bridge` in `config.yml` (this is the default)
4. Restart the backend server

That's it. The proxy handles everything — the backend just listens for plugin messages and fires the commands you configure.

---

### Option B — Standalone Spigot / Folia *(no proxy)*

The full bot, database and commands run directly on a single server.

1. Drop the appropriate JAR into your `plugins/` folder
2. Start the server once to generate the config files
3. Open `plugins/link/config.yml` and set:
   ```yaml
   mode: standalone
   ```
4. Fill in `database.yml` with your database credentials
5. Fill in `discord.yml` with your Discord token, guild ID, and role ID
6. Restart the server

---

## ⚙️ Configuration

Config is split into separate files for clarity. All files are generated automatically on first start.

### `config.yml` — Velocity

```yaml
cooldown: 3600        # seconds a player must wait before re-linking

servers:
  survival:           # must match your Velocity backend server name
    link-commands:
      - "lp user %player% parent add member"
      - "eco give %player% 100"
    unlink-commands:
      - "lp user %player% parent remove member"
```

### `config.yml` — Spigot / Folia (standalone mode)

```yaml
mode: standalone

cooldown: 3600

link-commands:
  - "lp user %player% parent add member"
unlink-commands:
  - "lp user %player% parent remove member"
```

### `database.yml`

```yaml
# Driver: sqlite | mysql | mariadb | postgresql  (default: sqlite)
driver: sqlite

# SQLite file path (only used when driver is 'sqlite')
file: "plugins/link/link.db"

# Remote connection settings (ignored for SQLite)
host: "localhost"
port: 3306
name: "link"
username: "user"
password: "password"
pool:
  maximum-pool-size: 10    # forced to 1 for SQLite
  minimum-idle: 2
  connection-timeout: 10000
  idle-timeout: 600000
  max-lifetime: 1800000
  keepalive-time: 60000
```

### `discord.yml`

```yaml
token: "YOUR_BOT_TOKEN"
guild-id: 123456789012345678
role-id: 123456789012345678
embed:
  title: "Account Linking"
  description: "Link your account to get in-game rewards!"
  color: "#97BA52"
  button-label: "✅ Authenticate Account"
  fields:
    - name: "Linking Guide:"
      value: " - Join the server\n - Run `/link`\n - Enter your code here"
      inline: false
```

> `%player%` is replaced with the player's username in all commands.

---

## 🔄 Velocity-Bridge vs Standalone

| | Velocity-Bridge | Standalone |
|---|---|---|
| **Where bot runs** | Velocity proxy | Spigot / Folia server |
| **Database** | Proxy only | Local server |
| **Best for** | Networks with multiple backends | Single-server setups |
| **Backend config** | `mode: velocity-bridge` | `mode: standalone` |
| **Commands fired on** | Each configured backend | The local server |

---

## 💬 Commands

| Command | Permission | Default | Description |
|---|---|---|---|
| `/link` | `link.link` | All players | Generates a linking code |
| `/link reload` | `link.reload` | OP | Reloads all config files and messages live |
| `/unlink` | `link.unlink` | All players | Unlinks your account |
| `/setup` | — | Discord admin | Posts the linking embed in the current channel |

---

## 📁 Project Structure

```
Link/
├── common/     # Shared logic — database, Discord bot, services, config
├── velocity/   # Velocity proxy plugin
├── spigot/     # Spigot plugin (1.21.4+)
└── folia/      # Folia plugin (region-threaded, 1.21.4+)
```

Output JARs after building:
- `velocity/build/libs/Link-<version>-velocity.jar`
- `spigot/build/libs/Link-<version>-spigot.jar`
- `folia/build/libs/Link-<version>-folia.jar`

---

## 🏗️ Building from Source

```bash
git clone https://github.com/huskydreaming/link.git
cd link
./gradlew build
```

The version is managed in a single place — the root `build.gradle.kts`:

```kotlin
version = "1.0.1"
```

Changing this one line updates the version in all built JARs and `plugin.yml` / `velocity-plugin.json` files automatically.

---

<div align="center">

Made with ❤️ by [HuskyDreaming](https://huskydreaming.com)

</div>
