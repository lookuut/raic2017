
import java.util.List;

public class CommandDefence extends Command {
    public CommandDefence () {
        super();
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {

        Point2D point = army.dangerPoint();

        if (point == null) {//danger is gone, relax take it easy
            return;
        }

        PPFieldEnemy damageField = army.getDamageField();

        army.getForm().recalc(army.getVehicles());
        double allyArmyDamageFactor = damageField.getFactorOld(damageField.getTransformedPoint(army.getForm().getAvgPoint()));

        MyStrategy.battleField.defineArmies();
        List<Army> enemyArmies = MyStrategy.battleField.getEnemyArmies();
        Army dangerArmy = null;

        for (Army enemyArmy : enemyArmies) {
            if (enemyArmy.getForm().getEdgesVehiclesCenter().subtract(army.getForm().getAvgPoint()).magnitude() <= CustomParams.safetyDistance + CustomParams.safetyDistance / 2) {
                double enemyDamageFactor = damageField.getFactorOld(damageField.getTransformedPoint(enemyArmy.getForm().getEdgesVehiclesCenter()));
                if (allyArmyDamageFactor + enemyDamageFactor > 0)  { //run forrest run from this guy, he can defeat us
                    dangerArmy = enemyArmy;
                    break;
                }
            }
        }

        if (dangerArmy == null) {//no dangerous army around, relax
            complete();
            return;
        }

        PPFieldEnemy enemyPPField = dangerArmy.getDamageField();
        Point2D safetyPoint = enemyPPField.getMinValuePoint();

        if (safetyPoint == null) {
            throw new Exception("Mistake call defence");
        }

        complete();
        army.addCommand(new CommandMove(safetyPoint.subtract(army.getForm().getAvgPoint())));
    }

    public void pinned() {
        return;
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
