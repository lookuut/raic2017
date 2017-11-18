public class BTreeAction extends BTreeNode {

    protected Command command;

    public BTreeAction(Command command) {
        this.command = command;
    }

    public void run() {
        if (command.getState() == CommandStates.New || command.getState() == CommandStates.Hold) {
            command.run();
        }
    }

    public boolean isRun () {
        return command.getState() == CommandStates.Run;
    }

    public Command getCommand () {
        return command;
    }
}
