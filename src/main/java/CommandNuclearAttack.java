import model.ActionType;

import java.util.function.Consumer;

public class CommandNuclearAttack extends  Command {

    public CommandNuclearAttack() {
        super();
        attackIndex = 0;
    }
    protected SmartVehicle gunner;

    protected double targetPositionX;
    protected double targetPositionY;
    protected int attackIndex;

    protected double targetX;
    protected double targetY;

    public Command prepare(AllyArmy army) throws Exception {
        try {
            army.recalculationMaxMin();
            double[] targetXY = army.getNuclearAttackTarget();

            targetX = targetXY[0];
            targetY = targetXY[1];

            gunner = army.getNearestVehicle(targetX, targetY);

            double[] result = army.getNearestSafetyPointForVehicle(gunner, targetX, targetY);
            targetPositionX = result[0];
            targetPositionY = result[1];


            double targetDistace = Math.sqrt((targetX - targetPositionX) * (targetX - targetPositionX) + (targetY - targetPositionY) * (targetY - targetPositionY));

            if (targetDistace < gunner.getVisionRange()) {
                targetPositionX = targetPositionX - (targetX - targetPositionX) * (gunner.getVisionRange() / targetDistace - 1);
                targetPositionY = targetPositionY - (targetY - targetPositionY) * (gunner.getVisionRange() / targetDistace - 1);
            }
            if (army.isOnCoordinates(targetPositionX, targetPositionY)) {
                return this;
            } else {
                double localX = targetPositionX - gunner.getX() + army.getAvgX();
                double localY = targetPositionY - gunner.getY() + army.getAvgY();

                army.addCommandToHead(this);
                return army.pathFinder(new CommandMove(localX, localY));
            }
        } catch (Exception e) {
            army.printEnemyField();
            e.printStackTrace();
        }
        setState(CommandStates.Failed);
        return new CommandAttack();
    }

    public void run(AllyArmy army) {
        if (isNew()) {
            army.select();
            double visionRange = army.getVehicleVisionRange(gunner);

            double vectorX = targetX - gunner.getX();
            double vectorY = targetY - gunner.getY();
            double length = Math.sqrt(vectorX * vectorX + vectorY * vectorY);

            double purposeX = gunner.getX() + vectorX * (visionRange)/ length - 1;
            double purposeY = gunner.getY() + vectorY * (visionRange)/ length - 1;

            Consumer<Command> nuclearAttack = (command) -> {
                MyStrategy.move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
                MyStrategy.move.setX(purposeX);
                MyStrategy.move.setY(purposeY);
                MyStrategy.move.setVehicleId(gunner.getId());
            };

            queue.add(new CommandWrapper(nuclearAttack, this, -1));
            super.run(army);
        }
    }

    public boolean check(AllyArmy army) {

        if (attackIndex > 0 && attackIndex + MyStrategy.game.getTacticalNuclearStrikeDelay() + 1 < MyStrategy.world.getTickIndex()) {
            setState(CommandStates.Complete);
            return true;
        }
        return false;
    }

    @Override
    public void runned() {
        attackIndex = MyStrategy.world.getTickIndex();
    }
}
