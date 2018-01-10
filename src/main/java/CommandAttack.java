
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
            if (army.isNeedToCompact()) {
                army.addCommand(new CommandCompact());
            } else {
                army.addCommand(new CommandWait(10));
            }

            complete();
            return;
        }

        if (target.vector.magnitude() > CustomParams.pathSegmentLenght){
            target.vector = target.vector.normalize().multiply(CustomParams.pathSegmentLenght);
        }

        CommandMove move = new CommandMove(target);

        move.setPriority(CommandPriority.Middle);
        if (army.isAerial()) {
            move.setPriority(CommandPriority.High);
        }

        setParentCommand(move);
    }

    public boolean check(ArmyAllyOrdering army) {

        PPFieldEnemy damageField = army.getDamageField();

        for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {
            if (vehicle.getDurability() > 0) {
                Point2D moveDirection = target.vector.normalize().multiply(CustomParams.tileCellSize);
                Point2D transformedVehcilePoint = damageField.getTransformedPoint(vehicle.getPoint().add(moveDirection));
                if (transformedVehcilePoint.getX() < damageField.getWidth() && transformedVehcilePoint.getY() < damageField.getHeight() &&
                        transformedVehcilePoint.getX() >= 0 && transformedVehcilePoint.getY() >= 0 &&
                        damageField.getFactor(transformedVehcilePoint) >= 0) {//fuck this shit run forest run

                    army.addCommand(new CommandDefence());
                    complete();
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

                    if (army.isAerial()) {
                        CommandScale scale = new CommandScale(10);
                        CommandRotate rotate1 = new CommandRotate(Math.PI / 2, army.getForm().getAvgPoint() , 20);
                        CommandRotate rotate2 = new CommandRotate(-Math.PI / 2, army.getForm().getAvgPoint() , 20);

                        scale.setPriority(CommandPriority.High);
                        rotate1.setPriority(CommandPriority.High);
                        rotate2.setPriority(CommandPriority.High);

                        army.addCommand(scale);
                        army.addCommand(rotate1);
                        army.addCommand(rotate2);
                    }
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
