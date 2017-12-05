import model.VehicleType;

import java.util.Collection;

public class CommandHeal extends Command {
    private ArmyDivisions divisions;

    public CommandHeal(ArmyDivisions divisions) {
        super();
        this.divisions = divisions;
    }


    public Command prepare(ArmyAllyOrdering army) throws Exception {
        army.getForm().recalc(army.getVehicles());

        Collection<ArmyAllyOrdering> armies = divisions.getArmyList(VehicleType.ARRV);

        if (armies == null) {//no arrv :(
            setState(CommandStates.Complete);
            return null;
        }

        double minDistance = Double.MAX_VALUE;
        ArmyAllyOrdering minDistArmy = null;
        for (ArmyAllyOrdering arrvArmy : armies) {
            arrvArmy.getForm().recalc(arrvArmy.getVehicles());
            double distance = arrvArmy.getForm().getAvgPoint().distance(army.getForm().getAvgPoint());
            if (distance < minDistance) {
                minDistArmy = arrvArmy;
                minDistance = distance;
            }

        }
        TargetPoint target = new TargetPoint();
        target.vector = minDistArmy.getForm().getAvgPoint().subtract(army.getForm().getAvgPoint());
        target.maxDamageValue = 0.0f;

        if (target.vector.magnitude() < CustomParams.onHealerEps) {
            return null;
        }

        CommandMove move = new CommandMove(target.vector);

        return army.pathFinder(move, target);
    }

    public boolean check (ArmyAllyOrdering army) {
        setState(CommandStates.Complete);
        return true;
    }


    public void run(ArmyAllyOrdering army) throws Exception {
        setState(CommandStates.Complete);
    }

    @Override
    public void pinned(){
    }

    @Override
    public void processing(SmartVehicle vehicle) {
    }
}
