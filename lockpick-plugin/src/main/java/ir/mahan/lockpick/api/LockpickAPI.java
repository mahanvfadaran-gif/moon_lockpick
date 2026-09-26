package ir.mahan.lockpick.api;

import ir.mahan.lockpick.LockpickManager;
import ir.mahan.lockpick.storage.DatabaseManager;
import ir.mahan.lockpick.storage.LockData;
import java.util.Map;
import org.bukkit.entity.Player;

public class LockpickAPI {
    private static LockpickManager manager;
    private static DatabaseManager databaseManager;

    public static void init(LockpickManager m, DatabaseManager db) {
        manager = m;
        databaseManager = db;
    }

    public static boolean startLockpick(Player player, String lockId, double difficulty) {
        LockpickAPI.checkReady();
        return manager.startSession(player, lockId, difficulty);
    }

    public static boolean isLockpicking(Player player) {
        LockpickAPI.checkReady();
        return manager.hasActiveSession(player);
    }

    public static void forceFail(Player player) {
        LockpickAPI.checkReady();
        manager.cancelSession(player);
    }

    public static void registerLock(String lockId, double difficulty, int pinCount) {
        LockpickAPI.checkReady();
        databaseManager.saveLock(new LockData(lockId, difficulty, pinCount));
    }

    public static void removeLock(String lockId) {
        LockpickAPI.checkReady();
        databaseManager.deleteLock(lockId);
    }

    public static LockData getLock(String lockId) {
        LockpickAPI.checkReady();
        return databaseManager.getLock(lockId);
    }

    public static Map<String, LockData> getAllLocks() {
        LockpickAPI.checkReady();
        return databaseManager.getAllLocks();
    }

    private static void checkReady() {
        if (manager == null || databaseManager == null) {
            throw new IllegalStateException("LockpickAPI \u0647\u0646\u0648\u0632 \u0645\u0642\u062f\u0627\u0631\u062f\u0647\u06cc \u0627\u0648\u0644\u06cc\u0647 \u0646\u0634\u062f\u0647 \u0627\u0633\u062a");
        }
    }
}
