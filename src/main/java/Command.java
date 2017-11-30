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
    public boolean isRun () {
        return getState() == CommandStates.Run;
    }
    public boolean isNew() {
        return getState() == CommandStates.New;
    }
    public boolean isFinished() { return getState() == CommandStates.Complete || getState() == CommandStates.Failed || getState() == CommandStates.Canceled; }
    public Command prepare(ArmyAllyOrdering army)  throws Exception { return this; }
    public Integer getRunTickIndex() {
        return runTickIndex;
    }

    abstract public void pinned();
    abstract public boolean check(ArmyAllyOrdering army);
    abstract public void processing(SmartVehicle vehicle);

    public void run(ArmyAllyOrdering army) throws Exception {

        if (this.getState() != CommandStates.New) {
            return;
        }

        Iterator<CommandWrapper> iter = queue.iterator();

        while (iter.hasNext()) {
            CommandQueue.getInstance().addCommand(iter.next());
        }

        setState(CommandStates.Hold);
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (runTickIndex < 0) {
            runTickIndex = MyStrategy.world.getTickIndex();
        }
    }


    public void addCommand(CommandWrapper command) {
        queue.add(command);
    }

}
