
import model.VehicleType;

import java.util.Collection;

public class CommandHeal extends Command {
    private ArmyDivisions divisions;
    private Army arrvArmy;

    public CommandHeal(ArmyDivisions divisions) {
        this.divisions = divisions;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {

        Army arrvArmy = divisions.getNearestArmy(VehicleType.ARRV, army.getForm().getEdgesVehiclesCenter());

        if (arrvArmy == null) {
            complete();
            return;
        }
        this.arrvArmy = arrvArmy;

        TargetPoint target = new TargetPoint();
        SmartVehicle minDurabilityVehicle = army.getMinDurabilityVehicle();

        target.vector = arrvArmy.getForm().getEdgesVehiclesCenter().subtract(minDurabilityVehicle.getPoint());

        if (target.vector.magnitude() < CustomParams.onHealerEps) {
            setParentCommand(new CommandWait(CustomParams.healTimeout));
            return;
        }
        target.maxDamageValue = army.getForm().getMinDamageFactor(army) * (-1);
        CommandMove move = new CommandMove(target, false);
        move.setPriority(CommandPriority.High);

        setParentCommand(move);
    }

    @Override
    public boolean check (ArmyAllyOrdering army) {
        if (army.averageDurability() >= CustomParams.endHealAVGDurability || !arrvArmy.isAlive() || arrvArmy == null) {
            complete();
            return true;
        }

        if (!army.isSafetyAround(CustomParams.healSafetyDistance)) {
            army.addCommand(new CommandDefence());
            complete();
            return true;
        }

        if (super.check(army)) {
            SmartVehicle minDurabilityVehicle = army.getMinDurabilityVehicle();
            TargetPoint target = new TargetPoint();
            target.vector = arrvArmy.getForm().getEdgesVehiclesCenter().subtract(minDurabilityVehicle.getPoint());

            if (target.vector.magnitude() < CustomParams.onHealerEps) {
                setParentCommand(new CommandWait(CustomParams.healTimeout));
            } else {
                target.maxDamageValue = army.getForm().getMinDamageFactor(army) * (-1);
                setParentCommand(new CommandMove(target, false));
            }
            return false;
        }

        return super.check(army);
    }

    @Override
    public void pinned(){
    }

    @Override
    public void processing(SmartVehicle vehicle) {
    }
}
