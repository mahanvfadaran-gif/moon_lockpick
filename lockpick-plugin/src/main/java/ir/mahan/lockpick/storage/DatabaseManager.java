package ir.mahan.lockpick.storage;

import ir.mahan.lockpick.LockpickPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final LockpickPlugin plugin;
    private Connection connection;

    public DatabaseManager(LockpickPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dbFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("database.file", "lockpick.db"));
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "اتصال به دیتابیس SQLite ناموفق بود", e);
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "بستن اتصال دیتابیس با خطا مواجه شد", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                CREATE TABLE IF NOT EXISTS locks (
                    id TEXT PRIMARY KEY,
                    difficulty REAL NOT NULL,
                    pin_count INTEGER NOT NULL,
                    success_command TEXT,
                    command_executor TEXT
                )
            """);
            statement.execute("""
                CREATE TABLE IF NOT EXISTS lock_stats (
                    player_uuid TEXT NOT NULL,
                    lock_id TEXT NOT NULL,
                    attempts INTEGER NOT NULL DEFAULT 0,
                    successes INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (player_uuid, lock_id)
                )
            """);
        }
        migrateLocksTable();
    }

    private void migrateLocksTable() throws SQLException {
        boolean hasSuccessCommand = false;
        boolean hasCommandExecutor = false;
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(locks)")) {
            while (result.next()) {
                String columnName = result.getString("name");
                if ("success_command".equals(columnName)) {
                    hasSuccessCommand = true;
                } else if ("command_executor".equals(columnName)) {
                    hasCommandExecutor = true;
                }
            }
        }
        try (Statement statement = connection.createStatement()) {
            if (!hasSuccessCommand) {
                statement.execute("ALTER TABLE locks ADD COLUMN success_command TEXT");
            }
            if (!hasCommandExecutor) {
                statement.execute("ALTER TABLE locks ADD COLUMN command_executor TEXT");
            }
        }
    }

    public void saveLock(LockData lock) {
        String sql = "INSERT INTO locks (id, difficulty, pin_count, success_command, command_executor) VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT(id) DO UPDATE SET difficulty = excluded.difficulty, pin_count = excluded.pin_count, " +
                "success_command = excluded.success_command, command_executor = excluded.command_executor";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, lock.getId());
            statement.setDouble(2, lock.getDifficulty());
            statement.setInt(3, lock.getPinCount());
            statement.setString(4, lock.getSuccessCommand());
            statement.setString(5, lock.getCommandExecutor() != null ? lock.getCommandExecutor().name() : null);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "ذخیره قفل در دیتابیس ناموفق بود", e);
        }
    }

    public void deleteLock(String id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM locks WHERE id = ?")) {
            statement.setString(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "حذف قفل از دیتابیس ناموفق بود", e);
        }
    }

    public LockData getLock(String id) {
        String sql = "SELECT id, difficulty, pin_count, success_command, command_executor FROM locks WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return readLock(result);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "خواندن قفل از دیتابیس ناموفق بود", e);
        }
        return null;
    }

    public Map<String, LockData> getAllLocks() {
        Map<String, LockData> locks = new HashMap<>();
        String sql = "SELECT id, difficulty, pin_count, success_command, command_executor FROM locks";
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            while (result.next()) {
                LockData lock = readLock(result);
                locks.put(lock.getId(), lock);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "خواندن لیست قفل‌ها از دیتابیس ناموفق بود", e);
        }
        return locks;
    }

    private LockData readLock(ResultSet result) throws SQLException {
        String successCommand = result.getString("success_command");
        String executorName = result.getString("command_executor");
        LockData.CommandExecutor executor = executorName != null ? LockData.CommandExecutor.valueOf(executorName) : null;
        return new LockData(result.getString("id"), result.getDouble("difficulty"), result.getInt("pin_count"),
                successCommand, executor);
    }

    public void recordAttempt(UUID playerId, String lockId, boolean success) {
        String sql = "INSERT INTO lock_stats (player_uuid, lock_id, attempts, successes) VALUES (?, ?, 1, ?) " +
                "ON CONFLICT(player_uuid, lock_id) DO UPDATE SET " +
                "attempts = attempts + 1, successes = successes + excluded.successes";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, lockId);
            statement.setInt(3, success ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "ثبت آمار تلاش ناموفق بود", e);
        }
    }

    public int[] getStats(UUID playerId, String lockId) {
        String sql = "SELECT attempts, successes FROM lock_stats WHERE player_uuid = ? AND lock_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            statement.setString(2, lockId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new int[]{result.getInt("attempts"), result.getInt("successes")};
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "خواندن آمار پلیر ناموفق بود", e);
        }
        return new int[]{0, 0};
    }
}
