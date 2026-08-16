package ir.mahan.lockpick;

import ir.mahan.lockpick.api.events.LockpickResultEvent;
import ir.mahan.lockpick.api.events.LockpickStartEvent;
import ir.mahan.lockpick.storage.LockData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LockpickManager {

    private final LockpickPlugin plugin;
    private final Map<UUID, LockpickSession> activeSessions = new ConcurrentHashMap<>();

    public LockpickManager(LockpickPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean startSession(Player player, String lockId, double difficulty) {
        if (hasActiveSession(player)) {
            return false;
        }

        if (!plugin.getLockpickItemManager().isHoldingUsableLockpick(player)) {
            String message = plugin.getConfig().getString("lockpick-item.missing-message",
                    "§cبرای باز کردن قفل باید یک لاک‌پیک سالم در دست داشته باشی.");
            player.sendMessage(message);
            return false;
        }

        LockpickStartEvent startEvent = new LockpickStartEvent(player, lockId, difficulty);
        plugin.getServer().getPluginManager().callEvent(startEvent);
        if (startEvent.isCancelled()) {
            return false;
        }

        LockData lockData = plugin.getDatabaseManager().getLock(lockId);
        int pinCount = lockData != null ? lockData.getPinCount() : Math.max(1, (int) Math.round(difficulty * 5));

        LockpickSession session = new LockpickSession(plugin, player, lockId, pinCount, success -> finishSession(player, success));
        activeSessions.put(player.getUniqueId(), session);
        session.start();
        return true;
    }

    private void finishSession(Player player, boolean success) {
        LockpickSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }

        plugin.getDatabaseManager().recordAttempt(player.getUniqueId(), session.getLockId(), success);

        String message = success
                ? plugin.getConfig().getString("session.success-message", "§aقفل باز شد!")
                : plugin.getConfig().getString("session.fail-message", "§cاشتباه بود، دوباره تلاش کن.");
        player.sendMessage(message);

        if (success) {
            dispatchSuccessCommand(player, session.getLockId());
        } else {
            plugin.getLockpickItemManager().registerMiss(player);
        }

        LockpickResultEvent resultEvent = new LockpickResultEvent(player, session.getLockId(), success, session.getAttempts());
        plugin.getServer().getPluginManager().callEvent(resultEvent);
    }

    private void dispatchSuccessCommand(Player player, String lockId) {
        LockData lockData = plugin.getDatabaseManager().getLock(lockId);
        if (lockData == null || !lockData.hasSuccessCommand()) {
            return;
        }

        String command = lockData.getSuccessCommand().replace("%player%", player.getName());
        if (lockData.getCommandExecutor() == LockData.CommandExecutor.PLAYER) {
            player.performCommand(command);
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public boolean handleInput(Player player) {
        LockpickSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }
        session.attemptUnlock();
        return true;
    }

    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    public void cancelSession(Player player) {
        LockpickSession session = activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.cancel();
        }
    }

    public void cancelAll() {
        activeSessions.values().forEach(LockpickSession::cancel);
        activeSessions.clear();
    }
}
