
public class CommandDefence extends Command {
    public CommandDefence () {
        super();
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {
        army.getForm().recalc(army.getVehicles());
        Point2D point = army.dangerPoint();

        if (point == null) {//danger is gone, relax take it easy
            return;
        }

        point = MyStrategy.enemyField.searchNearestSafetyPoint(army.getVehiclesType(), army.getForm().getAvgPoint(), point);

        if (point == null) {
            throw new Exception("Mistake call defence");
        }

        setParentCommand(new CommandMove(point.subtract(army.getForm().getAvgPoint())));
    }

    public void pinned() {
        return;
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
