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

    public void run(AllyArmy army) {
        setState(CommandStates.Complete);
    }

    public Command prepare(AllyArmy army) throws Exception {
        double[] coors = army.searchNearestEnemy();
        army.recalculationMaxMin();
        if (army.isOnCoordinates(coors[0], coors[1])) {
            return this;
        }

        army.addCommandToHead(this);

        CommandMove move = new CommandMove(coors[0], coors[1]);
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
}
