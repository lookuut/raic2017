import model.ActionType;
import model.Vehicle;
import model.VehicleType;

import java.util.function.Consumer;

public class CommandCreateArmy extends Command {

    protected VehicleType type;
    public CommandCreateArmy(VehicleType type) {
        this.type = type;
    }


    public void result(AllyArmy army, SmartVehicle vehicle) {
        if (vehicle.getSelected()) {
            army.addVehicle(vehicle);
        }
    }

    public void run(AllyArmy army) {
        if (isNew()) {
            Consumer<Command> selectVehicleType = (command) -> {
                MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);

                if (type != null) {
                    MyStrategy.move.setVehicleType(type);
                }

                MyStrategy.move.setRight(MyStrategy.world.getWidth());
                MyStrategy.move.setBottom(MyStrategy.world.getWidth());
            };

            Consumer<Command> assign = (command) -> {
                MyStrategy.move.setAction(ActionType.ASSIGN);
                MyStrategy.move.setGroup(army.getGroupId());
            };

            queue.add(new CommandWrapper(selectVehicleType, this, -1));
            queue.add(new CommandWrapper(assign, this, -1));

            super.run(army);
        }
    }

    @Override
    public boolean check (AllyArmy army) {
        if (army.getVehicles().size() > 0) {
            setState(CommandStates.Complete);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void runned(){

    }
}
