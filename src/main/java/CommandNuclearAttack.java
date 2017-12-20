import model.ActionType;

import java.util.function.Consumer;

public class CommandNuclearAttack extends  Command {

    public CommandNuclearAttack(SmartVehicle gunner, Point2D targetPoint) {
        super();
        this.gunner = gunner;
        this.targetPoint = targetPoint;
    }

    protected SmartVehicle gunner;
    protected Point2D targetPoint;

    protected int attackIndex;

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {

            Consumer<Command> nuclearAttack = (command) -> {
                MyStrategy.move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
                MyStrategy.move.setX(targetPoint.getX());
                MyStrategy.move.setY(targetPoint.getY());
                MyStrategy.move.setVehicleId(gunner.getId());
            };

            CommandWrapper cw = new CommandWrapper(this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId, CommandPriority.Middle);
            cw.addCommand(nuclearAttack);
            addCommand(cw);
            super.run(army);
        }
    }

    public boolean check(ArmyAllyOrdering army) {
        if (attackIndex + MyStrategy.game.getTacticalNuclearStrikeDelay() > MyStrategy.world.getTickIndex()) {
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
