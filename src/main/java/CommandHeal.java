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
            if (arrvArmy.isAlive()) {
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

        if (target.vector.magnitude() < CustomParams.onHealerEps) {
            army.addCommand(new CommandWait(200));
            complete();
            return;
        }

        CommandMove move = new CommandMove(target);
        move.setPriority(CommandPriority.High);

        setParentCommand(move);
    }

    @Override
    public boolean check (ArmyAllyOrdering army) {
        return super.check(army);
    }
    @Override
    public void pinned(){
    }

    @Override
    public void processing(SmartVehicle vehicle) {
    }
}
