<div align="center">

# 🔗 Link

**Bridge your Minecraft players to Discord — seamlessly.**

[![Version](https://img.shields.io/badge/version-1.0.0-6C63FF?style=for-the-badge&logo=github)](https://github.com/huskydreaming/link/releases)
[![Java](https://img.shields.io/badge/java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Velocity](https://img.shields.io/badge/velocity-3.5.0-0080FF?style=for-the-badge)](https://papermc.io/software/velocity)
[![Spigot](https://img.shields.io/badge/spigot-1.21.4-FF6B35?style=for-the-badge)](https://www.spigotmc.org/)
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
- 🌐 **Two deployment modes** — Velocity proxy or standalone Spigot

---

## 📋 Requirements

| Requirement | Version |
|---|---|
| Java | 21+ |
| Velocity *(proxy mode)* | 3.5.0+ |
| Spigot / Paper *(standalone mode)* | 1.21.4+ |
| MariaDB | 10.6+ |

---

## 🤖 Creating a Discord Bot

Before installing the plugin you need a Discord application.

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications) and click **New Application**
2. Name it (e.g. `Link`) and hit **Create**
3. Open the **Bot** tab → click **Add Bot**
4. Under **Privileged Gateway Intents** enable **Server Members Intent**
5. Click **Reset Token** and copy your token — you'll need it in `config.yml`
6. Open the **OAuth2 → URL Generator** tab
   - Scopes: `bot`, `applications.commands`
   - Bot Permissions: `Manage Roles`, `Send Messages`, `Read Messages/View Channels`
7. Copy the generated URL, open it in your browser and invite the bot to your server
8. In your Discord server: make sure the bot's role is **above** any role it needs to assign

---

## 📦 Installation

### Option A — Velocity Proxy *(recommended for networks)*

This is the standard setup. The bot and database live on the **Velocity proxy** only. Spigot servers are lightweight receivers.

**Step 1 — Velocity proxy**
1. Drop `Link.jar` into your Velocity `plugins/` folder
2. Start the server once to generate `plugins/link/config.yml` and `messages.yml`
3. Fill in your database credentials, Discord token, guild ID, and role ID in `config.yml`
4. Restart Velocity

**Step 2 — Each Spigot / Paper backend**
1. Drop `Link.jar` into the Spigot `plugins/` folder
2. Start the server once to generate `plugins/link/config.yml`
3. Ensure `mode` is set to `velocity-bridge` (this is the default)
4. Restart the backend server

That's it. The proxy handles everything — the backend just listens for plugin messages and fires the commands you configure.

---

### Option B — Standalone Spigot *(no proxy)*

The full bot, database and commands run directly on a single Spigot server.

1. Drop `Link.jar` into your Spigot `plugins/` folder
2. Start the server once to generate the config files
3. Open `plugins/link/config.yml` and set:
   ```yaml
   mode: standalone
   ```
4. Fill in your database credentials, Discord token, guild ID, and role ID
5. Restart the server

---

## ⚙️ Configuration

### `config.yml` — Velocity

```yaml
database:
  host: "localhost"
  port: 3306
  name: "link"
  username: "user"
  password: "password"

discord:
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

link:
  cooldown: 3600        # seconds a player must wait before re-linking
  servers:
    survival:           # must match your Velocity backend server name
      link-commands:
        - "lp user %player% parent add member"
        - "eco give %player% 100"
      unlink-commands:
        - "lp user %player% parent remove member"
```

### `config.yml` — Spigot (standalone mode)

```yaml
mode: standalone

discord:
  token: "YOUR_BOT_TOKEN"
  guild-id: 123456789012345678
  role-id: 123456789012345678

link:
  cooldown: 3600
  link-commands:
    - "lp user %player% parent add member"
  unlink-commands:
    - "lp user %player% parent remove member"
```

> `%player%` is replaced with the player's username in all commands.

---

## 🔄 Velocity-Bridge vs Standalone

| | Velocity-Bridge | Standalone |
|---|---|---|
| **Where bot runs** | Velocity proxy | Spigot server |
| **Database** | Proxy only | Spigot server |
| **Best for** | Networks with multiple backends | Single-server setups |
| **Spigot config** | `mode: velocity-bridge` | `mode: standalone` |
| **Commands fired on** | Each configured backend | The local Spigot server |

---

## 💬 Commands

| Command | Platform | Description |
|---|---|---|
| `/link` | In-game | Generates a linking code |
| `/unlink` | In-game | Unlinks your account |
| `/setup` | Discord | Posts the linking embed in the current channel *(admin only)* |

---

## 📁 Project Structure

```
Link/
├── common/      # Shared logic — database, Discord bot, services, config
├── velocity/    # Velocity proxy plugin
└── spigotmc/    # Spigot / Paper plugin
```

---

## 🏗️ Building from Source

```bash
git clone https://github.com/huskydreaming/link.git
cd link
./gradlew build
```

Output JARs are placed in `velocity/build/libs/` and `spigotmc/build/libs/`.

---

<div align="center">

Made with ❤️ by [HuskyDreaming](https://huskydreaming.com)

</div>

