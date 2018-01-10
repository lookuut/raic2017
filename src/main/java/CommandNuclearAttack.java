import model.ActionType;

import java.util.function.Consumer;

public class CommandNuclearAttack extends Command {

    public CommandNuclearAttack() {
        super();
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
                army.getForm().recalc(army.getVehicles());
                army.addCommand(new CommandMove(targetPoint.subtract(army.getForm().getAvgPoint()), false));
                setState(CommandStates.Complete);
                return;
            }

            /*
            if (army.getForm().isDamagedByNuclearAttack(Commander.getInstance().getNuclearAttackTarget())) {
                army.getForm().recalc(army.getVehicles());
                army.addCommand(new CommandMove(army.getForm().getAvgPoint().subtract(targetPoint)));
                setState(CommandStates.Complete);
                return;
            }*/

            Consumer<Command> nuclearAttack = (command) -> {
                MyStrategy.move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
                MyStrategy.move.setX(targetPoint.getX());
                MyStrategy.move.setY(targetPoint.getY());
                MyStrategy.move.setVehicleId(army.getGunnerVehicle(targetPoint).getId());
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
