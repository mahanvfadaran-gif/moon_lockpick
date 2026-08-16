package ir.mahan.lockpick.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LockpickResultEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String lockId;
    private final boolean success;
    private final int attempts;

    public LockpickResultEvent(Player player, String lockId, boolean success, int attempts) {
        this.player = player;
        this.lockId = lockId;
        this.success = success;
        this.attempts = attempts;
    }

    public Player getPlayer() {
        return player;
    }

    public String getLockId() {
        return lockId;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getAttempts() {
        return attempts;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
