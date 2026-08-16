package ir.mahan.lockpick.commands;

import ir.mahan.lockpick.LockpickPlugin;
import ir.mahan.lockpick.storage.LockData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LockpickCommand implements CommandExecutor {

    private final LockpickPlugin plugin;

    public LockpickCommand(LockpickPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7استفاده: /lockpick <create|remove|give|stats|test|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "give" -> handleGive(sender, args);
            case "stats" -> handleStats(sender, args);
            case "test" -> handleTest(sender, args);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage("§cدستور نامعتبر است.");
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("§7استفاده: /lockpick create <id> <difficulty 0-1> <pinCount> [player|console] [cmd...]");
            return;
        }
        try {
            String id = args[1];
            double difficulty = Double.parseDouble(args[2]);
            int pinCount = Integer.parseInt(args[3]);

            String successCommand = null;
            LockData.CommandExecutor executor = null;
            if (args.length >= 6) {
                String executorArg = args[4].toLowerCase();
                if (executorArg.equals("player")) {
                    executor = LockData.CommandExecutor.PLAYER;
                } else if (executorArg.equals("console")) {
                    executor = LockData.CommandExecutor.CONSOLE;
                } else {
                    sender.sendMessage("§cنوع اجراکننده باید player یا console باشد.");
                    return;
                }
                successCommand = String.join(" ", java.util.Arrays.copyOfRange(args, 5, args.length));
            } else if (args.length == 5) {
                sender.sendMessage("§7استفاده: /lockpick create <id> <difficulty 0-1> <pinCount> [player|console] [cmd...]");
                return;
            }

            plugin.getDatabaseManager().saveLock(new LockData(id, difficulty, pinCount, successCommand, executor));
            sender.sendMessage("§aقفل «" + id + "» با موفقیت ساخته شد." +
                    (successCommand != null ? " §7(کامند موفقیت ثبت شد)" : ""));
        } catch (NumberFormatException e) {
            sender.sendMessage("§cمقادیر عددی نامعتبر است.");
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7استفاده: /lockpick give <player> [amount]");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cپلیر پیدا نشد یا آنلاین نیست.");
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                sender.sendMessage("§cتعداد نامعتبر است.");
                return;
            }
        }
        for (int i = 0; i < amount; i++) {
            ItemStack item = plugin.getLockpickItemManager().createItem();
            target.getInventory().addItem(item);
        }
        sender.sendMessage("§a" + amount + " لاک‌پیک به " + target.getName() + " داده شد.");
        target.sendMessage("§aیک لاک‌پیک دریافت کردی.");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§7استفاده: /lockpick remove <id>");
            return;
        }
        plugin.getDatabaseManager().deleteLock(args[1]);
        sender.sendMessage("§aقفل «" + args[1] + "» حذف شد.");
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player) || args.length < 2) {
            sender.sendMessage("§7استفاده: /lockpick stats <id>");
            return;
        }
        int[] stats = plugin.getDatabaseManager().getStats(player.getUniqueId(), args[1]);
        sender.sendMessage("§7تلاش‌ها: §f" + stats[0] + " §7موفقیت‌ها: §f" + stats[1]);
    }

    private void handleTest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cاین دستور فقط برای پلیرها است.");
            return;
        }
        String lockId = args.length >= 2 ? args[1] : "test_lock";
        double difficulty = args.length >= 3 ? Double.parseDouble(args[2]) : 0.3;
        boolean started = plugin.getLockpickManager().startSession(player, lockId, difficulty);
        if (!started) {
            player.sendMessage("§cنمی‌توان مینی‌گیم را شروع کرد (شاید یک جلسه فعال داری).");
        }
    }

    private void handleReload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage("§aتنظیمات پلاگین دوباره بارگذاری شد.");
    }
}
