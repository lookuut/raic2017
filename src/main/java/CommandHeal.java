import model.VehicleType;

import java.util.Collection;

public class CommandHeal extends Command {
    private ArmyDivisions divisions;

    public CommandHeal(ArmyDivisions divisions) {
        super();
        this.divisions = divisions;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {
        army.getForm().recalc(army.getVehicles());

        Collection<ArmyAllyOrdering> armies = divisions.getArmyList(VehicleType.ARRV);

        if (armies == null) {//no arrv :(
            complete();
            return;
        }

        double minDistance = Double.MAX_VALUE;
        ArmyAllyOrdering minDistArmy = null;
        for (ArmyAllyOrdering arrvArmy : armies) {
            if (arrvArmy.isArmyAlive()) {
                arrvArmy.getForm().recalc(arrvArmy.getVehicles());
                double distance = arrvArmy.getForm().getAvgPoint().distance(army.getForm().getAvgPoint());
                if (distance < minDistance) {
                    minDistArmy = arrvArmy;
                    minDistance = distance;
                }
            }
        }
        if (minDistArmy == null) {
            complete();
            return;
        }

        TargetPoint target = new TargetPoint();
        target.vector = minDistArmy.getForm().getAvgPoint().subtract(army.getForm().getAvgPoint());
        target.maxDamageValue = 200.0f;

        if (target.vector.magnitude() < CustomParams.onHealerEps) {

            complete();
            return;
        }

        setParentCommand(new CommandMove(target));
    }

    @Override
    public void pinned(){
    }

    @Override
    public void processing(SmartVehicle vehicle) {
    }
}
