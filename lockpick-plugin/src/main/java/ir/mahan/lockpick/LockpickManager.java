package ir.mahan.lockpick;

import ir.mahan.lockpick.LockpickPlugin;
import ir.mahan.lockpick.LockpickSession;
import ir.mahan.lockpick.api.events.LockpickResultEvent;
import ir.mahan.lockpick.api.events.LockpickStartEvent;
import ir.mahan.lockpick.storage.LockData;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class LockpickManager {
    private final LockpickPlugin plugin;
    private final Map<UUID, LockpickSession> activeSessions = new ConcurrentHashMap<UUID, LockpickSession>();

    public LockpickManager(LockpickPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean startSession(Player player, String lockId, double difficulty) {
        if (this.hasActiveSession(player)) {
            return false;
        }
        if (!this.plugin.getLockpickItemManager().isHoldingUsableLockpick(player)) {
            String message = this.plugin.getConfig().getString("lockpick-item.missing-message", "\u00a7c\u0628\u0631\u0627\u06cc \u0628\u0627\u0632 \u06a9\u0631\u062f\u0646 \u0642\u0641\u0644 \u0628\u0627\u06cc\u062f \u06cc\u06a9 \u0644\u0627\u06a9\u200c\u067e\u06cc\u06a9 \u0633\u0627\u0644\u0645 \u062f\u0631 \u062f\u0633\u062a \u062f\u0627\u0634\u062a\u0647 \u0628\u0627\u0634\u06cc.");
            player.sendMessage(message);
            return false;
        }
        LockpickStartEvent startEvent = new LockpickStartEvent(player, lockId, difficulty);
        this.plugin.getServer().getPluginManager().callEvent((Event)startEvent);
        if (startEvent.isCancelled()) {
            return false;
        }
        LockData lockData = this.plugin.getDatabaseManager().getLock(lockId);
        int pinCount = lockData != null ? lockData.getPinCount() : Math.max(1, (int)Math.round(difficulty * 5.0));
        LockpickSession session = new LockpickSession(this.plugin, player, lockId, pinCount, success -> this.finishSession(player, (boolean)success));
        this.activeSessions.put(player.getUniqueId(), session);
        session.start();
        return true;
    }

    private void finishSession(Player player, boolean success) {
        LockpickSession session = this.activeSessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }
        this.plugin.getDatabaseManager().recordAttempt(player.getUniqueId(), session.getLockId(), success);
        String message = success ? this.plugin.getConfig().getString("session.success-message", "\u00a7a\u0642\u0641\u0644 \u0628\u0627\u0632 \u0634\u062f!") : this.plugin.getConfig().getString("session.fail-message", "\u00a7c\u0627\u0634\u062a\u0628\u0627\u0647 \u0628\u0648\u062f\u060c \u062f\u0648\u0628\u0627\u0631\u0647 \u062a\u0644\u0627\u0634 \u06a9\u0646.");
        player.sendMessage(message);
        if (success) {
            this.dispatchSuccessCommand(player, session.getLockId());
        } else {
            this.plugin.getLockpickItemManager().registerMiss(player);
        }
        LockpickResultEvent resultEvent = new LockpickResultEvent(player, session.getLockId(), success, session.getAttempts());
        this.plugin.getServer().getPluginManager().callEvent((Event)resultEvent);
    }

    private void dispatchSuccessCommand(Player player, String lockId) {
        LockData lockData = this.plugin.getDatabaseManager().getLock(lockId);
        if (lockData == null || !lockData.hasSuccessCommand()) {
            return;
        }
        String command = lockData.getSuccessCommand().replace("%player%", player.getName());
        if (lockData.getCommandExecutor() == LockData.CommandExecutor.PLAYER) {
            player.performCommand(command);
        } else {
            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)command);
        }
    }

    public boolean handleInput(Player player) {
        LockpickSession session = this.activeSessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }
        session.attemptUnlock();
        return true;
    }

    public boolean hasActiveSession(Player player) {
        return this.activeSessions.containsKey(player.getUniqueId());
    }

    public void cancelSession(Player player) {
        LockpickSession session = this.activeSessions.get(player.getUniqueId());
        if (session != null) {
            session.cancel();
        }
    }

    public void cancelAll() {
        this.activeSessions.values().forEach(LockpickSession::cancel);
        this.activeSessions.clear();
    }
}
