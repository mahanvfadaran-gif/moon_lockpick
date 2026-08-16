package ir.mahan.lockpick.item;

import ir.mahan.lockpick.LockpickPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the physical "lockpick" tool item: building it from config, checking
 * whether a given ItemStack is a valid (non-broken) lockpick, and managing its
 * remaining-uses counter (stored in the item's PersistentDataContainer since the
 * item does not need to be a vanilla Damageable material).
 */
public class LockpickItemManager {

    private final LockpickPlugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey usesKey;

    public LockpickItemManager(LockpickPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "lockpick_item");
        this.usesKey = new NamespacedKey(plugin, "lockpick_uses");
    }

    public int getMaxUses() {
        return Math.max(1, plugin.getConfig().getInt("lockpick-item.max-uses", 5));
    }

    public ItemStack createItem() {
        String materialName = plugin.getConfig().getString("lockpick-item.material", "TRIPWIRE_HOOK");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("متریال «" + materialName + "» برای آیتم لاک‌پیک نامعتبر است، از TRIPWIRE_HOOK استفاده می‌شود.");
            material = Material.TRIPWIRE_HOOK;
        }

        ItemStack item = new ItemStack(material, 1);
        applyMeta(item, getMaxUses());
        return item;
    }

    private void applyMeta(ItemStack item, int uses) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        String name = plugin.getConfig().getString("lockpick-item.name", "§eلاک‌پیک");
        meta.setDisplayName(translate(name));

        List<String> lore = new ArrayList<>();
        for (String line : plugin.getConfig().getStringList("lockpick-item.lore")) {
            lore.add(translate(line));
        }
        String usesLine = plugin.getConfig().getString("lockpick-item.uses-lore-line", "§7دوام: §f{uses}/{max}");
        lore.add(translate(usesLine)
                .replace("{uses}", String.valueOf(uses))
                .replace("{max}", String.valueOf(getMaxUses())));
        meta.setLore(lore);

        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(usesKey, PersistentDataType.INTEGER, uses);

        item.setItemMeta(meta);
    }

    private String translate(String s) {
        return s == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    public boolean isLockpickItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }

    public int getRemainingUses(ItemStack item) {
        if (!isLockpickItem(item)) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        Integer uses = meta.getPersistentDataContainer().get(usesKey, PersistentDataType.INTEGER);
        return uses != null ? uses : 0;
    }

    public boolean isHoldingUsableLockpick(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        return isLockpickItem(hand) && getRemainingUses(hand) > 0;
    }

    public boolean registerMiss(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!isLockpickItem(hand)) {
            return false;
        }

        int remaining = getRemainingUses(hand) - 1;
        if (remaining <= 0) {
            player.getInventory().setItemInMainHand(null);
            String breakMessage = plugin.getConfig().getString("lockpick-item.break-message", "§cلاک‌پیکت شکست!");
            player.sendMessage(translate(breakMessage));
            return true;
        }

        applyMeta(hand, remaining);
        player.getInventory().setItemInMainHand(hand);
        return false;
    }
}
