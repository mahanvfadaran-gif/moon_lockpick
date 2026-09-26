package ir.mahan.lockpick.storage;

public class LockData {
    private final String id;
    private final double difficulty;
    private final int pinCount;
    private final String successCommand;
    private final CommandExecutor commandExecutor;
    private final String worldName;
    private final Integer blockX;
    private final Integer blockY;
    private final Integer blockZ;

    public LockData(String id, double difficulty, int pinCount) {
        this(id, difficulty, pinCount, null, null);
    }

    public LockData(String id, double difficulty, int pinCount, String successCommand, CommandExecutor commandExecutor) {
        this(id, difficulty, pinCount, successCommand, commandExecutor, null, null, null, null);
    }

    public LockData(String id, double difficulty, int pinCount, String successCommand, CommandExecutor commandExecutor, String worldName, Integer blockX, Integer blockY, Integer blockZ) {
        this.id = id;
        this.difficulty = difficulty;
        this.pinCount = pinCount;
        this.successCommand = successCommand;
        this.commandExecutor = commandExecutor;
        this.worldName = worldName;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
    }

    public String getId() {
        return this.id;
    }

    public double getDifficulty() {
        return this.difficulty;
    }

    public int getPinCount() {
        return this.pinCount;
    }

    public String getSuccessCommand() {
        return this.successCommand;
    }

    public boolean hasSuccessCommand() {
        return this.successCommand != null && !this.successCommand.isBlank();
    }

    public CommandExecutor getCommandExecutor() {
        return this.commandExecutor;
    }

    public boolean hasLocation() {
        return this.worldName != null && this.blockX != null && this.blockY != null && this.blockZ != null;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public Integer getBlockX() {
        return this.blockX;
    }

    public Integer getBlockY() {
        return this.blockY;
    }

    public Integer getBlockZ() {
        return this.blockZ;
    }

    public static enum CommandExecutor {
        PLAYER,
        CONSOLE;

    }
}
