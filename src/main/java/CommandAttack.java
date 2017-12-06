import java.util.HashMap;
import java.util.Map;

public class CommandAttack extends Command {


    private Map<Long, Integer> durabilityBeforeAttack;
    private Integer currentDurabilityDelta;

    public CommandAttack () {
        durabilityBeforeAttack = new HashMap<>();
        currentDurabilityDelta = 0;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {
        army.getForm().recalc(army.getVehicles());
        TargetPoint target = army.searchNearestEnemy();

        if (target == null) {//no enemy for vehicle
            complete();
            return;
        }

        if (target.vector.magnitude() == 0) {
            complete();
            return;
        }

        CommandMove move = new CommandMove(target);
        setParentCommand(move);

        for (SmartVehicle vehicle : army.getVehicles().values()) {
            if (vehicle.getDurability() > 0) {
                durabilityBeforeAttack.put(vehicle.getId(), vehicle.getDurability());
            }
        }
    }

    public boolean check(ArmyAllyOrdering army) {
        if (currentDurabilityDelta > 200) {//go to defence command need
            getParentCommand().complete();
            army.addCommand(new CommandDefence());
            return true;
        }
        currentDurabilityDelta = 0;
        return super.check(army);
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
            currentDurabilityDelta += (durabilityBeforeAttack.get(vehicle.getId()) - vehicle.getDurability());
        }
    }

    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
