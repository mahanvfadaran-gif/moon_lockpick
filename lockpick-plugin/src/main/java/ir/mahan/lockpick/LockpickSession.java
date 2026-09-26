package ir.mahan.lockpick;

import ir.mahan.lockpick.LockpickPlugin;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class LockpickSession {
    private final LockpickPlugin plugin;
    private final Player player;
    private final String lockId;
    private final int totalPins;
    private final double baseSpeed;
    private final double speedIncreasePerPin;
    private final Consumer<Boolean> onFinish;
    private final BossBar bossBar;
    private final BarColor baseColor;
    private BukkitTask task;
    private int currentPin = 0;
    private double progress = 0.0;
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
        this.speed = this.baseSpeed;
        String title = plugin.getConfig().getString("bossbar.title", "\u00a7e\u062f\u0631 \u062d\u0627\u0644 \u0628\u0627\u0632 \u06a9\u0631\u062f\u0646 \u0642\u0641\u0644...");
        this.baseColor = BarColor.valueOf((String)plugin.getConfig().getString("bossbar.color", "YELLOW"));
        BarStyle style = BarStyle.valueOf((String)plugin.getConfig().getString("bossbar.style", "SOLID"));
        this.bossBar = Bukkit.createBossBar((String)title, (BarColor)this.baseColor, (BarStyle)style, (BarFlag[])new BarFlag[0]);
        this.bossBar.addPlayer(player);
        this.rollTargetZone();
    }

    public void start() {
        this.task = Bukkit.getScheduler().runTaskTimer((Plugin)this.plugin, () -> {
            this.progress += this.speed;
            if (this.progress >= 1.0) {
                this.progress = 1.0;
                this.speed = -this.speed;
            } else if (this.progress <= 0.0) {
                this.progress = 0.0;
                this.speed = -this.speed;
            }
            this.bossBar.setProgress(this.progress);
            boolean inZone = this.progress >= this.targetMin && this.progress <= this.targetMax;
            this.bossBar.setColor(inZone ? BarColor.GREEN : this.baseColor);
        }, 0L, 1L);
    }

    public boolean attemptUnlock() {
        boolean hit;
        if (this.finished) {
            return false;
        }
        ++this.attempts;
        boolean bl = hit = this.progress >= this.targetMin && this.progress <= this.targetMax;
        if (hit) {
            ++this.currentPin;
            if (this.currentPin >= this.totalPins) {
                this.complete(true);
                return true;
            }
            this.speed += (double)(this.speed > 0.0 ? 1 : -1) * this.speedIncreasePerPin;
            this.rollTargetZone();
            return true;
        }
        this.complete(false);
        return false;
    }

    private void rollTargetZone() {
        double min;
        double width = 0.12;
        this.targetMin = min = Math.random() * (1.0 - width);
        this.targetMax = min + width;
    }

    public void complete(boolean success) {
        if (this.finished) {
            return;
        }
        this.finished = true;
        if (this.task != null) {
            this.task.cancel();
        }
        this.bossBar.removeAll();
        this.onFinish.accept(success);
    }

    public void cancel() {
        this.complete(false);
    }

    public Player getPlayer() {
        return this.player;
    }

    public String getLockId() {
        return this.lockId;
    }

    public int getAttempts() {
        return this.attempts;
    }
}
