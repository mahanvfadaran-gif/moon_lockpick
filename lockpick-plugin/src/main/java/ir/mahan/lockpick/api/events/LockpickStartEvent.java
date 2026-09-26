package ir.mahan.lockpick.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LockpickStartEvent
extends Event
implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final String lockId;
    private final double difficulty;
    private boolean cancelled;

    public LockpickStartEvent(Player player, String lockId, double difficulty) {
        this.player = player;
        this.lockId = lockId;
        this.difficulty = difficulty;
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getLockId() {
        return this.lockId;
    }

    public double getDifficulty() {
        return this.difficulty;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
