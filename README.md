# 🌙 Moon Lockpick

**Moon Lockpick** is a Minecraft Paper plugin that adds a lockpicking system to your server, allowing players to interact with locked containers through a dedicated lockpicking mechanic.

## ✨ Features

- 🔐 Lockpicking system for containers
- 🎯 Interactive lockpicking sessions
- 🧰 Custom Lockpick item management
- 💾 SQLite database storage for lock data
- 🔌 Public API for integrating lockpicking with other plugins
- 📢 Custom events for lockpick start and result
- ⚙️ Configurable plugin settings
- 🧩 Clean and modular Java architecture

## 📦 Installation

1. Download or build the plugin JAR.
2. Place the JAR file into your server's `plugins` folder.
3. Start or restart your Paper server.
4. Configure the plugin through `config.yml` if needed.

## 🔨 Building from Source

The project uses Maven and Java 17+.

```bash
mvn clean package
```

The compiled JAR will be generated in the `target` directory.

## ⚙️ Requirements

- **Minecraft:** 1.20.4+
- **Server:** Paper
- **Java:** 17+
- **Maven:** 3.8+

## 📁 Project Structure

```text
lockpick-plugin/
├── src/main/java/ir/mahan/lockpick/
│   ├── api/
│   ├── commands/
│   ├── item/
│   ├── listeners/
│   ├── storage/
│   ├── LockpickManager.java
│   ├── LockpickPlugin.java
│   └── LockpickSession.java
├── src/main/resources/
│   ├── config.yml
│   └── plugin.yml
└── pom.xml
```

## 🔌 API

Moon Lockpick provides an API that other plugins can use to interact with the lockpicking system.

It also provides events such as:

- `LockpickStartEvent`
- `LockpickResultEvent`

These events can be used to integrate custom gameplay, rewards, permissions, sounds, or other server mechanics with the lockpicking process.

## 💾 Storage

Lock and player-related data can be persisted using SQLite, making the plugin suitable for servers that need data to survive restarts.

## 🛠️ Development

The project is structured to make future features and integrations easier to add. Developers can extend the API, storage layer, commands, and lockpicking mechanics without having to rewrite the entire system.

## 📌 Status

Moon Lockpick is an actively developed project. Features and mechanics may change as development continues.

## 👤 Author

**Mahan Vafadaran**

---

⭐ If you find Moon Lockpick useful, consider starring the repository on GitHub!
