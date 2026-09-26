package ir.mahan.lockpick.storage;

import ir.mahan.lockpick.LockpickPlugin;
import ir.mahan.lockpick.storage.LockData;
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
            File dbFile = new File(this.plugin.getDataFolder(), this.plugin.getConfig().getString("database.file", "lockpick.db"));
            if (!this.plugin.getDataFolder().exists()) {
                this.plugin.getDataFolder().mkdirs();
            }
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            this.createTables();
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, "\u0627\u062a\u0635\u0627\u0644 \u0628\u0647 \u062f\u06cc\u062a\u0627\u0628\u06cc\u0633 SQLite \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
    }

    public void disconnect() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u0628\u0633\u062a\u0646 \u0627\u062a\u0635\u0627\u0644 \u062f\u06cc\u062a\u0627\u0628\u06cc\u0633 \u0628\u0627 \u062e\u0637\u0627 \u0645\u0648\u0627\u062c\u0647 \u0634\u062f", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement statement = this.connection.createStatement();){
            statement.execute("    CREATE TABLE IF NOT EXISTS locks (\n        id TEXT PRIMARY KEY,\n        difficulty REAL NOT NULL,\n        pin_count INTEGER NOT NULL,\n        success_command TEXT,\n        command_executor TEXT,\n        world TEXT,\n        block_x INTEGER,\n        block_y INTEGER,\n        block_z INTEGER\n    )\n");
            statement.execute("    CREATE TABLE IF NOT EXISTS lock_stats (\n        player_uuid TEXT NOT NULL,\n        lock_id TEXT NOT NULL,\n        attempts INTEGER NOT NULL DEFAULT 0,\n        successes INTEGER NOT NULL DEFAULT 0,\n        PRIMARY KEY (player_uuid, lock_id)\n    )\n");
        }
        this.migrateLocksTable();
    }

    private void migrateLocksTable() throws SQLException {
        boolean hasSuccessCommand = false;
        boolean hasCommandExecutor = false;
        boolean hasWorld = false;
        boolean hasBlockX = false;
        boolean hasBlockY = false;
        boolean hasBlockZ = false;
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(locks)");){
            while (result.next()) {
                String columnName = result.getString("name");
                if ("success_command".equals(columnName)) {
                    hasSuccessCommand = true;
                    continue;
                }
                if ("command_executor".equals(columnName)) {
                    hasCommandExecutor = true;
                    continue;
                }
                if ("world".equals(columnName)) {
                    hasWorld = true;
                    continue;
                }
                if ("block_x".equals(columnName)) {
                    hasBlockX = true;
                    continue;
                }
                if ("block_y".equals(columnName)) {
                    hasBlockY = true;
                    continue;
                }
                if (!"block_z".equals(columnName)) continue;
                hasBlockZ = true;
            }
        }
        Statement statement = this.connection.createStatement();
        try {
            if (!hasSuccessCommand) {
                statement.execute("ALTER TABLE locks ADD COLUMN success_command TEXT");
            }
            if (!hasCommandExecutor) {
                statement.execute("ALTER TABLE locks ADD COLUMN command_executor TEXT");
            }
            if (!hasWorld) {
                statement.execute("ALTER TABLE locks ADD COLUMN world TEXT");
            }
            if (!hasBlockX) {
                statement.execute("ALTER TABLE locks ADD COLUMN block_x INTEGER");
            }
            if (!hasBlockY) {
                statement.execute("ALTER TABLE locks ADD COLUMN block_y INTEGER");
            }
            if (!hasBlockZ) {
                statement.execute("ALTER TABLE locks ADD COLUMN block_z INTEGER");
            }
        }
        finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public void saveLock(LockData lock) {
        String sql = "INSERT INTO locks (id, difficulty, pin_count, success_command, command_executor, world, block_x, block_y, block_z) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET difficulty = excluded.difficulty, pin_count = excluded.pin_count, success_command = excluded.success_command, command_executor = excluded.command_executor, world = excluded.world, block_x = excluded.block_x, block_y = excluded.block_y, block_z = excluded.block_z";
        try (PreparedStatement statement = this.connection.prepareStatement(sql);){
            statement.setString(1, lock.getId());
            statement.setDouble(2, lock.getDifficulty());
            statement.setInt(3, lock.getPinCount());
            statement.setString(4, lock.getSuccessCommand());
            statement.setString(5, lock.getCommandExecutor() != null ? lock.getCommandExecutor().name() : null);
            statement.setString(6, lock.getWorldName());
            this.setNullableInt(statement, 7, lock.getBlockX());
            this.setNullableInt(statement, 8, lock.getBlockY());
            this.setNullableInt(statement, 9, lock.getBlockZ());
            statement.executeUpdate();
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u0630\u062e\u06cc\u0631\u0647 \u0642\u0641\u0644 \u062f\u0631 \u062f\u06cc\u062a\u0627\u0628\u06cc\u0633 \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value != null) {
            statement.setInt(index, value);
        } else {
            statement.setNull(index, java.sql.Types.INTEGER);
        }
    }

    public void deleteLock(String id) {
        try (PreparedStatement statement = this.connection.prepareStatement("DELETE FROM locks WHERE id = ?");){
            statement.setString(1, id);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u062d\u0630\u0641 \u0642\u0641\u0644 \u0627\u0632 \u062f\u06cc\u062a\u0627\u0628\u06cc\u0633 \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
    }

    public LockData getLock(String id) {
        String sql = "SELECT id, difficulty, pin_count, success_command, command_executor, world, block_x, block_y, block_z FROM locks WHERE id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql);){
            statement.setString(1, id);
            try (ResultSet result = statement.executeQuery();){
                if (!result.next()) return null;
                LockData lockData = this.readLock(result);
                return lockData;
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u062e\u0648\u0627\u0646\u062f\u0646 \u0642\u0641\u0644 \u0627\u0632 \u062f\u06cc\u062a\u0627\u0628\u06cc\u0633 \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
        return null;
    }

    public LockData getLockByLocation(String world, int x, int y, int z) {
        String sql = "SELECT id, difficulty, pin_count, success_command, command_executor, world, block_x, block_y, block_z FROM locks WHERE world = ? AND block_x = ? AND block_y = ? AND block_z = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql);){
            statement.setString(1, world);
            statement.setInt(2, x);
            statement.setInt(3, y);
            statement.setInt(4, z);
            try (ResultSet result = statement.executeQuery();){
                if (!result.next()) return null;
                return this.readLock(result);
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u062e\u0648\u0627\u0646\u062f\u0646 \u0642\u0641\u0644 \u0628\u0631 \u0627\u0633\u0627\u0633 \u0645\u0648\u0642\u0639\u06cc\u062a \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
        return null;
    }

    public Map<String, LockData> getAllLocks() {
        HashMap<String, LockData> locks = new HashMap<String, LockData>();
        String sql = "SELECT id, difficulty, pin_count, success_command, command_executor, world, block_x, block_y, block_z FROM locks";
        try (Statement statement = this.connection.createStatement();
             ResultSet result = statement.executeQuery(sql);){
            while (result.next()) {
                LockData lock = this.readLock(result);
                locks.put(lock.getId(), lock);
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u062e\u0648\u0627\u0646\u062f\u0646 \u0644\u06cc\u0633\u062a \u0642\u0641\u0644\u200c\u0647\u0627 \u0627\u0632 \u062f\u06cc\u062a\u0627\u0628\u06cc\u0633 \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
        return locks;
    }

    private LockData readLock(ResultSet result) throws SQLException {
        String successCommand = result.getString("success_command");
        String executorName = result.getString("command_executor");
        LockData.CommandExecutor executor = executorName != null ? LockData.CommandExecutor.valueOf(executorName) : null;
        String world = result.getString("world");
        Integer x = (Integer) result.getObject("block_x");
        Integer y = (Integer) result.getObject("block_y");
        Integer z = (Integer) result.getObject("block_z");
        return new LockData(result.getString("id"), result.getDouble("difficulty"), result.getInt("pin_count"), successCommand, executor, world, x, y, z);
    }

    public void recordAttempt(UUID playerId, String lockId, boolean success) {
        String sql = "INSERT INTO lock_stats (player_uuid, lock_id, attempts, successes) VALUES (?, ?, 1, ?) ON CONFLICT(player_uuid, lock_id) DO UPDATE SET attempts = attempts + 1, successes = successes + excluded.successes";
        try (PreparedStatement statement = this.connection.prepareStatement(sql);){
            statement.setString(1, playerId.toString());
            statement.setString(2, lockId);
            statement.setInt(3, success ? 1 : 0);
            statement.executeUpdate();
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u062b\u0628\u062a \u0622\u0645\u0627\u0631 \u062a\u0644\u0627\u0634 \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public int[] getStats(UUID playerId, String lockId) {
        String sql = "SELECT attempts, successes FROM lock_stats WHERE player_uuid = ? AND lock_id = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql);){
            statement.setString(1, playerId.toString());
            statement.setString(2, lockId);
            try (ResultSet result = statement.executeQuery();){
                if (!result.next()) return new int[]{0, 0};
                int[] nArray = new int[]{result.getInt("attempts"), result.getInt("successes")};
                return nArray;
            }
        }
        catch (SQLException e) {
            this.plugin.getLogger().log(Level.WARNING, "\u062e\u0648\u0627\u0646\u062f\u0646 \u0622\u0645\u0627\u0631 \u067e\u0644\u06cc\u0631 \u0646\u0627\u0645\u0648\u0641\u0642 \u0628\u0648\u062f", e);
        }
        return new int[]{0, 0};
    }
}
