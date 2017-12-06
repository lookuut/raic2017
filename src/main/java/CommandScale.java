import model.ActionType;

import java.util.function.Consumer;

public class CommandScale extends Command {
    private Integer scaleDurability;
    public CommandScale(Integer scaleDurability) {
        this.scaleDurability = scaleDurability;
    }


    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void run (ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            Consumer<Command> commandScale = (command) -> {
                MyStrategy.move.setAction(ActionType.SCALE);
                army.getForm().recalc(army.getVehicles());
                MyStrategy.move.setX(army.getForm().getAvgPoint().getX());
                MyStrategy.move.setY(army.getForm().getAvgPoint().getY());
                MyStrategy.move.setFactor(CustomParams.armyScaleFactor);
            };

            addCommand(new CommandWrapper(commandScale, this, CustomParams.runImmediatelyTick, army.getGroupId()));

            super.run(army);
        }
    }


    public boolean check (ArmyAllyOrdering army) {
        if (isRun() && getRunningTicks() > scaleDurability) {
            complete();
            return true;
        }
        return false;
    }

    public void processing(SmartVehicle vehicle) {
    }
}
