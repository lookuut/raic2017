import model.ActionType;
import model.Player;

import java.util.function.Consumer;

public class CommandNuclearDefence extends Command {
    protected Integer attackTick;
    protected double attackX;
    protected double attackY;

    public CommandNuclearDefence() {
        super();
    }

    public void run (ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            Consumer<Command> commandSelect = (command) -> {
                Player player = MyStrategy.nuclearAttack();
                attackTick = player.getNextNuclearStrikeTickIndex();
                MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
                attackX = player.getNextNuclearStrikeX();
                attackY = player.getNextNuclearStrikeY();
                MyStrategy.move.setRight(attackX + MyStrategy.game.getTacticalNuclearStrikeRadius());
                MyStrategy.move.setLeft(attackX - MyStrategy.game.getTacticalNuclearStrikeRadius());

                MyStrategy.move.setTop(attackY - MyStrategy.game.getTacticalNuclearStrikeRadius());
                MyStrategy.move.setBottom(attackY + MyStrategy.game.getTacticalNuclearStrikeRadius());
            };

            Consumer<Command> commandDefence = (command) -> {
                MyStrategy.move.setAction(ActionType.SCALE);
                MyStrategy.move.setX(attackX);
                MyStrategy.move.setY(attackY);
                MyStrategy.move.setFactor(5);
            };


            addCommand(new CommandWrapper(commandSelect, this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId));
            addCommand(new CommandWrapper(commandDefence, this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId));
            super.run(army);
        }
    }

    public boolean check (ArmyAllyOrdering army) {
        if (getState() == CommandStates.Run && MyStrategy.world.getTickIndex() >= this.attackTick + MyStrategy.game.getTacticalNuclearStrikeDelay() + 50) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
