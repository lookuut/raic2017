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
            };
            addCommand(new CommandWrapper(funcRotate, this, CustomParams.runImmediatelyTick, army.getGroupId()));
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
