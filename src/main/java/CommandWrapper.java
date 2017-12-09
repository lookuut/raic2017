
import java.util.function.Consumer;

public class CommandWrapper {
    public Consumer<Command> consumer;
    public Command command;
    public Integer tickIndex = -1;
    public Integer armyId;

    public CommandWrapper(Consumer<Command> consumer, Command command, Integer tickIndex, Integer armyId) {
        this.consumer = consumer;
        this.command = command;
        this.tickIndex = tickIndex;
        this.armyId = armyId;
    }
}