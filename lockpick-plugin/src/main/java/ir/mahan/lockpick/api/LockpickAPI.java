package ir.mahan.lockpick.api;

import ir.mahan.lockpick.LockpickManager;
import ir.mahan.lockpick.storage.DatabaseManager;
import ir.mahan.lockpick.storage.LockData;
import org.bukkit.entity.Player;

import java.util.Map;

public class LockpickAPI {

    private static LockpickManager manager;
    private static DatabaseManager databaseManager;

    public static void init(LockpickManager m, DatabaseManager db) {
        manager = m;
        databaseManager = db;
    }

    public static boolean startLockpick(Player player, String lockId, double difficulty) {
        checkReady();
        return manager.startSession(player, lockId, difficulty);
    }

    public static boolean isLockpicking(Player player) {
        checkReady();
        return manager.hasActiveSession(player);
    }

    public static void forceFail(Player player) {
        checkReady();
        manager.cancelSession(player);
    }

    public static void registerLock(String lockId, double difficulty, int pinCount) {
        checkReady();
        databaseManager.saveLock(new LockData(lockId, difficulty, pinCount));
    }

    public static void removeLock(String lockId) {
        checkReady();
        databaseManager.deleteLock(lockId);
    }

    public static LockData getLock(String lockId) {
        checkReady();
        return databaseManager.getLock(lockId);
    }

    public static Map<String, LockData> getAllLocks() {
        checkReady();
        return databaseManager.getAllLocks();
    }

    private static void checkReady() {
        if (manager == null || databaseManager == null) {
            throw new IllegalStateException("LockpickAPI هنوز مقداردهی اولیه نشده است");
        }
    }
}
