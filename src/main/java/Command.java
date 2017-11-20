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
    protected Queue<Consumer<Command>> queue;

    public Command(AllyArmy army) {
        this.army = army;
        this.selectArmy = (command) -> {
            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            MyStrategy.move.setGroup(command.getArmy().getGroupId());
        };
        queue = new LinkedList<>();
    }

    public CommandStates getState() {
        return state;
    }

    public void setState(CommandStates state) {
        this.state = state;
    }

    public boolean check() {
        return true;
    }

    public AllyArmy getArmy () {
        return army;
    }

    public void run() {

        if (this.getState() != CommandStates.New) {
            return;
        }

        Iterator<Consumer<Command>> iter = queue.iterator();

        while (iter.hasNext()) {
            CommandQueue.getInstance().addCommand(new CommandWrapper(iter.next(), this));
        }

        this.setState(CommandStates.Hold);
    }

    public void result(SmartVehicle vehicle) {

    }

    public boolean isRun () {
        return getState() == CommandStates.Run;
    }
}
