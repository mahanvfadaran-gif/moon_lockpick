package ir.mahan.lockpick.storage;

public class LockData {

    public enum CommandExecutor {
        PLAYER,
        CONSOLE
    }

    private final String id;
    private final double difficulty;
    private final int pinCount;
    private final String successCommand;
    private final CommandExecutor commandExecutor;

    public LockData(String id, double difficulty, int pinCount) {
        this(id, difficulty, pinCount, null, null);
    }

    public LockData(String id, double difficulty, int pinCount, String successCommand, CommandExecutor commandExecutor) {
        this.id = id;
        this.difficulty = difficulty;
        this.pinCount = pinCount;
        this.successCommand = successCommand;
        this.commandExecutor = commandExecutor;
    }

    public String getId() {
        return id;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public int getPinCount() {
        return pinCount;
    }

    public String getSuccessCommand() {
        return successCommand;
    }

    public boolean hasSuccessCommand() {
        return successCommand != null && !successCommand.isBlank();
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }
}
