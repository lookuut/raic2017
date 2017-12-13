
public class CommandDefence extends Command {
    public CommandDefence () {
        super();
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {

        Point2D point = army.dangerPoint();

        if (point == null) {//danger is gone, relax take it easy
            army.getForm().recalc(army.getVehicles());
            Point2D mapCentre = new Point2D(MyStrategy.world.getWidth() / 2, MyStrategy.world.getHeight() / 2);
            Point2D toCentreVec = mapCentre.subtract(army.getForm().getAvgPoint());
            if (toCentreVec.magnitude() > 100) {
                setParentCommand(new CommandMove(toCentreVec));
            } else {
                setParentCommand(new CommandWait(20));
            }

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
