import model.ActionType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

enum CommandStates {
    New, Run, Canceled, Failed, Complete, Hold
}

abstract public class Command {

    protected CommandStates state;
    protected Queue<CommandWrapper> queue;
    protected Integer runTickIndex = -1;

    public Command() {
        this.setState(CommandStates.New);
        queue = new LinkedList<>();
    }

    public CommandStates getState() {
        return state;
    }

    public void setState(CommandStates state) {
        this.state = state;
    }

    abstract public boolean check(AllyArmy army);

    public void run(AllyArmy army) throws Exception {

        if (this.getState() != CommandStates.New) {
            return;
        }

        Iterator<CommandWrapper> iter = queue.iterator();

        while (iter.hasNext()) {
            CommandQueue.getInstance().addCommand(iter.next());
        }

        this.setState(CommandStates.Hold);
    }

    public void result(AllyArmy army, SmartVehicle vehicle) {
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

    public Command prepare(AllyArmy army)  throws Exception {
        return this;
    }

    abstract public void runned();


    public void addCommand(CommandWrapper cw) {
        queue.add(cw);
    }

    abstract public void processing(SmartVehicle vehicle);
}
