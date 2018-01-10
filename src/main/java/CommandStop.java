import model.ActionType;

import java.util.function.Consumer;

public class CommandStop extends Command {

    public CommandStop() {
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void run (ArmyAllyOrdering army) throws Exception {
        if (isNew()) {

            Consumer<Command> commandStop = (command) -> {
                MyStrategy.move.setAction(ActionType.MOVE);
                MyStrategy.move.setX(army.getForm().getAvgPoint().getX());
                MyStrategy.move.setY(army.getForm().getAvgPoint().getY());
                MyStrategy.move.setMaxSpeed(0.0000001);
            };

            CommandWrapper cw = new CommandWrapper( this, CustomParams.runImmediatelyTick, army.getGroupId(), getPriority());
            cw.addCommand(commandStop);
            addCommand(cw);

            super.run(army);
        }
    }

    public boolean check (ArmyAllyOrdering army) {
        return getState() == CommandStates.Complete;
    }

    public void processing(SmartVehicle vehicle) {
    }

    public void pinned() {
        super.pinned();
        setState(CommandStates.Complete);
    }
}
