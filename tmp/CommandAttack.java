
import java.util.ArrayList;
import java.util.List;

public class CommandAttack extends Command {

    private TargetPoint target;
    private double movingEndDamageSum;
    private List<Point2D> movingEndCells;

    public CommandAttack () {
        movingEndCells = new ArrayList<>();
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {
        army.getForm().update(army.getVehicles());
        target = army.searchNearestEnemy();

        if (target == null) {//no enemy for vehicle
            army.addCommand(new CommandDefence());
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

        CommandMove move = new CommandMove(target, false);

        move.setPriority(CommandPriority.Middle);
        if (army.isAerial()) {
            move.setPriority(CommandPriority.High);
        }

        for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {
            Point2D transformedPoint = army.getDamageField().getTransformedPoint(vehicle.getPoint().add(target.vector));
            if (Math.floor(transformedPoint.getX()) < 0) {
                transformedPoint.setX(0.0);
            }
            if (Math.floor(transformedPoint.getY()) < 0) {
                transformedPoint.setY(0.0);
            }

            if (Math.floor(transformedPoint.getX()) >= army.getDamageField().getWidth()) {
                transformedPoint.setX(army.getDamageField().getWidth() - 1);
            }

            if (Math.floor(transformedPoint.getY()) >= army.getDamageField().getHeight()) {
                transformedPoint.setY(army.getDamageField().getHeight() - 1);
            }

            movingEndCells.add(transformedPoint);
            movingEndDamageSum += army.getDamageField().getTileFactor(transformedPoint);
        }

        setParentCommand(move);
    }

    public boolean check(ArmyAllyOrdering army) {

        if (army.isDangerousAround()) {
            army.addCommand(new CommandDefence());
            complete();
        } else if (army.isHaveTargetArmyAround(CustomParams.safetyDistance)) {
            PPFieldEnemy damageField = army.getDamageField();
            double movingEndDamageSum = 0;

            for (Point2D point2D : movingEndCells) {
                movingEndDamageSum += damageField.getTileFactor(point2D);
            }

            if (movingEndDamageSum > 0 && movingEndDamageSum > this.movingEndDamageSum + Math.abs(movingEndDamageSum) * 0.3) {
                army.addCommand(new CommandDefence());
                complete();
            }
        }

        return super.check(army);
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {

        if (!vehicle.isAlly() && SmartVehicle.isTargetVehicleType(army.getVehiclesType().iterator().next(), vehicle.getType())) {

            if (army.getForm().isPointInDistance(vehicle.getPoint(), vehicle.getAttackRange(vehicle.isAerial()))) {
                army.getForm().update(army.getVehicles());

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
