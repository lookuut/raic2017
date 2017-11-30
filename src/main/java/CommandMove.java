import model.ActionType;


import geom.Point2D;

import java.util.HashMap;

import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public class CommandMove extends Command {
    protected Point2D targetPosition;
    protected Point2D targetVector;

    protected int startTick;

    protected Map<Long, Point2D> vehiclesTargetPositions;
    protected HashSet<Long> readyVehicles;

    public CommandMove(Point2D targetPosition) throws Exception {
        super();
        this.targetPosition = targetPosition;
        if (targetPosition.getX() > MyStrategy.world.getWidth() || targetPosition.getX() < 0 || targetPosition.getY() > MyStrategy.world.getHeight() || targetPosition.getY() < 0 ) {
            throw new Exception("Wrong params" + targetPosition.toString());
        }

        vehiclesTargetPositions = new HashMap<>();
        readyVehicles = new HashSet<>();
    }

    public Point2D getTargetPosition() {
        return targetPosition;
    }

    public boolean check (ArmyAllyOrdering army) {

        if (getState() == CommandStates.Complete || getState() == CommandStates.Failed) {
            return true;
        }

        if (getState() == CommandStates.Hold) {
            return false;
        }

        if (readyVehicles.size() > 0) {
            army.addCommand(new CommandScale());
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

            targetVector = targetPosition.subtract(avgPoint);

            if (targetVector.magnitude() == 0) {
                setState(CommandStates.Failed);
                throw new Exception("Something goes wrong with move position " + targetPosition.toString());
            }

            for (SmartVehicle vehicle : army.getVehicles().values()) {
                Point2D point = vehicle.getPoint().add(targetVector);
                vehiclesTargetPositions.put(vehicle.getId(), point);
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
    }

    @Override
    public void processing(SmartVehicle vehicle) {
        if (vehicle.getDurability() == 0) {
            vehiclesTargetPositions.remove(vehicle.getId());
        } else if (vehiclesTargetPositions.get(vehicle.getId()).equals(vehicle.getPoint())) {
            readyVehicles.add(vehicle.getId());
        }
    }
}
