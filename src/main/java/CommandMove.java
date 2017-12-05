import model.ActionType;

import java.util.function.Consumer;

public class CommandMove extends Command {
    protected Point2D targetVector;

    private int startTick;
    private int maxRunnableTick = 0;


    public CommandMove(Point2D targetVector){
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
            if (army.isNeedToCompact()) {
                army.addCommand(new CommandScale());
            }

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
            maxRunnableTick = Math.max(nearVehicle.getVehiclePointAtTick(targetVector), 1);

            if (targetVector.magnitude() == 0) {//already at point
                setState(CommandStates.Complete);
                return;
            }

            startTick = MyStrategy.world.getTickIndex();
            Consumer<Command> funcMove = (command) -> {
                MyStrategy.move.setAction(ActionType.MOVE);
                MyStrategy.move.setX(targetVector.getX());
                MyStrategy.move.setY(targetVector.getY());
                MyStrategy.move.setMaxSpeed(army.getMinSpeed());
            };

            addCommand(new CommandWrapper(funcMove, this, CustomParams.runImmediatelyTick, army.getGroupId()));
            super.run(army);
        }
    }

    public Command prepare(ArmyAllyOrdering army) throws Exception {
        TargetPoint target = new TargetPoint();

        target.vector = this.targetVector;
        target.maxDamageValue = 0;

        CommandMove move = army.pathFinder(this, target);
        if (move == this) {
            return this;
        }
        this.targetVector = this.getTargetVector().subtract(move.targetVector);
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
