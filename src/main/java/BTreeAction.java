import java.util.function.Supplier;

public class BTreeAction extends BTreeNode {

    protected Command command;
    protected Supplier<Command> commandSupplier;

    public BTreeAction(Supplier<Command> commandSupplier) {
        this.commandSupplier = commandSupplier;
        command = null;
    }

    public void run() {
        if (command == null) {
            command = commandSupplier.get();
        }

        if (command.getState() == CommandStates.New || command.getState() == CommandStates.Hold) {
            command.run();
        }
    }

    public boolean isRun () {
        return  command != null && command.getState() == CommandStates.Run;
    }

    public Command getCommand () {
        if (command == null) {
            command = commandSupplier.get();
        }

        return command;
    }

    public boolean isComplete() {
        return command != null && command.getState() == CommandStates.Complete;
    }
}
