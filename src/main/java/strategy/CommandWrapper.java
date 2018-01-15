package strategy;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

public class CommandWrapper {
    private Queue<Consumer<Command>> queue;
    private Command command;
    private Integer tickIndex;
    private Integer armyId;
    private CommandPriority priority;

    public CommandWrapper(Command command, Integer tickIndex, Integer armyId, CommandPriority priority) {
        queue = new LinkedList<>();
        this.command = command;
        this.tickIndex = tickIndex;
        this.armyId = armyId;
        this.priority = priority;
    }

    public void addCommand(Consumer<Command> consumer) {
        queue.add(consumer);
    }

    public Integer getArmyId() {
        return armyId;
    }

    public Integer getTickIndex() {
        return tickIndex;
    }

    public CommandPriority getPriority() {
        return priority;
    }

    public Command getCommand() {
        return command;
    }

    public Queue<Consumer<Command>> getQueue() {
        return queue;
    }
}