package ir.mahan.lockpick.item;

import ir.mahan.lockpick.LockpickPlugin;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class LockpickItemManager {
    private final LockpickPlugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey usesKey;

    public LockpickItemManager(LockpickPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey((Plugin)plugin, "lockpick_item");
        this.usesKey = new NamespacedKey((Plugin)plugin, "lockpick_uses");
    }

    public int getMaxUses() {
        return Math.max(1, this.plugin.getConfig().getInt("lockpick-item.max-uses", 5));
    }

    public ItemStack createItem() {
        String materialName = this.plugin.getConfig().getString("lockpick-item.material", "TRIPWIRE_HOOK");
        Material material = Material.matchMaterial((String)materialName);
        if (material == null) {
            this.plugin.getLogger().warning("\u0645\u062a\u0631\u06cc\u0627\u0644 \u00ab" + materialName + "\u00bb \u0628\u0631\u0627\u06cc \u0622\u06cc\u062a\u0645 \u0644\u0627\u06a9\u200c\u067e\u06cc\u06a9 \u0646\u0627\u0645\u0639\u062a\u0628\u0631 \u0627\u0633\u062a\u060c \u0627\u0632 TRIPWIRE_HOOK \u0627\u0633\u062a\u0641\u0627\u062f\u0647 \u0645\u06cc\u200c\u0634\u0648\u062f.");
            material = Material.TRIPWIRE_HOOK;
        }
        ItemStack item = new ItemStack(material, 1);
        this.applyMeta(item, this.getMaxUses());
        return item;
    }

    private void applyMeta(ItemStack item, int uses) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        String name = this.plugin.getConfig().getString("lockpick-item.name", "\u00a7e\u0644\u0627\u06a9\u200c\u067e\u06cc\u06a9");
        meta.setDisplayName(this.translate(name));
        ArrayList<String> lore = new ArrayList<String>();
        for (String line : this.plugin.getConfig().getStringList("lockpick-item.lore")) {
            lore.add(this.translate(line));
        }
        String usesLine = this.plugin.getConfig().getString("lockpick-item.uses-lore-line", "\u00a77\u062f\u0648\u0627\u0645: \u00a7f{uses}/{max}");
        lore.add(this.translate(usesLine).replace("{uses}", String.valueOf(uses)).replace("{max}", String.valueOf(this.getMaxUses())));
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(this.itemKey, PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(this.usesKey, PersistentDataType.INTEGER, uses);
        item.setItemMeta(meta);
    }

    private String translate(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes((char)'&', (String)s);
    }

    public boolean isLockpickItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(this.itemKey, PersistentDataType.BYTE);
    }

    public int getRemainingUses(ItemStack item) {
        if (!this.isLockpickItem(item)) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        Integer uses = (Integer)meta.getPersistentDataContainer().get(this.usesKey, PersistentDataType.INTEGER);
        return uses != null ? uses : 0;
    }

    public boolean isHoldingUsableLockpick(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        return this.isLockpickItem(hand) && this.getRemainingUses(hand) > 0;
    }

    public boolean registerMiss(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!this.isLockpickItem(hand)) {
            return false;
        }
        int remaining = this.getRemainingUses(hand) - 1;
        if (remaining <= 0) {
            player.getInventory().setItemInMainHand(null);
            String breakMessage = this.plugin.getConfig().getString("lockpick-item.break-message", "\u00a7c\u0644\u0627\u06a9\u200c\u067e\u06cc\u06a9\u062a \u0634\u06a9\u0633\u062a!");
            player.sendMessage(this.translate(breakMessage));
            return true;
        }
        this.applyMeta(hand, remaining);
        player.getInventory().setItemInMainHand(hand);
        return false;
    }
}
