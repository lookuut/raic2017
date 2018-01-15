package strategy;

public class CommanderTask {
    private ArmyAllyOrdering army;
    private Command command;
    public CommanderTask(ArmyAllyOrdering army, Command command) {
        this.army = army;
        this.command = command;
    }

    public void run () throws Exception {
        if (command.isNew()) {
            command.run(army);
        }
    }

    public boolean check () {
        return command.check(army);
    }

    public ArmyAllyOrdering getArmy() {
        return army;
    }


    public void result (SmartVehicle vehicle) {
        command.result(army, vehicle);
    }

    public Command getCommand() {
        return command;
    }
}
