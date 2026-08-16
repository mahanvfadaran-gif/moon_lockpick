package ir.mahan.lockpick;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class LockpickSession {

    private final LockpickPlugin plugin;
    private final Player player;
    private final String lockId;
    private final int totalPins;
    private final double baseSpeed;
    private final double speedIncreasePerPin;
    private final Consumer<Boolean> onFinish;

    private final BossBar bossBar;
    private BukkitTask task;

    private int currentPin = 0;
    private double progress = 0;
    private double speed;
    private double targetMin;
    private double targetMax;
    private int attempts = 0;
    private boolean finished = false;

    public LockpickSession(LockpickPlugin plugin, Player player, String lockId, int totalPins, Consumer<Boolean> onFinish) {
        this.plugin = plugin;
        this.player = player;
        this.lockId = lockId;
        this.totalPins = totalPins;
        this.onFinish = onFinish;

        this.baseSpeed = plugin.getConfig().getDouble("session.base-speed", 0.02);
        this.speedIncreasePerPin = plugin.getConfig().getDouble("session.speed-increase-per-pin", 0.006);
        this.speed = baseSpeed;

        String title = plugin.getConfig().getString("bossbar.title", "§eدر حال باز کردن قفل...");
        BarColor color = BarColor.valueOf(plugin.getConfig().getString("bossbar.color", "YELLOW"));
        BarStyle style = BarStyle.valueOf(plugin.getConfig().getString("bossbar.style", "SOLID"));

        this.bossBar = Bukkit.createBossBar(title, color, style);
        this.bossBar.addPlayer(player);

        rollTargetZone();
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            progress += speed;
            if (progress >= 1) {
                progress = 1;
                speed = -speed;
            } else if (progress <= 0) {
                progress = 0;
                speed = -speed;
            }
            bossBar.setProgress(progress);
        }, 0L, 1L);
    }

    public boolean attemptUnlock() {
        if (finished) {
            return false;
        }
        attempts++;
        boolean hit = progress >= targetMin && progress <= targetMax;

        if (hit) {
            currentPin++;
            if (currentPin >= totalPins) {
                complete(true);
                return true;
            }
            speed += (speed > 0 ? 1 : -1) * speedIncreasePerPin;
            rollTargetZone();
            return true;
        }

        complete(false);
        return false;
    }

    private void rollTargetZone() {
        double width = 0.12;
        double min = Math.random() * (1 - width);
        this.targetMin = min;
        this.targetMax = min + width;
    }

    public void complete(boolean success) {
        if (finished) {
            return;
        }
        finished = true;
        if (task != null) {
            task.cancel();
        }
        bossBar.removeAll();
        onFinish.accept(success);
    }

    public void cancel() {
        complete(false);
    }

    public Player getPlayer() {
        return player;
    }

    public String getLockId() {
        return lockId;
    }

    public int getAttempts() {
        return attempts;
    }
}
