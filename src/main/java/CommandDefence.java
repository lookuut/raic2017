
public class CommandDefence extends Command {
    public CommandDefence () {
        super();
    }

    private CommandMove moveToSafePointCommand;

    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            army.getForm().recalc(army.getVehicles());
            Point2D point = army.dangerPoint();

            if (point == null) {//danger is gone, relax take it easy
                setState(CommandStates.Complete);
                return;
            }

            point = MyStrategy.enemyField.searchNearestSafetyPoint(army.getVehiclesType(), army.getForm().getAvgPoint(), point);

            if (point == null) {
                throw new Exception("Mistake call defence");
            }

            moveToSafePointCommand = new CommandMove(point);
            moveToSafePointCommand.run(army);
            setState(CommandStates.Run);
        }
    }

    public boolean check(ArmyAllyOrdering army) {
        if (moveToSafePointCommand.check(army)) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    public void pinned() {
        return;
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
