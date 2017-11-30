import model.ActionType;
import model.VehicleType;

import java.util.function.Consumer;

public class CommandCreateArmy extends Command {

    protected VehicleType type;
    public CommandCreateArmy(VehicleType type) {
        this.type = type;
    }


    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (vehicle.getSelected() && !vehicle.isHaveArmy(army)) {
            vehicle.addArmy(army);
            army.addVehicle(vehicle);
            army.getTrack().addStep(MyStrategy.world.getTickIndex(), new Step(army.getBattleField().pointTransform(vehicle.getPoint()), CustomParams.allyUnitPPFactor), vehicle.getType());
        }
    }

    public void run(ArmyAllyOrdering army) throws Exception {
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

            addCommand(new CommandWrapper(selectVehicleType, this, -1, CustomParams.noAssignGroupId));
            addCommand(new CommandWrapper(assign, this, -1, CustomParams.noAssignGroupId));

            super.run(army);
        }
    }

    @Override
    public boolean check (ArmyAllyOrdering army) {
        if (army.getVehicles().size() > 0) {
            setState(CommandStates.Complete);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void pinned(){
    }

    @Override
    public void processing(SmartVehicle vehicle) {
    }
}
