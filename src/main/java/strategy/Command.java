package strategy;

import model.ActionType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;


enum CommandPriority {
    High, Middle, Low
}

enum CommandStates {
    New, Run, Canceled, Failed, Complete, Hold
}

abstract public class Command {

    private CommandPriority priority;
    private CommandStates state;
    private Queue<CommandWrapper> queue;
    private Integer runTickIndex;
    private Command parentCommand;

    public Command() {
        this.setState(CommandStates.New);
        queue = new LinkedList<>();
        parentCommand = null;
        runTickIndex = -1;
        priority = CommandPriority.Low;
    }

    public CommandStates getState() {
        return state;
    }

    public void setState(CommandStates state) {
        this.state = state;
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

    public Integer getRunningTicks() {
        return MyStrategy.world.getTickIndex() - getRunTickIndex();
    }

    public void pinned() {
        setRunTickIndex(MyStrategy.world.getTickIndex());
    }

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

    public void setRunTickIndex (Integer tick) {
        runTickIndex = tick;
    }
    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {}

    public void setParentCommand(Command parentCommand) {
        this.parentCommand = parentCommand;
    }

    public void addCommand(CommandWrapper command) {
        queue.add(command);
    }
    public CommandPriority getPriority () {
        return priority;
    }

    public void setPriority(CommandPriority priority) {
        this.priority = priority;
    }

    public static Consumer<Command> selectSquare(Point2D minPoint, Point2D maxPoint) {
        Consumer<Command> commandSelect = (command) -> {

            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            MyStrategy.move.setLeft(minPoint.getX());
            MyStrategy.move.setBottom(maxPoint.getY());
            MyStrategy.move.setRight(maxPoint.getX());
            MyStrategy.move.setTop(minPoint.getY());
        };

        return commandSelect;
    }

    public static Consumer<Command> selectSquare(List<Long> edgeVehicleIds) {
        Consumer<Command> commandSelect = (command) -> {

            Function<Long, SmartVehicle> getAliveVehicle = (vehicleId) -> {
                return MyStrategy.getVehicles().get(vehicleId).getDurability() > 0 ? MyStrategy.getVehicles().get(vehicleId) : MyStrategy.getVehiclePrevState(vehicleId);
            };

            SmartVehicle leftVehicle = getAliveVehicle.apply(edgeVehicleIds.get(0));
            SmartVehicle rightVehicle = getAliveVehicle.apply(edgeVehicleIds.get(1));
            SmartVehicle topVehicle = getAliveVehicle.apply(edgeVehicleIds.get(2));
            SmartVehicle bottomVehicle = getAliveVehicle.apply(edgeVehicleIds.get(3));

            double left = Math.max(leftVehicle.getX() - MyStrategy.game.getVehicleRadius(), 0);
            double right = Math.min(rightVehicle.getX() + MyStrategy.game.getVehicleRadius(), MyStrategy.world.getWidth());
            double top = Math.max(topVehicle.getY() - MyStrategy.game.getVehicleRadius(), 0);
            double bottom = Math.min(bottomVehicle.getY() + MyStrategy.game.getVehicleRadius(), MyStrategy.world.getHeight());

            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            MyStrategy.move.setLeft(left);
            MyStrategy.move.setRight(right);
            MyStrategy.move.setBottom(bottom);
            MyStrategy.move.setTop(top);
        };

        return commandSelect;
    }
}
