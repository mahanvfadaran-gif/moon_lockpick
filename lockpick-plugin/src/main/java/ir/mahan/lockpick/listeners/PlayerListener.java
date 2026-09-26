package ir.mahan.lockpick.listeners;

import ir.mahan.lockpick.LockpickPlugin;
import ir.mahan.lockpick.storage.LockData;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerListener
implements Listener {
    private final LockpickPlugin plugin;

    public PlayerListener(LockpickPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        boolean handled = this.plugin.getLockpickManager().handleInput(event.getPlayer());
        if (handled) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        boolean handledInput = this.plugin.getLockpickManager().handleInput(player);
        if (handledInput) {
            event.setCancelled(true);
            return;
        }
        if (action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        LockData lockData = this.plugin.getDatabaseManager().getLockByLocation(clickedBlock.getWorld().getName(), clickedBlock.getX(), clickedBlock.getY(), clickedBlock.getZ());
        if (lockData == null) {
            return;
        }
        event.setCancelled(true);
        this.plugin.getLockpickManager().startSession(player, lockData.getId(), lockData.getDifficulty());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.plugin.getLockpickManager().hasActiveSession(event.getPlayer())) {
            this.plugin.getLockpickManager().cancelSession(event.getPlayer());
        }
    }
}
