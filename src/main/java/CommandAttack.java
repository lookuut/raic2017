public class CommandAttack extends Command {

    private TargetPoint target;

    public CommandAttack () {
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

        if (target.vector.magnitude() > CustomParams.pathSegmentLenght){
            target.vector = target.vector.normalize().multiply(CustomParams.pathSegmentLenght);
        }

        CommandMove move = new CommandMove(target);
        setParentCommand(move);
    }

    public boolean check(ArmyAllyOrdering army) {

        PPFieldEnemy damageField = army.getDamageField();

        for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {
            if (vehicle.getDurability() > 0) {
                Point2D transformedPoint = damageField.getTransformedPoint(vehicle.getPoint());
                if (damageField.getFactor(transformedPoint) > 10) {//fuck this shit run forest run
                    complete();
                    army.addCommand(new CommandDefence());
                    break;
                }
            }
        }

        return super.check(army);
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {

        if (!vehicle.isAlly() && SmartVehicle.isTargetVehicleType(army.getVehiclesType().iterator().next(), vehicle.getType())) {

            if (army.getForm().isPointInDistance(vehicle.getPoint(), vehicle.getAttackRange(vehicle.isAerial()))) {
                army.getForm().recalc(army.getVehicles());

                Point2D enemyArmyVector = vehicle.getPoint().subtract(army.getForm().getAvgPoint());

                SmartVehicle prevVehicleState = MyStrategy.getVehiclePrevState(vehicle.getId());
                Point2D enemyMoveDirection = new Point2D(0,0);
                if (prevVehicleState != null) {
                    enemyMoveDirection = vehicle.getPoint().subtract(prevVehicleState.getPoint());
                }

                double angle = enemyArmyVector.angle(enemyMoveDirection);
                if ((angle < 180 / 6 && angle > -180 / 6) || (angle > (2 * 180 - 180 / 6) && angle < (2 * 180 + 180 / 6))) { //enemy running
                    //@TODO do something to catch them
                } else if ((angle < 180 + 180 / 6 && angle > 180  - 180 / 6)) {
                    army.addCommand(new CommandScale(10));
                    army.addCommand(new CommandRotate(Math.PI / 2, army.getForm().getAvgPoint() , 20));
                    army.addCommand(new CommandRotate(-Math.PI / 2, army.getForm().getAvgPoint() , 20));
                    complete();
                }
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
