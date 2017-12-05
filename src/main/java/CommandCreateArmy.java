import model.ActionType;
import model.VehicleType;

import java.util.function.Consumer;

public class CommandCreateArmy extends Command {

    private Square square;
    public CommandCreateArmy(Square square) {
        this.square = square;
    }


    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (vehicle.getSelected() && !vehicle.isHaveArmy(army)) {
            vehicle.addArmy(army);
            army.addVehicle(vehicle);
            army.setLastModificateTick(MyStrategy.world.getTickIndex());
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
            };

            Consumer<Command> assign = (command) -> {
                MyStrategy.move.setAction(ActionType.ASSIGN);
                MyStrategy.move.setGroup(army.getGroupId());
            };

            Consumer<Command> scale = (command) -> {

                Point2D centre = new Point2D(
                        square.getLeftBottomAngle().getX() + (square.getRightTopAngle().getX() - square.getLeftBottomAngle().getX()) / 2 ,
                        square.getLeftBottomAngle().getY() + (square.getRightTopAngle().getY() - square.getLeftBottomAngle().getY()) / 2
                );
                MyStrategy.move.setAction(ActionType.SCALE);
                MyStrategy.move.setX(centre.getX());
                MyStrategy.move.setY(centre.getY());
                MyStrategy.move.setFactor(CustomParams.armyScaleFactor);
            };

            addCommand(new CommandWrapper(selectVehicleType, this, -1, CustomParams.noAssignGroupId));
            addCommand(new CommandWrapper(assign, this, -1, CustomParams.noAssignGroupId));
            addCommand(new CommandWrapper(scale, this, -1, CustomParams.noAssignGroupId));

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

            army.addCommand(new CommandWait(CustomParams.armyAfterCreateTimeWait));
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
