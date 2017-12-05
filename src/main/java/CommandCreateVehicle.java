import model.ActionType;
import model.VehicleType;

import java.util.function.Consumer;

public class CommandCreateVehicle extends Command{
    private Long facilityId;
    private VehicleType vehicleType;

    public CommandCreateVehicle(Long facilityId, VehicleType vehicleType) {
        this.facilityId = facilityId;
        this.vehicleType = vehicleType;
    }

    public boolean check (ArmyAllyOrdering army) {
        setState(CommandStates.Complete);
        return true;
    }


    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {

            Consumer<Command> funcMove = (command) -> {
                MyStrategy.move.setAction(ActionType.SETUP_VEHICLE_PRODUCTION);
                MyStrategy.move.setFacilityId(this.facilityId);
                MyStrategy.move.setVehicleType(this.vehicleType);
            };

            addCommand(new CommandWrapper(funcMove, this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId));
            super.run(army);
        }
    }
    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }

}
