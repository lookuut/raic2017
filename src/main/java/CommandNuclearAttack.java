import model.ActionType;

import java.util.function.Consumer;

public class CommandNuclearAttack extends  Command{
    public CommandNuclearAttack(AllyArmy army, double x, double y) {
        super(army);

        Consumer<Command> nuclearAttack = (command) -> {
            SmartVehicle vehicle = army.getNearestVehicle(x,y);
            MyStrategy.move.setAction(ActionType.TACTICAL_NUCLEAR_STRIKE);
            MyStrategy.move.setX(x);
            MyStrategy.move.setY(y);
            MyStrategy.move.setVehicleId(vehicle.getId());
        };

        queue.add(new CommandWrapper(selectArmy, this, -1));
        queue.add(new CommandWrapper(nuclearAttack, this, -1));
    }
}
