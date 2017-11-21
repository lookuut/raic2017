import model.ActionType;

import java.util.function.Consumer;

public class CommandMove extends Command {
    protected double x;
    protected double y;

    public CommandMove(AllyArmy army, double x, double y) {
        super(army);

        this.x = x;
        this.y = y;

        Consumer<Command> funcMove = (command) -> {
            double localX = this.x - command.getArmy().getAvgX();
            double localY = this.y - command.getArmy().getAvgY();

            MyStrategy.move.setAction(ActionType.MOVE);
            MyStrategy.move.setX(localX);
            MyStrategy.move.setY(localY);
        };

        queue.add(new CommandWrapper(selectArmy, this, -1));
        queue.add(new CommandWrapper(funcMove, this, -1));
    }

    public boolean check () {

        if (this.getState() == CommandStates.Complete || this.getState() == CommandStates.Failed) {
            return true;
        }

        double x = this.army.getAvgX();
        double y = this.army.getAvgY();

        this.army.recalculationMaxMin();
        double maxX = this.army.getMaxX();
        double maxY = this.army.getMaxY();

        double minX = this.army.getMinX();
        double minY = this.army.getMinY();

        if (this.x <= maxX && this.y <= maxY && this.x >= minX && this.y >= minY) {
            //CommandQueue.getInstance().addCommand(new CommandWrapper(this.selectArmy, this));

            /*
            Consumer<Command> funcStop = (command) -> {
                MyStrategy.move.setAction(ActionType.MOVE);
                MyStrategy.move.setX(0);
                MyStrategy.move.setY(0);
            };*/
            //CommandQueue.getInstance().addCommand(new CommandWrapper(funcStop , this));
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    public void result(SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
        }
    }
}
