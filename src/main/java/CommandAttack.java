import java.util.HashMap;
import java.util.Map;

public class CommandAttack extends Command {


    private Map<Long, Integer> durabilityBeforeAttack;
    private Integer damageSum;

    private TargetPoint target;
    private Map<Long, Integer> attackedEnemiesDurability;

    public CommandAttack () {
        durabilityBeforeAttack = new HashMap<>();
        attackedEnemiesDurability = new HashMap<>();
        damageSum = 0;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {
        army.getForm().recalc(army.getVehicles());
        target = army.searchNearestEnemy();

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

        if (damageSum > 0) {//go to defence command need

            Integer inflictedDamage = 0;
            for (Map.Entry<Long, Integer> entry : attackedEnemiesDurability.entrySet()) {
                inflictedDamage += entry.getValue() - MyStrategy.getVehicles().get(entry.getKey()).getDurability();
             }

            if (damageSum - inflictedDamage > 200) {
                getParentCommand().complete();
                army.addCommand(new CommandDefence());
                if (army.isNeedToCompact()) {
                    army.addCommand(new CommandCompact());
                }
                return true;
            }
        }

        damageSum = 0;
        return super.check(army);
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
            damageSum += (durabilityBeforeAttack.get(vehicle.getId()) - vehicle.getDurability());
        }

        if (!vehicle.isAlly() && (damageSum > 0) && !attackedEnemiesDurability.containsKey(vehicle.getId()) &&
                army.getForm().isPointInDistance(vehicle.getPoint(), CustomParams.dangerRadious)) {//@TODO might be high perfomance operation
            attackedEnemiesDurability.put(vehicle.getId(), vehicle.getDurability());
        }
    }

    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
