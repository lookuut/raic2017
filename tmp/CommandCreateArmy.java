
import model.ActionType;
import model.VehicleType;

import java.util.function.Consumer;

public class CommandCreateArmy extends Command {

    private Square square;
    private VehicleType vehicleType;
    public CommandCreateArmy(Square square, VehicleType vehicleType) {
        this.square = square;
        this.vehicleType = vehicleType;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (vehicle.getSelected() && !vehicle.isHaveArmy(army) && vehicle.getArmy() == null && isRun()) {
            vehicle.addArmy(army);
            army.addVehicle(vehicle);
            vehicle.addArmy(army);

            army.setLastUpdateTick(MyStrategy.world.getTickIndex());
            army.getTrack().addStep(MyStrategy.world.getTickIndex(), new Step(army.getBattleField().pointTransform(vehicle.getPoint()), CustomParams.allyUnitPPFactor), vehicle.getType());
        }
    }

    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            Consumer<Command> selectVehicleType = (command) -> {
                MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);

                MyStrategy.move.setLeft(square.getLeftBottomAngle().getX());
                MyStrategy.move.setRight(square.getRightTopAngle().getX());
                MyStrategy.move.setTop(square.getLeftBottomAngle().getY());
                MyStrategy.move.setBottom(square.getRightTopAngle().getY());
                if (this.vehicleType != null) {
                    MyStrategy.move.setVehicleType(this.vehicleType);
                }

            };

            Consumer<Command> assign = (command) -> {
                MyStrategy.move.setAction(ActionType.ASSIGN);
                MyStrategy.move.setGroup(army.getGroupId());
            };

            CommandWrapper cw = new CommandWrapper( this, -1, CustomParams.noAssignGroupId, getPriority());
            cw.addCommand(selectVehicleType);
            cw.addCommand(assign);
            addCommand(cw);

            super.run(army);
        }
    }

    @Override
    public boolean check (ArmyAllyOrdering army) {
        if (army.getVehicles().size() > 0) {

            for (SmartVehicle vehicle : MyStrategy.getVehicles().values()) {
                if (!vehicle.isAlly()) {
                    army.setEnemy(vehicle);
                }
            }

            army.getForm().update(army.getVehicles());
            army.addCommand(new CommandScale(30));
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    @Override
    public void pinned(){
    }

    @Override
    public void processing(SmartVehicle vehicle) {
    }
}
