
import java.util.List;

public class CommandDefence extends Command {
    private Point2D mapCenter;
    public CommandDefence () {
        super();
        mapCenter = new Point2D(MyStrategy.world.getWidth()/2, (MyStrategy.world.getHeight()/2));
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {

        Point2D point = army.dangerPoint();

        if (point == null) {//danger is gone, relax take it easy
            army.getForm().update(army.getVehicles());
            army.addCommand(new CommandMove(army.getForm().getAvgPoint().subtract(mapCenter), true));
            complete();
            return;
        }

        PPFieldEnemy damageField = army.getDamageField();

        army.getForm().update(army.getVehicles());
        double allyArmyDamageFactor = damageField.getTileFactor(damageField.getTransformedPoint(army.getForm().getAvgPoint()));

        MyStrategy.battleField.defineArmies();
        List<Army> enemyArmies = MyStrategy.battleField.getEnemyArmies();
        Army dangerArmy = null;
        double enemyArrivedTick = Double.MAX_VALUE;
        for (Army enemyArmy : enemyArmies) {
            double enemyDamageFactor = damageField.getTileFactor(damageField.getTransformedPoint(enemyArmy.getForm().getEdgesVehiclesCenter()));

            if (allyArmyDamageFactor + enemyDamageFactor > 0)  { //run forrest run from this guy, he can defeat us
                double tick = enemyArmy.getForm().getEdgesVehiclesCenter().distance(army.getForm().getEdgesVehiclesCenter()) / enemyArmy.getSpeed();
                if (tick < enemyArrivedTick) {
                    enemyArrivedTick = tick;
                    dangerArmy = enemyArmy;
                }
            }
        }

        if (dangerArmy == null) {//no dangerous army around, relax
            complete();
            return;
        }

        Point2D safetyPoint;
        if (army.isAerial()) {
            PPFieldEnemy enemyPPField = dangerArmy.getDamageField();
            safetyPoint = enemyPPField.getMinValuePoint();
        } else {
            safetyPoint = army.getForm().getAvgPoint().subtract(dangerArmy.getForm().getEdgesVehiclesCenter()).add(army.getForm().getAvgPoint());
        }

        if (safetyPoint == null) {
            throw new Exception("Mistake call defence");
        }

        army.addCommand(new CommandMove(safetyPoint.subtract(army.getForm().getAvgPoint()), true));
        complete();
    }

    public void pinned() {
        return;
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
