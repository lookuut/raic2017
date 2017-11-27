import javafx.geometry.Point2D;

public class CommandAttack extends Command {

    public CommandAttack () {

    }

    public boolean check(AllyArmy army) {
        setState(CommandStates.Complete);
        return true;
    }

    protected double x;
    protected double y;

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void run(AllyArmy army) throws Exception {
        setState(CommandStates.Complete);
    }

    public Command prepare(AllyArmy army) throws Exception {
        double[] coors = army.searchNearestEnemy();
        army.recalculationMaxMin();
        if (army.isOnCoordinates(coors[0], coors[1])) {
            return this;
        }

        CommandMove move = new CommandMove(new Point2D(coors[0], coors[1]));
        return army.pathFinder(move);
    }



    public void result(AllyArmy army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
        }
    }

    @Override
    public void runned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
