
import model.ActionType;

import java.util.function.Consumer;

public class CommandNuclearAttack extends Command {

    public CommandNuclearAttack() {
        Commander.getInstance().nuclearAttack();
        targetPoint = Commander.getInstance().getNuclearAttackTarget();
    }

    protected Point2D targetPoint;

    protected int attackIndex;

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {

    }

    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {


            if (!army.getForm().isPointInVisionRange(targetPoint)) {
                army.getForm().update(army.getVehicles());

                TargetPoint targetPoint = new TargetPoint();
                targetPoint.vector = this.targetPoint.subtract(army.getForm().getAvgPoint());
                targetPoint.maxDamageValue = army.getForm().getMinDamageFactor(army) * (-1);
                army.addCommand(new CommandMove(targetPoint));
                setState(CommandStates.Complete);
                return;
            }

            Consumer<Command> nuclearAttack = (command) -> {
                SmartVehicle gunner = army.getGunnerVehicle(targetPoint);
                double visionRange = gunner.getActualVisionRange();
                if (gunner.getPoint().distance(targetPoint) > visionRange) {
                    targetPoint = targetPoint.subtract(gunner.getPoint()).normalize().multiply(visionRange - 1).add(gunner.getPoint());
                }
                MyStrategy.move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
                MyStrategy.move.setX(targetPoint.getX());
                MyStrategy.move.setY(targetPoint.getY());
                MyStrategy.move.setVehicleId(gunner.getId());
            };

            CommandWrapper cw = new CommandWrapper(this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId, CommandPriority.High);
            cw.addCommand(nuclearAttack);
            addCommand(cw);
            super.run(army);
        }
    }

    public boolean check(ArmyAllyOrdering army) {
        if (isRun() && attackIndex + MyStrategy.game.getTacticalNuclearStrikeDelay() < MyStrategy.world.getTickIndex()) {
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
