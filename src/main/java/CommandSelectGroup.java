import model.ActionType;

import java.util.function.Consumer;

public class CommandSelectGroup extends Command {

    public CommandSelectGroup(Integer group) {

        Consumer<Command> select = (command) -> {
                MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
                MyStrategy.move.setGroup(group);
        };

        this.addCommand(new CommandWrapper(select, this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId));
    }

    public boolean check(ArmyAllyOrdering army) {
        return true;
    }

    @Override
    public void pinned() {

    }

    public void processing(SmartVehicle vehicle) {
    }
}
