import geom.Point2D;

public class CommandDefence extends Command {
    public CommandDefence () {
        super();
    }


    public Command prepare(ArmyAllyOrdering army) throws Exception {
        try {

            army.getForm().recalc(army.getVehicles());
            Point2D armyPoint = army.getForm().getAvgPoint();
            Point2D[] points = army.getNearestEnemyPointAndSafetyPoint(CustomParams.safetyDistance);

            if (points[0].distance(armyPoint) < CustomParams.safetyDistance) {
                return army.pathFinder(new CommandMove(points[1]));
            }

            return this;
        } catch (Exception e) {
            e.printStackTrace();
        }
        setState(CommandStates.Failed);
        return new CommandAttack();
    }


    public void run(ArmyAllyOrdering army) throws Exception {
        setState(CommandStates.Complete);
    }

    public boolean check(ArmyAllyOrdering army) {
        setState(CommandStates.Complete);
        return true;
    }

    public void pinned() {
        return;
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
