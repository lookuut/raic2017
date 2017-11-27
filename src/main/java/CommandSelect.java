import model.ActionType;

import java.util.function.Consumer;

public class CommandSelect extends Command {

    public CommandSelect(AllyArmy army) {

        Consumer<Command> select = (command) -> {

            if (!army.isSelected()) {
                MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
                MyStrategy.move.setGroup(army.getGroupId());
                army.selected();
            }
        };

        queue.add(new CommandWrapper(select, this, -1));
    }

    public boolean check(AllyArmy army) {
        return true;
    }

    @Override
    public void runned() {

    }

    public void processing(SmartVehicle vehicle) {
    }
}
