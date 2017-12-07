import model.ActionType;

import java.util.function.Consumer;

public class CommandScale extends Command {
    private Integer scaleDurability;
    private Point2D scalePoint;
    public CommandScale(Integer scaleDurability) {
        this.scaleDurability = scaleDurability;
        this.scalePoint = null;
    }

    public CommandScale(Integer scaleDurability, Point2D scalePoint) {
        this.scaleDurability = scaleDurability;
        this.scalePoint = scalePoint;
    }


    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void run (ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            Consumer<Command> commandScale = (command) -> {
                MyStrategy.move.setAction(ActionType.SCALE);

                if (scalePoint == null) {
                    army.getForm().recalc(army.getVehicles());
                    MyStrategy.move.setX(army.getForm().getAvgPoint().getX());
                    MyStrategy.move.setY(army.getForm().getAvgPoint().getY());
                } else {
                    MyStrategy.move.setX(scalePoint.getX());
                    MyStrategy.move.setY(scalePoint.getY());
                }

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
