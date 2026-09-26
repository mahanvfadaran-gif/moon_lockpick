package ir.mahan.lockpick.commands;

import ir.mahan.lockpick.LockpickPlugin;
import ir.mahan.lockpick.storage.LockData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

public class LockpickCommand
implements CommandExecutor,
TabCompleter {
    private final LockpickPlugin plugin;

    public LockpickCommand(LockpickPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("\u00a77\u0627\u0633\u062a\u0641\u0627\u062f\u0647: /lockpick <create|remove|give|stats|test|reload>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create": {
                this.handleCreate(sender, args);
                break;
            }
            case "remove": {
                this.handleRemove(sender, args);
                break;
            }
            case "give": {
                this.handleGive(sender, args);
                break;
            }
            case "stats": {
                this.handleStats(sender, args);
                break;
            }
            case "test": {
                this.handleTest(sender, args);
                break;
            }
            case "reload": {
                this.handleReload(sender);
                break;
            }
            default: {
                sender.sendMessage("\u00a7c\u062f\u0633\u062a\u0648\u0631 \u0646\u0627\u0645\u0639\u062a\u0628\u0631 \u0627\u0633\u062a.");
            }
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("\u00a77\u0627\u0633\u062a\u0641\u0627\u062f\u0647: /lockpick create <id> <difficulty 0-1> <pinCount> [player|console] [cmd...]");
            return;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7c\u0627\u06cc\u0646 \u062f\u0633\u062a\u0648\u0631 \u0641\u0642\u0637 \u0628\u0631\u0627\u06cc \u067e\u0644\u06cc\u0631\u0647\u0627 \u0627\u0633\u062a (\u0628\u0627\u06cc\u062f \u0628\u0647 \u0628\u0644\u0627\u06a9 \u0645\u0648\u0631\u062f\u0646\u0638\u0631 \u0646\u06af\u0627\u0647 \u06a9\u0646\u06cc).");
            return;
        }
        Player player = (Player) sender;
        RayTraceResult rayTrace = player.rayTraceBlocks(6.0);
        Block targetBlock = rayTrace != null ? rayTrace.getHitBlock() : null;
        if (targetBlock == null) {
            sender.sendMessage("\u00a7c\u0628\u0627\u06cc\u062f \u0628\u0647 \u06cc\u06a9 \u0628\u0644\u0627\u06a9 \u0646\u06af\u0627\u0647 \u06a9\u0646\u06cc \u062a\u0627 \u0642\u0641\u0644 \u0628\u0647 \u0622\u0646 \u0648\u0635\u0644 \u0634\u0648\u062f (\u062d\u062f\u0627\u06a9\u062b\u0631 \u06f6 \u0628\u0644\u0627\u06a9).");
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
                    sender.sendMessage("\u00a7c\u0646\u0648\u0639 \u0627\u062c\u0631\u0627\u06a9\u0646\u0646\u062f\u0647 \u0628\u0627\u06cc\u062f player \u06cc\u0627 console \u0628\u0627\u0634\u062f.");
                    return;
                }
                successCommand = String.join((CharSequence)" ", Arrays.copyOfRange(args, 5, args.length));
            } else if (args.length == 5) {
                sender.sendMessage("\u00a77\u0627\u0633\u062a\u0641\u0627\u062f\u0647: /lockpick create <id> <difficulty 0-1> <pinCount> [player|console] [cmd...]");
                return;
            }
            String worldName = targetBlock.getWorld().getName();
            int x = targetBlock.getX();
            int y = targetBlock.getY();
            int z = targetBlock.getZ();
            this.plugin.getDatabaseManager().saveLock(new LockData(id, difficulty, pinCount, successCommand, executor, worldName, x, y, z));
            sender.sendMessage("\u00a7a\u0642\u0641\u0644 \u00ab" + id + "\u00bb \u0631\u0648\u06cc \u0628\u0644\u0627\u06a9 (" + x + ", " + y + ", " + z + ") \u0628\u0627 \u0645\u0648\u0641\u0642\u06cc\u062a \u0633\u0627\u062e\u062a\u0647 \u0634\u062f." + (successCommand != null ? " \u00a77(\u06a9\u0627\u0645\u0646\u062f \u0645\u0648\u0641\u0642\u06cc\u062a \u062b\u0628\u062a \u0634\u062f)" : ""));
        }
        catch (NumberFormatException e) {
            sender.sendMessage("\u00a7c\u0645\u0642\u0627\u062f\u06cc\u0631 \u0639\u062f\u062f\u06cc \u0646\u0627\u0645\u0639\u062a\u0628\u0631 \u0627\u0633\u062a.");
        }
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("\u00a77\u0627\u0633\u062a\u0641\u0627\u062f\u0647: /lockpick give <player> [amount]");
            return;
        }
        Player target = Bukkit.getPlayer((String)args[1]);
        if (target == null) {
            sender.sendMessage("\u00a7c\u067e\u0644\u06cc\u0631 \u067e\u06cc\u062f\u0627 \u0646\u0634\u062f \u06cc\u0627 \u0622\u0646\u0644\u0627\u06cc\u0646 \u0646\u06cc\u0633\u062a.");
            return;
        }
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            }
            catch (NumberFormatException e) {
                sender.sendMessage("\u00a7c\u062a\u0639\u062f\u0627\u062f \u0646\u0627\u0645\u0639\u062a\u0628\u0631 \u0627\u0633\u062a.");
                return;
            }
        }
        for (int i = 0; i < amount; ++i) {
            ItemStack item = this.plugin.getLockpickItemManager().createItem();
            target.getInventory().addItem(new ItemStack[]{item});
        }
        sender.sendMessage("\u00a7a" + amount + " \u0644\u0627\u06a9\u200c\u067e\u06cc\u06a9 \u0628\u0647 " + target.getName() + " \u062f\u0627\u062f\u0647 \u0634\u062f.");
        target.sendMessage("\u00a7a\u06cc\u06a9 \u0644\u0627\u06a9\u200c\u067e\u06cc\u06a9 \u062f\u0631\u06cc\u0627\u0641\u062a \u06a9\u0631\u062f\u06cc.");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("\u00a77\u0627\u0633\u062a\u0641\u0627\u062f\u0647: /lockpick remove <id>");
            return;
        }
        this.plugin.getDatabaseManager().deleteLock(args[1]);
        sender.sendMessage("\u00a7a\u0642\u0641\u0644 \u00ab" + args[1] + "\u00bb \u062d\u0630\u0641 \u0634\u062f.");
    }

    private void handleStats(CommandSender sender, String[] args) {
        Player player;
        block3: {
            block2: {
                if (!(sender instanceof Player)) break block2;
                player = (Player)sender;
                if (args.length >= 2) break block3;
            }
            sender.sendMessage("\u00a77\u0627\u0633\u062a\u0641\u0627\u062f\u0647: /lockpick stats <id>");
            return;
        }
        int[] stats = this.plugin.getDatabaseManager().getStats(player.getUniqueId(), args[1]);
        sender.sendMessage("\u00a77\u062a\u0644\u0627\u0634\u200c\u0647\u0627: \u00a7f" + stats[0] + " \u00a77\u0645\u0648\u0641\u0642\u06cc\u062a\u200c\u0647\u0627: \u00a7f" + stats[1]);
    }

    private void handleTest(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7c\u0627\u06cc\u0646 \u062f\u0633\u062a\u0648\u0631 \u0641\u0642\u0637 \u0628\u0631\u0627\u06cc \u067e\u0644\u06cc\u0631\u0647\u0627 \u0627\u0633\u062a.");
            return;
        }
        Player player = (Player)sender;
        String lockId = args.length >= 2 ? args[1] : "test_lock";
        double difficulty = args.length >= 3 ? Double.parseDouble(args[2]) : 0.3;
        boolean started = this.plugin.getLockpickManager().startSession(player, lockId, difficulty);
        if (!started) {
            player.sendMessage("\u00a7c\u0646\u0645\u06cc\u200c\u062a\u0648\u0627\u0646 \u0645\u06cc\u0646\u06cc\u200c\u06af\u06cc\u0645 \u0631\u0627 \u0634\u0631\u0648\u0639 \u06a9\u0631\u062f (\u0634\u0627\u06cc\u062f \u06cc\u06a9 \u062c\u0644\u0633\u0647 \u0641\u0639\u0627\u0644 \u062f\u0627\u0631\u06cc).");
        }
    }

    private void handleReload(CommandSender sender) {
        this.plugin.reloadConfig();
        sender.sendMessage("\u00a7a\u062a\u0646\u0638\u06cc\u0645\u0627\u062a \u067e\u0644\u0627\u06af\u06cc\u0646 \u062f\u0648\u0628\u0627\u0631\u0647 \u0628\u0627\u0631\u06af\u0630\u0627\u0631\u06cc \u0634\u062f.");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String sub;
        if (args.length == 1) {
            return this.filter(Arrays.asList("create", "remove", "give", "stats", "test", "reload"), args[0]);
        }
        return switch (sub = args[0].toLowerCase()) {
            case "create" -> {
                switch (args.length) {
                    case 2: {
                        yield this.filter(List.of("<id>"), args[1]);
                    }
                    case 3: {
                        yield this.filter(List.of("0.0", "0.25", "0.5", "0.75", "1.0"), args[2]);
                    }
                    case 4: {
                        yield this.filter(List.of("3", "4", "5", "6"), args[3]);
                    }
                    case 5: {
                        yield this.filter(List.of("player", "console"), args[4]);
                    }
                }
                yield this.filter(List.of("<command...>"), args[args.length - 1]);
            }
            case "remove", "stats" -> {
                if (args.length == 2) {
                    yield this.filter(this.lockIds(), args[1]);
                }
                yield List.of();
            }
            case "give" -> {
                switch (args.length) {
                    case 2: {
                        yield this.filter(this.onlinePlayerNames(), args[1]);
                    }
                    case 3: {
                        yield this.filter(List.of("1", "5", "10"), args[2]);
                    }
                }
                yield List.of();
            }
            case "test" -> {
                switch (args.length) {
                    case 2: {
                        yield this.filter(this.lockIds(), args[1]);
                    }
                    case 3: {
                        yield this.filter(List.of("0.0", "0.25", "0.5", "0.75", "1.0"), args[2]);
                    }
                }
                yield List.of();
            }
            default -> List.of();
        };
    }

    private List<String> lockIds() {
        return new ArrayList<String>(this.plugin.getDatabaseManager().getAllLocks().keySet());
    }

    private List<String> onlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }

    private List<String> filter(List<String> options, String current) {
        String lower = current.toLowerCase();
        return options.stream().filter(option -> option.toLowerCase().startsWith(lower)).collect(Collectors.toList());
    }
}
