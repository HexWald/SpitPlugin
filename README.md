# SpitPlugin 💦

**SpitPlugin** is a fun and lightweight Minecraft plugin for Spigot/Paper servers that allows players to spit like llamas 🦙 — perfect for adding chaos, humor, and unique interactions to your server.

---

## ✨ Features

* 💦 Player command to spit projectiles
* 🎯 Configurable damage system
* 🔊 Optional sound effects
* ⏱️ Cooldown system to prevent spam
* 🔒 Permission-based access
* ⚙️ Fully configurable via `config.yml`

---

## 📥 Installation

1. Download the plugin `.jar`
2. Place it in your server's `plugins` folder
3. Restart or reload the server
4. Done ✅

---

## 📜 Commands

| Command        | Description                 |
| -------------- | --------------------------- |
| `/spit`        | Shoot a spit projectile     |
| `/spit reload` | Reload plugin configuration |

---

## 🔐 Permissions

| Permission    | Description                 |
| ------------- | --------------------------- |
| `spit.use`    | Allows usage of `/spit`     |
| `spit.reload` | Allows reloading the config |

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Damage dealt by the spit
damage: 2

# Enable or disable sound effects
sound: true

# Cooldown in seconds
cooldown: 5

# Instantly kill target
kill: false
```

---

## 🧠 How It Works

When a player uses `/spit`:

* A projectile (llama spit) is launched forward
* On impact:

  * Deals damage (if enabled)
  * Plays sound effects (optional)

---

## 📌 Requirements

* Java 17+
* Spigot or Paper server

---

## 🛠️ Build

```bash
mvn clean package
```

---

## 🚀 Future Ideas

* Status effects (poison, slowness)
* PvP-focused mechanics
* Custom spit types
* GUI configuration

---

🤝 CONTRIBUTING

Contributions are welcome!

If you have ideas, improvements, or fixes:

* Fork the repository
* Make your changes
* Submit a pull request

All contributions are appreciated 💙


---

## ⭐ Support

If you like this plugin, consider giving it a ⭐ on GitHub!
