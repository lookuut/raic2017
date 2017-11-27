import model.ActionType;

import java.util.function.Consumer;

public class CommandScale extends Command {
    protected Integer scaleStartedIndex;
    public CommandScale() {
    }


    @Override
    public Command prepare(AllyArmy army) throws Exception {
        return this;
    }

    public void run (AllyArmy army) throws Exception {
        if (isNew()) {
            Consumer<Command> commandScale = (command) -> {
                MyStrategy.move.setAction(ActionType.SCALE);
                MyStrategy.move.setX(army.getAvgX());
                MyStrategy.move.setY(army.getAvgY());
                MyStrategy.move.setFactor(CustomParams.armyScaleFactor);
            };

            queue.add(new CommandWrapper(commandScale, this, -1));
            army.selectCommand();
            super.run(army);
        }
    }


    public boolean check (AllyArmy army) {
        if (getState() == CommandStates.Run) {
            if (MyStrategy.world.getTickIndex() - scaleStartedIndex > CustomParams.armyScaleMaxTime) {
                setState(CommandStates.Complete);
                army.recalculationMaxMin();
                return true;
            }
        }

        return false;
    }


    @Override
    public void runned(){
        this.scaleStartedIndex = MyStrategy.world.getTickIndex();
    }

    public void processing(SmartVehicle vehicle) {
    }
}
