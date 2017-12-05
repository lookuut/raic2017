
public class CommandAttack extends Command {

    public CommandAttack () {

    }

    public boolean check(ArmyAllyOrdering army) {
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

    public void run(ArmyAllyOrdering army) throws Exception {
        setState(CommandStates.Complete);
    }

    public Command prepare(ArmyAllyOrdering army) throws Exception {
        army.getForm().recalc(army.getVehicles());
        TargetPoint target = army.searchNearestEnemy();

        if (target == null) {//no enemy for vehicle
            return this;
        }

        if (target.vector.magnitude() == 0) {
            return this;
        }

        CommandMove move = new CommandMove(target.vector);
        if (target.vector.magnitude() < CustomParams.pathSegmentLenght) {
            return move;
        }

        return army.pathFinder(move, target);
    }



    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
        }
    }

    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
