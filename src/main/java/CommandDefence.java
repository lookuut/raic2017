import javafx.geometry.Point2D;

public class CommandDefence extends Command {
    public CommandDefence () {
        super();
    }


    public Command prepare(AllyArmy army) throws Exception {
        try {

            army.recalculationMaxMin();
            Point2D armyPoint = army.getAvgPoint();
            Point2D[] points = army.getNearestEnemyPointAndSafetyPoint(CustomParams.safetyDistance);

            if (points[0].distance(armyPoint) < CustomParams.safetyDistance) {
                return army.pathFinder(new CommandMove(points[1]));
            }

            return this;
        } catch (Exception e) {
            army.printEnemyField();
            e.printStackTrace();
        }
        setState(CommandStates.Failed);
        return new CommandAttack();
    }


    public void run(AllyArmy army) throws Exception {
        setState(CommandStates.Complete);
    }

    public boolean check(AllyArmy army) {
        setState(CommandStates.Complete);
        return true;
    }

    public void runned() {
        return;
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
