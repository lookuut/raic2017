import model.ActionType;

import java.util.function.Consumer;

public class CommandSelect extends Command {

    public CommandSelect(int groupId) {

        Consumer<Command> select = (command) -> {
            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            MyStrategy.move.setGroup(groupId);
        };

        queue.add(new CommandWrapper(select, this, -1));
    }

    public boolean check(AllyArmy army) {
        return true;
    }

    @Override
    public void runned(){

    }
}
