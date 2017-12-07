import org.omg.PortableInterceptor.INACTIVE;

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

        if (target.vector.magnitude() < CustomParams.nearestEnemyEps) {
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
    private boolean heat = false;

    private CommandScale scale;
    private CommandRotate rotateLeft;
    private CommandRotate rotateRight;
    private CommandMove moveAttack;
    public boolean check(ArmyAllyOrdering army) {
        boolean checkresult = super.check(army);

        if (scale != null && scale.isFinished() && moveAttack == null) {
            if (rotateLeft == null) {
                army.getForm().recalc(army.getVehicles());
                rotateLeft = new CommandRotate(Math.PI/6, army.getForm().getAvgPoint(), 10);
                setParentCommand(rotateLeft);
                checkresult = false;
            } else if (rotateLeft.isFinished() && rotateRight == null) {
                rotateRight = new CommandRotate(-Math.PI/6, army.getForm().getAvgPoint(), 10);
                setParentCommand(rotateRight);
                checkresult = false;
            }

        }

        if (damageSum > 0) {//go to defence command need

            Integer inflictedDamage = 0;
            for (Map.Entry<Long, Integer> entry : attackedEnemiesDurability.entrySet()) {
                inflictedDamage += entry.getValue() - MyStrategy.getVehicles().get(entry.getKey()).getDurability();
            }
            Integer damageSumAtTick = 0;
            for (Map.Entry<Long, Integer> entry : durabilityBeforeAttack.entrySet()) {
                damageSumAtTick +=  (entry.getValue() - MyStrategy.getVehicles().get(entry.getKey()).getDurability());
            }

            if (heat == false) {
                try {

                    scale = new CommandScale(10, army.getForm().getAvgPoint());
                    getParentCommand().complete();
                    setParentCommand(scale);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                heat = true;
            } else  if ((damageSumAtTick - inflictedDamage) > 100) {
                complete();
                army.getForm().recalc(army.getVehicles());
                army.addCommand(new CommandDefence());
                return true;
            } else if ((inflictedDamage - damageSumAtTick) > 100 && (moveAttack == null)) {
                complete();
                try {
                    moveAttack = new CommandMove(target.vector.normalize().multiply(7), false );
                    setParentCommand(moveAttack);
                    checkresult = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        damageSum = 0;
        return checkresult;
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
            damageSum += (durabilityBeforeAttack.get(vehicle.getId()) - vehicle.getDurability());
        }

        if (!vehicle.isAlly() && !attackedEnemiesDurability.containsKey(vehicle.getId()) &&
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
