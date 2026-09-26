package ir.mahan.lockpick;

import ir.mahan.lockpick.LockpickManager;
import ir.mahan.lockpick.api.LockpickAPI;
import ir.mahan.lockpick.commands.LockpickCommand;
import ir.mahan.lockpick.item.LockpickItemManager;
import ir.mahan.lockpick.listeners.PlayerListener;
import ir.mahan.lockpick.storage.DatabaseManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class LockpickPlugin
extends JavaPlugin {
    private DatabaseManager databaseManager;
    private LockpickManager lockpickManager;
    private LockpickItemManager lockpickItemManager;

    public void onEnable() {
        this.saveDefaultConfig();
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.connect();
        this.lockpickItemManager = new LockpickItemManager(this);
        this.lockpickManager = new LockpickManager(this);
        LockpickAPI.init(this.lockpickManager, this.databaseManager);
        this.getServer().getPluginManager().registerEvents((Listener)new PlayerListener(this), (Plugin)this);
        LockpickCommand lockpickCommand = new LockpickCommand(this);
        this.getCommand("lockpick").setExecutor((CommandExecutor)lockpickCommand);
        this.getCommand("lockpick").setTabCompleter((TabCompleter)lockpickCommand);
        this.getLogger().info("Moon Lockpick \u0641\u0639\u0627\u0644 \u0634\u062f.");
    }

    public void onDisable() {
        if (this.lockpickManager != null) {
            this.lockpickManager.cancelAll();
        }
        if (this.databaseManager != null) {
            this.databaseManager.disconnect();
        }
        this.getLogger().info("Moon Lockpick \u063a\u06cc\u0631\u0641\u0639\u0627\u0644 \u0634\u062f.");
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    public LockpickManager getLockpickManager() {
        return this.lockpickManager;
    }

    public LockpickItemManager getLockpickItemManager() {
        return this.lockpickItemManager;
    }
}
