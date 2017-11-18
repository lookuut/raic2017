import model.ActionType;

import java.util.function.Consumer;

public class CommandMove extends Command {
    protected double x;
    protected double y;
    protected AllyArmy army;


    public CommandMove(double x, double y, AllyArmy army) {
        super(army);

        this.x = x;
        this.y = y;

        this.setState(CommandStates.New);
        this.army = army;

        Consumer<Command> funcMove = (command) -> {
            double localX = this.x - command.getArmy().getAvgX();
            double localY = this.y - command.getArmy().getAvgY();

            MyStrategy.move.setAction(ActionType.MOVE);
            MyStrategy.move.setX(localX);
            MyStrategy.move.setY(localY);
        };

        queue.add(selectArmy);
        queue.add(funcMove);
    }

    public boolean check () {

        if (this.getState() == CommandStates.Complete || this.getState() == CommandStates.Failed) {
            return true;
        }

        double x = this.army.getAvgX();
        double y = this.army.getAvgY();

        if (Math.abs(x - this.x) < 20 && Math.abs(y - this.y) < 20) {
            CommandQueue.getInstance().addCommand(new CommandWrapper(this.selectArmy, this));

            Consumer<Command> funcStop = (command) -> {
                MyStrategy.move.setAction(ActionType.MOVE);
                MyStrategy.move.setX(0);
                MyStrategy.move.setY(0);
            };
            CommandQueue.getInstance().addCommand(new CommandWrapper(funcStop , this));
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
