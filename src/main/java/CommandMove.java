import model.ActionType;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.List;
import java.util.function.Consumer;

public class CommandMove extends Command {
    protected double x;
    protected double y;
    protected int startTick;

    public CommandMove(double x, double y) throws Exception {
        super();
        this.x = x;
        this.y = y;
        if (x > MyStrategy.world.getWidth() || x < 0 || y > MyStrategy.world.getHeight() || y < 0 ) {
            throw new Exception("Wrong params");
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }



    public boolean check (AllyArmy army) {

        if (this.getState() == CommandStates.Complete || this.getState() == CommandStates.Failed) {
            return true;
        }

        army.recalculationMaxMin();
        double maxX = army.getMaxX();
        double maxY = army.getMaxY();

        double minX = army.getMinX();
        double minY = army.getMinY();

        if (this.x <= maxX && this.y <= maxY && this.x >= minX && this.y >= minY) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    public void run(AllyArmy army) {
        army.select();

        if (isNew()) {
            startTick = MyStrategy.world.getTickIndex();
            Consumer<Command> funcMove = (command) -> {
                army.recalculationMaxMin();
                double localX = this.x - army.getAvgX();
                double localY = this.y - army.getAvgY();

                MyStrategy.move.setAction(ActionType.MOVE);
                MyStrategy.move.setX(localX);
                MyStrategy.move.setY(localY);
            };
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
    public void runned(){

    }
}
