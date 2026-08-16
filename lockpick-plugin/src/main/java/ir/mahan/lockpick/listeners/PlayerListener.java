package ir.mahan.lockpick.listeners;

import ir.mahan.lockpick.LockpickPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerListener implements Listener {

    private final LockpickPlugin plugin;

    public PlayerListener(LockpickPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        boolean handled = plugin.getLockpickManager().handleInput(event.getPlayer());
        if (handled) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (plugin.getLockpickManager().hasActiveSession(event.getPlayer())) {
            plugin.getLockpickManager().cancelSession(event.getPlayer());
        }
    }
}
