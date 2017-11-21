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

        //CommandQueue.getInstance().addCommand(new CommandWrapper(selectVehicleType, this));

        Consumer<Command> assign = (command) -> {
            MyStrategy.move.setAction(ActionType.ASSIGN);
            MyStrategy.move.setGroup(command.getArmy().getGroupId());
        };

        queue.add(new CommandWrapper(selectVehicleType, this, -1));
        queue.add(new CommandWrapper(assign, this, -1));
    }


    public void result(SmartVehicle vehicle) {
        if (vehicle.getSelected()) {
            this.army.addVehicle(vehicle);
        }
    }

    public boolean check () {
        if (army.getVehicles().size() > 0) {
            setState(CommandStates.Complete);
            return true;
        } else {
            return false;
        }
    }
}
