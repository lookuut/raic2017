import geom.Point2D;
import model.ActionType;

import java.util.function.Consumer;

public class CommandNuclearAttack extends  Command {

    public CommandNuclearAttack() {
        super();
        attackIndex = 0;
    }
    protected SmartVehicle gunner;

    protected Point2D targetSafetyPos;
    protected Point2D targetPoint;

    protected int attackIndex;


    public Command prepare(ArmyAllyOrdering army) throws Exception {
        try {
            army.getForm().recalc(army.getVehicles());
            targetPoint = army.getNuclearAttackTarget();
            gunner = army.getNearestVehicle(targetPoint);
            targetSafetyPos = army.getNearestSafetyPointForVehicle(gunner, targetPoint);

            Point2D targetVector = targetPoint.subtract(targetSafetyPos);
            double targetDistace = targetVector.magnitude();

            if (targetDistace == 0) {
                targetSafetyPos = targetPoint.multiply( 1 - gunner.getVisionRange()/targetPoint.magnitude());
            } else if (targetDistace < gunner.getVisionRange()) {
                targetSafetyPos = targetSafetyPos.subtract( targetVector.multiply((gunner.getVisionRange() - targetVector.magnitude()) / gunner.getVisionRange())).subtract(new Point2D(1,1) );
            }

            Point2D armySize = army.getForm().getArmySize();

            Point2D armyMaxPoint = targetSafetyPos.add(new Point2D(armySize.getX() / 2.0, armySize.getY() / 2.0));
            if (armyMaxPoint.getX() > MyStrategy.world.getWidth() || armyMaxPoint.getY() > MyStrategy.world.getHeight()) {
                targetSafetyPos = new Point2D(
                        Math.min(MyStrategy.world.getWidth() - armySize.getX() / 2.0 , targetSafetyPos.getX()),
                        Math.min(MyStrategy.world.getHeight() - armySize.getY() / 2.0 , targetSafetyPos.getY())
                );
            }

            if (armyMaxPoint.getX() <= 0 || armyMaxPoint.getY() < 0) {
                targetSafetyPos = new Point2D(
                        Math.max(-armySize.getX() / 2.0 , targetSafetyPos.getX()),
                        Math.max(-armySize.getY() / 2.0 , targetSafetyPos.getY())
                );
            }

            if (targetSafetyPos.getX() > MyStrategy.world.getWidth() || targetSafetyPos.getY() > MyStrategy.world.getHeight()) {
                System.out.println("dawdawdawd");
            }

            if (gunner.getX() >= targetSafetyPos.getX() - 2 && gunner.getX() <= targetSafetyPos.getX() + 2
                    &&
                    gunner.getY() >= targetSafetyPos.getY() - 2 && gunner.getY() <= targetSafetyPos.getY() + 2
                    ) {
                return this;
            }

            Point2D newPoint = targetSafetyPos.subtract(gunner.getPoint()).add(army.getForm().getAvgPoint());
            armyMaxPoint = newPoint.add(new Point2D(armySize.getX() / 2.0, armySize.getY() / 2.0));

            if (armyMaxPoint.getX() > MyStrategy.world.getWidth() || armyMaxPoint.getY() > MyStrategy.world.getHeight()) {
                newPoint = new Point2D(
                        Math.min(MyStrategy.world.getWidth() - armySize.getX() / 2.0 , newPoint.getX()),
                        Math.min(MyStrategy.world.getHeight() - armySize.getY() / 2.0 , newPoint.getY())
                );
            }

            army.addCommandToHead(this);

            if (army.getForm().getAvgPoint().subtract(newPoint).magnitude() < 150) {
                return new CommandMove(new Point2D(Math.ceil(newPoint.getX()),Math.ceil(newPoint.getY())));
            }

            return army.pathFinder(new CommandMove(new Point2D(Math.ceil(newPoint.getX()),Math.ceil(newPoint.getY()))));

        } catch (Exception e) {
            e.printStackTrace();
        }
        setState(CommandStates.Failed);
        return new CommandAttack();
    }

    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            double visionRange = gunner.getVisionRange();

            double length = targetPoint.subtract(gunner.getPoint()).magnitude();
            Point2D point;

            if (length > visionRange) {
                point = gunner.getPoint().add( targetPoint.subtract(gunner.getPoint()).multiply((visionRange)/ length) ).subtract(new Point2D(1,1));
            } else {
                point = targetPoint;
            }

            Consumer<Command> nuclearAttack = (command) -> {
                MyStrategy.move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
                MyStrategy.move.setX(point.getX());
                MyStrategy.move.setY(point.getY());
                MyStrategy.move.setVehicleId(gunner.getId());
            };

            addCommand(new CommandWrapper(nuclearAttack, this, CustomParams.runImmediatelyTick, army.getGroupId()));
            super.run(army);
        }
    }

    public boolean check(ArmyAllyOrdering army) {

        if (attackIndex > 0 && attackIndex + MyStrategy.game.getTacticalNuclearStrikeDelay() + 1 < MyStrategy.world.getTickIndex()) {
            setState(CommandStates.Complete);
            return true;
        }
        return false;
    }

    @Override
    public void pinned() {
        attackIndex = MyStrategy.world.getTickIndex();
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
