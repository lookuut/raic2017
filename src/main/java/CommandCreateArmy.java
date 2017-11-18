import model.ActionType;
import model.VehicleType;

import java.util.function.Consumer;

public class CommandCreateArmy extends Command {


    public CommandCreateArmy(AllyArmy army, VehicleType type) {
        super(army);

        this.setState(CommandStates.New);
        this.army = army;

        Consumer<Command> selectVehicleType = (command) -> {
            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            MyStrategy.move.setVehicleType(type);
            MyStrategy.move.setRight(MyStrategy.world.getWidth());
            MyStrategy.move.setBottom(MyStrategy.world.getWidth());
        };

        CommandQueue.getInstance().addCommand(new CommandWrapper(selectVehicleType, this));

        Consumer<Command> assign = (command) -> {
            MyStrategy.move.setAction(ActionType.ASSIGN);
            MyStrategy.move.setGroup(command.getArmy().getGroupId());
        };

        queue.add(selectVehicleType);
        queue.add(assign);
    }


    public void result(SmartVehicle vehicle) {
        if (vehicle.getSelected()) {
            this.army.addVehicle(vehicle);
        }
    }

    public boolean check () {
        return army.getVehicles().size() > 0;
    }
}
