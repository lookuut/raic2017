import model.ActionType;


import javafx.geometry.Point2D;

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

    public double getX() {
        return targetPosition.getX();
    }

    public double getY() {
        return targetPosition.getY();
    }

    ///kostiil
    protected Point2D lastPoint;
    protected int lastActiveIndex;

    public boolean check (AllyArmy army) {

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


        //@TODO workaround
        if (CustomParams.coorsCeil(lastPoint.getX()) != CustomParams.coorsCeil(army.getAvgX()) || CustomParams.coorsCeil(lastPoint.getY()) != CustomParams.coorsCeil(army.getAvgY())) {
            lastActiveIndex = MyStrategy.world.getTickIndex();
        }

        if (MyStrategy.world.getTickIndex() - lastActiveIndex > 200) {
            army.addCommand(new CommandScale());
            setState(CommandStates.Failed);
            return true;
        }

        lastPoint = new Point2D(army.getAvgX(), army.getAvgY());

        return false;
    }

    public void run(AllyArmy army) throws Exception {


        if (isNew()) {

            army.recalculationMaxMin();
            //be carefull with double values
            Point2D avgPoint = new Point2D(Math.ceil(army.getAvgPoint().getX()), Math.ceil(army.getAvgPoint().getY()));

            targetVector = targetPosition.subtract(avgPoint);

            if (targetVector.magnitude() == 0) {
                setState(CommandStates.Failed);
                throw new Exception("Something goes wrong with move position " + targetPosition.toString());
            }

            for (SmartVehicle vehicle : army.getVehicles().values()) {
                Point2D point = vehicle.point.add(targetVector);
                vehiclesTargetPositions.put(vehicle.getId(), new Point2D(CustomParams.coorsCeil(point.getX()), CustomParams.coorsCeil(point.getY())));
            }

            startTick = MyStrategy.world.getTickIndex();
            Consumer<Command> funcMove = (command) -> {
                MyStrategy.move.setAction(ActionType.MOVE);
                MyStrategy.move.setX(targetVector.getX());
                MyStrategy.move.setY(targetVector.getY());

            };
            army.selectCommand();
            queue.add(new CommandWrapper(funcMove, this, -1));
            super.run(army);
        }
    }

    public Command prepare(AllyArmy army) throws Exception {


        CommandMove move = army.pathFinder(this);
        if (move == this) {
            return this;
        }

        army.addCommandToHead(this);
        return move;
    }

    public void result(AllyArmy army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
        }
    }

    @Override
    public void runned() {
        lastActiveIndex = MyStrategy.world.getTickIndex();
        lastPoint = new Point2D(0,0);
    }

    @Override
    public void processing(SmartVehicle vehicle) {
        if (vehicle.getDurability() == 0) {
            vehiclesTargetPositions.remove(vehicle.getId());
        } else if (vehiclesTargetPositions.get(vehicle.getId()).getX()  == CustomParams.coorsCeil(vehicle.getX())
                &&
                vehiclesTargetPositions.get(vehicle.getId()).getY()  == CustomParams.coorsCeil(vehicle.getY())) {
            readyVehicles.add(vehicle.getId());
        }
    }
}
