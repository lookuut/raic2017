
import model.ActionType;

import java.util.function.Consumer;

public class CommandRotate extends Command {

    private Point2D centre;
    private double angle;
    private Integer durability;

    public CommandRotate(double angle, Point2D centre, Integer durability) {
        this.centre = centre;
        this.angle = angle;
        this.durability = durability;
    }

    public CommandRotate(ArmyAllyOrdering army) {
        this.angle = army.getMaxDamageVehicleTurnedAngle(army.getTargetVehicle().getPoint());
        this.centre = army.getForm().getEdgesVehiclesCenter();//@TODO turn from nearest point of army
        SmartVehicle farestVehicle = army.getFarestVehicleFromPoint(army.getForm().getEdgesVehiclesCenter());
        double farestVehiclePathLenght = 2 * Math.abs(this.angle) * farestVehicle.getPoint().subtract(army.getForm().getEdgesVehiclesCenter()).magnitude();
        this.durability = (int)Math.ceil(farestVehiclePathLenght / army.getMinSpeed());
    }

    public boolean check (ArmyAllyOrdering army) {
        if (isRun() && getRunningTicks() > durability) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            Consumer<Command> funcRotate = (command) -> {
                MyStrategy.move.setAction(ActionType.ROTATE);
                MyStrategy.move.setX(centre.getX());
                MyStrategy.move.setY(centre.getY());
                MyStrategy.move.setAngle(angle);
                MyStrategy.move.setMaxSpeed(army.getMinSpeed());
            };
            CommandWrapper cw = new CommandWrapper( this, CustomParams.runImmediatelyTick, army.getGroupId(), getPriority());
            cw.addCommand(funcRotate);
            addCommand(cw);

            super.run(army);
        }
    }

    @Override
    public void pinned(){
        super.pinned();
    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
