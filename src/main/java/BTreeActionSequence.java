import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

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

    public Command getCommand () {

        if ((command == null || command.isFinished()) && commandsSupplierQueue.size() > 0) {
            command = commandsSupplierQueue.poll().get();
        }

        return command;
    }

    public boolean isComplete() {
        return command.getState() == CommandStates.Complete;
    }

}
