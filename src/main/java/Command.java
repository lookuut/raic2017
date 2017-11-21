import model.ActionType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

enum CommandStates {
    New, Run, Canceled, Failed, Complete, Hold
}

public class Command {

    protected CommandStates state;
    protected AllyArmy army;
    protected Consumer<Command> selectArmy;
    protected Queue<CommandWrapper> queue;
    protected Integer runTickIndex = -1;

    public Command(AllyArmy army) {
        this.army = army;
        this.selectArmy = (command) -> {
            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            MyStrategy.move.setGroup(command.getArmy().getGroupId());
        };
        this.setState(CommandStates.New);
        queue = new LinkedList<>();
    }

    public CommandStates getState() {
        return state;
    }

    public void setState(CommandStates state) {
        this.state = state;
    }

    public boolean check() {
        setState(CommandStates.Complete);
        return true;
    }

    public AllyArmy getArmy () {
        return army;
    }

    public void run() {

        if (this.getState() != CommandStates.New) {
            return;
        }

        Iterator<CommandWrapper> iter = queue.iterator();

        while (iter.hasNext()) {
            CommandQueue.getInstance().addCommand(iter.next());
            //CommandQueue.getInstance().addCommand(new CommandWrapper(iter.next(), this, -1));
        }

        this.setState(CommandStates.Hold);
    }

    public void result(SmartVehicle vehicle) {
        if (runTickIndex < 0) {
            runTickIndex = MyStrategy.world.getTickIndex();
        }
    }

    public Integer getRunTickIndex() {
        return runTickIndex;
    }

    public boolean isRun () {
        return getState() == CommandStates.Run;
    }

    public boolean isNew() {
        return getState() == CommandStates.New;
    }

    /**
     * @canceled, complete or failed
     * @return
     */

    public boolean isFinished() {
        return getState() == CommandStates.Complete || getState() == CommandStates.Failed || getState() == CommandStates.Canceled;
    }

    public void addCommand(CommandWrapper cw) {
        queue.add(cw);
    }
}
