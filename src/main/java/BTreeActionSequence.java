import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BTreeActionSequence extends BTreeAction {
    protected Queue<Supplier<Command>> commandsSupplierQueue;
    protected Command command;

    public BTreeActionSequence(Supplier<Command> command) {
        super(command);
        this.commandsSupplierQueue = new LinkedList<>();
        commandsSupplierQueue.add(command);
    }

    public void addCommand(Supplier<Command> command) {
        this.commandsSupplierQueue.add(command);
    }

    public boolean isRun () {
        return commandsSupplierQueue.size() > 0 && commandsSupplierQueue.peek().get().getState() == CommandStates.Run;
    }

    public List<Command> getCommand () {
        return commandsSupplierQueue.stream().map(Supplier::get).collect(Collectors.toList());
    }

    public boolean isComplete() {
        return command.getState() == CommandStates.Complete;
    }

}
