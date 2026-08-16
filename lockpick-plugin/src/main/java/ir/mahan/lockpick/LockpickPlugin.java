package ir.mahan.lockpick;

import ir.mahan.lockpick.api.LockpickAPI;
import ir.mahan.lockpick.commands.LockpickCommand;
import ir.mahan.lockpick.item.LockpickItemManager;
import ir.mahan.lockpick.listeners.PlayerListener;
import ir.mahan.lockpick.storage.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LockpickPlugin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private LockpickManager lockpickManager;
    private LockpickItemManager lockpickItemManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        lockpickItemManager = new LockpickItemManager(this);
        lockpickManager = new LockpickManager(this);
        LockpickAPI.init(lockpickManager, databaseManager);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getCommand("lockpick").setExecutor(new LockpickCommand(this));

        getLogger().info("Moon Lockpick فعال شد.");
    }

    @Override
    public void onDisable() {
        if (lockpickManager != null) {
            lockpickManager.cancelAll();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        getLogger().info("Moon Lockpick غیرفعال شد.");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public LockpickManager getLockpickManager() {
        return lockpickManager;
    }

    public LockpickItemManager getLockpickItemManager() {
        return lockpickItemManager;
    }
}
