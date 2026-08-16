# 🌙 Moon Lockpick

A lightweight and extensible **Minecraft Paper plugin** that adds an interactive lockpicking system to your server.

Moon Lockpick lets server owners create custom locks with different difficulties, give players lockpicks, track player statistics, and execute commands when a lock is successfully picked.

## ✨ Features

- 🔐 Custom lock creation with unique IDs
- 🎯 Interactive lockpicking mini-game
- 📊 Configurable difficulty and pin count
- 🧰 Custom Lockpick items
- 🎁 Execute a command after a successful lockpick
- 👤 Commands can be executed as the player or from console
- 📈 Per-player lockpick statistics
- 💾 SQLite database storage
- 🔌 Public API for other plugins
- 📢 Custom lockpick events
- ⚙️ Reloadable configuration
- 🧩 Modular Java architecture

## 📦 Installation

1. Build or download the plugin JAR.
2. Put the JAR into your server's `plugins` folder.
3. Start or restart the server.
4. Configure `config.yml` if required.
5. Players can use the lockpicking system with the required Lockpick item.

## ⚙️ Requirements

- **Minecraft:** 1.20.4+
- **Server:** Paper
- **Java:** 17+
- **Maven:** 3.8+

Optional integrations are supported through soft dependencies such as **WorldGuard**, **ItemsAdder**, and **Vault**.

## 🛠️ Build from Source

Clone the repository and run:

```bash
mvn clean package
```

The compiled plugin will be available in the `target` directory.

## 🎮 Commands

All administrative commands use the `/lockpick` command.

### Create a Lock

```text
/lockpick create <id> <difficulty> <pinCount> [player|console] [command...]
```

Creates a new lock.

**Example:**

```text
/lockpick create bank_vault 0.35 5 console give %player% diamond 1
```

- `id` — Unique ID of the lock.
- `difficulty` — Difficulty value from `0` to `1`.
- `pinCount` — Number of pins used by the lock.
- `player` / `console` — Optional command executor.
- `command` — Optional command executed when the lock is successfully picked.

### Remove a Lock

```text
/lockpick remove <id>
```

Deletes a lock from the database.

**Example:**

```text
/lockpick remove bank_vault
```

### Give a Lockpick

```text
/lockpick give <player> [amount]
```

Gives a player one or more Lockpick items.

**Examples:**

```text
/lockpick give Mahan
/lockpick give Mahan 5
```

### View Lock Statistics

```text
/lockpick stats <id>
```

Shows the current player's attempts and successful lockpicks for a specific lock.

**Example:**

```text
/lockpick stats bank_vault
```

### Test the Lockpicking Mini-game

```text
/lockpick test [lockId] [difficulty]
```

Starts a lockpicking session for testing purposes.

**Examples:**

```text
/lockpick test
/lockpick test bank_vault
/lockpick test bank_vault 0.3
```

### Reload Configuration

```text
/lockpick reload
```

Reloads the plugin configuration.

## 🔑 Permissions

| Permission | Default | Description |
|---|---|---|
| `lockpick.admin` | OP | Access to administrative `/lockpick` commands |
| `lockpick.use` | Everyone | Allows players to use the lockpicking mini-game |

### Example Permission Setup

With a permissions plugin such as LuckPerms, you can grant administrative access with:

```text
/lp user <player> permission set lockpick.admin true
```

## 🔌 API & Events

Moon Lockpick includes a public API for integration with other plugins.

Available events include:

- `LockpickStartEvent` — Fired when a lockpicking session starts.
- `LockpickResultEvent` — Fired when a lockpicking session produces a result.

This allows developers to connect rewards, custom messages, sounds, permissions, statistics, or other gameplay systems to the lockpicking process.

## 💾 Data Storage

Moon Lockpick uses **SQLite** to persist lock and player statistics data, allowing information to survive server restarts.

## 📁 Project Structure

```text
lockpick-plugin/
├── src/main/java/ir/mahan/lockpick/
│   ├── api/
│   │   └── events/
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

## 🧑‍💻 Development

The project is designed with separate managers for lockpicking, items, storage, commands, listeners, and API functionality. This makes it easier to extend the plugin with new lock types, rewards, integrations, and gameplay mechanics.

## 📌 Status

Moon Lockpick is currently under development. Features, commands, and APIs may evolve in future releases.

## 👤 Author

**Mahan Vafadaran**

## ⭐ Support

If you find Moon Lockpick useful, consider starring the repository on GitHub and sharing it with other Minecraft server owners.
