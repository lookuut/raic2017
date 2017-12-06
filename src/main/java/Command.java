import sun.awt.geom.AreaOp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

enum CommandStates {
    New, Run, Canceled, Failed, Complete, Hold
}

abstract public class Command {

    private CommandStates state;
    private Queue<CommandWrapper> queue;
    protected Integer runTickIndex = -1;
    private Command parentCommand;

    public Command() {
        this.setState(CommandStates.New);
        queue = new LinkedList<>();
        parentCommand = null;
    }

    public CommandStates getState() {
        return state;
    }

    public void setState(CommandStates state) {
        this.state = state;
    }

    public Command getParentCommand() {
        return parentCommand;
    }

    public void complete() {
        if (parentCommand != null) {
            parentCommand.complete();
        }
        setState(CommandStates.Complete);
    }

    public boolean isRun () {
        return parentCommand != null && parentCommand.isRun() || getState() == CommandStates.Run;
    }

    public boolean isHold () {
        return parentCommand != null && parentCommand.isHold() || getState() == CommandStates.Hold;
    }

    public boolean isNew() {
        return parentCommand != null && parentCommand.isNew() || getState() == CommandStates.New;
    }
    public boolean isFinished() {
        CommandStates state = parentCommand != null ? parentCommand.getState() : getState();
        return state == CommandStates.Complete || state == CommandStates.Failed || state == CommandStates.Canceled;
    }
    public Integer getRunTickIndex() {
        return runTickIndex;
    }

    abstract public void pinned();

    public boolean check(ArmyAllyOrdering army) {
        if (parentCommand != null) {
            if (parentCommand.check(army)) {
                setState(CommandStates.Complete);
                return true;
            }
            return false;
        }

        setState(CommandStates.Complete);
        return true;
    }

    abstract public void processing(SmartVehicle vehicle);
    abstract public void prepare(ArmyAllyOrdering army) throws Exception;

    public void run(ArmyAllyOrdering army) throws Exception {
        if (parentCommand != null && parentCommand.isNew()) {
            parentCommand.run(army);
            setState(CommandStates.Hold);
        } else {
            if (this.getState() != CommandStates.New) {
                return;
            }

            Iterator<CommandWrapper> iter = queue.iterator();

            while (iter.hasNext()) {
                CommandQueue.getInstance().addCommand(iter.next());
            }

            setState(CommandStates.Hold);
        }

    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {}

    public void setParentCommand(Command parentCommand) {
        this.parentCommand = parentCommand;
    }

    public void addCommand(CommandWrapper command) {
        queue.add(command);
    }
}
