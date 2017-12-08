import model.VehicleType;

import java.util.HashMap;
import java.util.Map;

public class CommandAttack extends Command {


    private Map<Long, Integer> durabilityBeforeAttack;
    private Integer damageSum;

    private TargetPoint target;
    private Map<Long, SmartVehicle> attackedEnemies;
    private Map<VehicleType, Integer> attackedEnemiesTypes;

    public CommandAttack () {
        durabilityBeforeAttack = new HashMap<>();
        attackedEnemies = new HashMap<>();
        attackedEnemiesTypes = new HashMap<>();
        damageSum = 0;
        attackedEnemiesTypes.put(VehicleType.HELICOPTER, 0);
        attackedEnemiesTypes.put(VehicleType.FIGHTER, 0);
        attackedEnemiesTypes.put(VehicleType.TANK, 0);
        attackedEnemiesTypes.put(VehicleType.IFV, 0);
        attackedEnemiesTypes.put(VehicleType.ARRV, 0);
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
    private boolean rageModeOn = false;

    private CommandScale scale;
    private CommandRotate rotateLeft;
    private CommandRotate rotateRight;
    private CommandMove moveAttack;
    public boolean check(ArmyAllyOrdering army) {
        boolean checkResult = super.check(army);

        if (rageModeOn == false && attackedEnemies.size() > 5 && (moveAttack == null || moveAttack.isFinished())) {
            VehicleType armyVehicleType = army.getVehiclesType().stream().findFirst().get();
            Integer victimsCount = 0;
            Integer predatorCount = 0;
            Point2D vicitmsAgvPoint = new Point2D(0,0);
            SmartVehicle attackedVictim = null;
            for (Map.Entry<Long , SmartVehicle> entry : attackedEnemies.entrySet()) {
                if (entry.getValue().getDurability() > 0) {
                    if (SmartVehicle.isVictimType(armyVehicleType, entry.getValue().getType())) {
                        victimsCount += 1;
                        vicitmsAgvPoint.setX(vicitmsAgvPoint.getX() + entry.getValue().getPoint().getX());
                        vicitmsAgvPoint.setY(vicitmsAgvPoint.getY() + entry.getValue().getPoint().getY());
                        if (attackedVictim == null) {
                            attackedVictim = entry.getValue();
                        }
                    }

                    if (SmartVehicle.isVictimType(entry.getValue().getType(), armyVehicleType)) {
                        predatorCount += 1;
                    }
                }
            }

            if (victimsCount > 0 && victimsCount < army.getVehicles().size() && predatorCount < army.getVehicles().size() / 2) {//need rage mode

                vicitmsAgvPoint.setX(vicitmsAgvPoint.getX()/ victimsCount);
                vicitmsAgvPoint.setY(vicitmsAgvPoint.getY()/ victimsCount);

                SmartVehicle previousEnemyState = MyStrategy.getVehiclePrevState(attackedVictim.getId());
                Point2D enemyMoveDirection = attackedVictim.getPoint().subtract(previousEnemyState.getPoint());
                enemyMoveDirection = enemyMoveDirection.multiply(20);

                army.getForm().recalc(army.getVehicles());

                Point2D enemyVector = vicitmsAgvPoint.subtract(army.getForm().getAvgPoint());
                Point2D enemyFuturePoint = enemyMoveDirection.add(vicitmsAgvPoint);
                Point2D moveAttackVector = enemyFuturePoint.subtract(army.getForm().getAvgPoint());

                if (moveAttackVector.angle(enemyVector) > Math.PI * (5 / 6) && moveAttackVector.angle(enemyVector) < Math.PI * (7 / 6)) {//enemy move to our side just scale and wait
                    scale = new CommandScale(15, army.getForm().getAvgPoint());
                    getParentCommand().complete();
                    setParentCommand(scale);
                } else {// go to forward to enemy
                    moveAttackVector.multiplySelf(1.4);
                    moveAttack = new CommandMove(moveAttackVector, false );
                    setParentCommand(moveAttack);
                }
                rageModeOn = true;
                checkResult = false;
            }
        }

        /*
        if (scale != null && scale.isFinished() && moveAttack == null && army.isAerial()) {

            if (rotateLeft == null) {
                army.getForm().recalc(army.getVehicles());
                rotateLeft = new CommandRotate(Math.PI/6, army.getForm().getAvgPoint(), 10);
                setParentCommand(rotateLeft);
                checkResult = false;
            } else if (rotateLeft.isFinished() && rotateRight == null) {
                rotateRight = new CommandRotate(-Math.PI/6, army.getForm().getAvgPoint(), 10);
                setParentCommand(rotateRight);
                checkResult = false;
            }

        }*/

        if (damageSum > 0 && (rageModeOn == false)) {//go to defence command need
            Integer inflictedDamage = 0;
            for (Map.Entry<Long, SmartVehicle> entry : attackedEnemies.entrySet()) {
                inflictedDamage += entry.getValue().getDurability() - MyStrategy.getVehicles().get(entry.getKey()).getDurability();
            }
            Integer damageSumAtTick = 0;
            for (Map.Entry<Long, Integer> entry : durabilityBeforeAttack.entrySet()) {
                damageSumAtTick +=  (entry.getValue() - MyStrategy.getVehicles().get(entry.getKey()).getDurability());
            }

            if (heat == false) {
                try {
                    Integer victimsCount = 0;
                    Integer predatorCount = 0;
                    for (Map.Entry<VehicleType, Integer> entry : attackedEnemiesTypes.entrySet()) {
                        if (SmartVehicle.isVictimType(entry.getKey(),  army.getVehiclesType().iterator().next())) {
                            predatorCount += entry.getValue();
                        }
                        if (SmartVehicle.isVictimType( army.getVehiclesType().iterator().next(), entry.getKey())) {
                            victimsCount += entry.getValue();
                        }
                    }
                    if (predatorCount > victimsCount && predatorCount > army.getVehicleCount() * 0.75) {
                        complete();
                        army.addCommand(new CommandDefence());
                        return true;
                    } else if (predatorCount > army.getVehicleCount() * 0.3) {
                        getParentCommand().complete();
                        scale = new CommandScale(10, army.getForm().getAvgPoint());
                        setParentCommand(scale);
                        checkResult = false;
                    }

                } catch (Exception e) {
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
                    checkResult = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        damageSum = 0;
        return checkResult;
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
            damageSum += (durabilityBeforeAttack.get(vehicle.getId()) - vehicle.getDurability());
        }

        if (!vehicle.isAlly()
                &&
                army.getForm().isPointInDistance(vehicle.getPoint(),  CustomParams.enemyVisionRange)) {//@TODO might be high perfomance operation

            if (!attackedEnemies.containsKey(vehicle.getId())) {
                attackedEnemies.put(vehicle.getId(), vehicle);
                attackedEnemiesTypes.put(vehicle.getType(), attackedEnemiesTypes.get(vehicle.getType()) + 1);
            }

            if (vehicle.getDurability() == 0) {
                attackedEnemiesTypes.put(vehicle.getType(), attackedEnemiesTypes.get(vehicle.getType()) - 1);
            }
        }
    }

    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
