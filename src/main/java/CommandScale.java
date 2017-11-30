import model.ActionType;

import java.util.function.Consumer;

public class CommandScale extends Command {
    protected Integer scaleStartedIndex;
    public CommandScale() {
    }


    @Override
    public Command prepare(ArmyAllyOrdering army) throws Exception {
        return this;
    }

    public void run (ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            Consumer<Command> commandScale = (command) -> {
                MyStrategy.move.setAction(ActionType.SCALE);

                MyStrategy.move.setX(army.getForm().getAvgPoint().getX());
                MyStrategy.move.setY(army.getForm().getAvgPoint().getY());
                MyStrategy.move.setFactor(CustomParams.armyScaleFactor);
            };

            addCommand(new CommandWrapper(commandScale, this, CustomParams.runImmediatelyTick, army.getGroupId()));

            super.run(army);
        }
    }


    public boolean check (ArmyAllyOrdering army) {
        if (getState() == CommandStates.Run) {
            if (MyStrategy.world.getTickIndex() - scaleStartedIndex > CustomParams.armyScaleMaxTime) {
                setState(CommandStates.Complete);
                army.getForm().recalc(army.getVehicles());
                return true;
            }
        }

        return false;
    }


    @Override
    public void pinned(){
        this.scaleStartedIndex = MyStrategy.world.getTickIndex();
    }

    public void processing(SmartVehicle vehicle) {
    }
}
