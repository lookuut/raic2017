import model.ActionType;

import java.util.function.Consumer;

public class CommandMove extends Command {
    protected Point2D targetVector;

    private int startTick;
    private int maxRunnableTick = 0;


    public CommandMove(Point2D targetVector) throws Exception {
        super();
        this.targetVector = targetVector;
    }

    public boolean check (ArmyAllyOrdering army) {

        if (getState() == CommandStates.Complete || getState() == CommandStates.Failed) {
            return true;
        }

        if (getState() == CommandStates.Hold) {
            return false;
        }

        if (maxRunnableTick + startTick <= MyStrategy.world.getTickIndex()) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    public void run(ArmyAllyOrdering army) throws Exception {


        if (isNew()) {

            army.getForm().recalc(army.getVehicles());
            //be carefull with double values
            Point2D avgPoint = new Point2D(army.getForm().getAvgPoint().getX(), army.getForm().getAvgPoint().getY());
            SmartVehicle nearVehicle = army.getNearestVehicle(avgPoint);
            maxRunnableTick = nearVehicle.getVehiclePointAtTick(targetVector);

            if (targetVector.magnitude() == 0) {
                setState(CommandStates.Failed);
                throw new Exception("Something goes wrong with move position " + targetVector.toString());
            }

            startTick = MyStrategy.world.getTickIndex();
            Consumer<Command> funcMove = (command) -> {
                MyStrategy.move.setAction(ActionType.MOVE);
                MyStrategy.move.setX(targetVector.getX());
                MyStrategy.move.setY(targetVector.getY());
            };

            addCommand(new CommandWrapper(funcMove, this, CustomParams.runImmediatelyTick, army.getGroupId()));
            super.run(army);
        }
    }

    public Command prepare(ArmyAllyOrdering army) throws Exception {
        CommandMove move = army.pathFinder(this);
        if (move == this) {
            return this;
        }

        army.addCommandToHead(this);
        return move;
    }

    public void result(ArmyAlly army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
        }
    }

    @Override
    public void pinned() {
        startTick = MyStrategy.world.getTickIndex();
    }

    public Point2D getTargetVector() {
        return targetVector;
    }

    public Integer getMaxRunnableTick () {
        return maxRunnableTick;
    }

    public void processing(SmartVehicle vehicle) {
    }
}
