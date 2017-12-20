import model.ActionType;

import java.util.function.Consumer;

public class CommandMove extends Command {

    protected TargetPoint target;

    private int startTick;
    private int maxRunnableTick = 0;
    private boolean withPathFinder = true;

    public CommandMove(Point2D targetVector){
        super();
        target = new TargetPoint();
        target.vector = targetVector;
        target.maxDamageValue = 0;
    }

    public CommandMove(Point2D targetVector, boolean withPathFinder) {
        super();
        this.withPathFinder = withPathFinder;
        target = new TargetPoint();
        target.vector = targetVector;
        target.maxDamageValue = 0;
    }

    public CommandMove(TargetPoint target){
        super();
        this.target = target;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    @Override
    public boolean check (ArmyAllyOrdering army) {

        if (getState() == CommandStates.Complete || getState() == CommandStates.Failed) {
            return true;
        }

        if (getState() == CommandStates.Hold) {
            return false;
        }

        if (isRun() && maxRunnableTick + startTick <= MyStrategy.world.getTickIndex()) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    public void run(ArmyAllyOrdering army) throws Exception {


        if (isNew()) {

            if (target.vector.magnitude() == 0) {//already at point
                setState(CommandStates.Complete);
                return;
            }

            Consumer<Command> funcMove = (command) -> {
                try {
                    army.getForm().recalc(army.getVehicles());

                    if (withPathFinder) {
                        target.vector = army.pathFinder(this, target);
                    }

                    if (target == null || target.vector == null) {
                        return;
                    }

                    Integer maxRunnableTick = 0;
                    for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {
                        if (vehicle.getDurability() > 0) {
                            Integer vehicleRunableTick = Math.max(vehicle.getVehiclePointAtTick(target.vector), 1);
                            if (maxRunnableTick < vehicleRunableTick) {
                                maxRunnableTick = vehicleRunableTick;
                            }
                        }
                    }

                    this.maxRunnableTick = maxRunnableTick;

                    MyStrategy.move.setAction(ActionType.MOVE);
                    MyStrategy.move.setX(target.vector.getX());
                    MyStrategy.move.setY(target.vector.getY());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            CommandWrapper cw = new CommandWrapper(this, CustomParams.runImmediatelyTick, army.getGroupId(), getPriority());
            cw.addCommand(funcMove);
            addCommand(cw);
            super.run(army);
        }
    }

    public void result(ArmyAllyOrdering army, SmartVehicle vehicle) {
        if (army.containVehicle(vehicle.getId())) {
            army.putVehicle(vehicle);
        }
    }

    @Override
    public void pinned() {
        startTick = MyStrategy.world.getTickIndex();
    }

    public Point2D getTargetVector() {
        return target.vector;
    }

    public Integer getMaxRunnableTick () {
        return maxRunnableTick;
    }

    public void processing(SmartVehicle vehicle) {
    }
}
