
import model.ActionType;
import model.VehicleType;

import java.util.function.Consumer;

public class CommandMove extends Command {

    protected TargetPoint target;

    private int startTick;
    private int maxRunnableTick = 0;
    private boolean isDefence = true;

    public CommandMove(Point2D targetVector, boolean isDefence){
        super();
        target = new TargetPoint();
        target.vector = targetVector;
        target.maxDamageValue = null;
        this.isDefence = isDefence;
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
                    army.getForm().update(army.getVehicles());

                    PPField sumPPFields = MyStrategy.enemyField.getDamageField(army.getVehiclesType());
                    if (isDefence) {
                        if (army.isAerial()) {
                            sumPPFields.sumField(army.constAerialPPField);
                        } else {
                            sumPPFields.sumField(army.constTerrainPPField);
                        }
                    }
                    target.vector = army.pathFinder(this, target, sumPPFields);

                    if (target == null || target.vector == null) {
                        return;
                    }

                    double maxSpeed = army.getSpeed();
                    Integer maxRunnableTick = 0;
                    if (!army.getVehiclesType().contains(VehicleType.FIGHTER)) {
                        for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {
                            maxRunnableTick = Math.max(vehicle.getVehiclePointAtTick(target.vector), maxRunnableTick);
                        }
                    } else {
                        if (army.getVehicleCount() > 1) {
                            maxSpeed = army.getMinSpeed();
                        }
                        maxRunnableTick = (int)Math.ceil(target.vector.magnitude() / maxSpeed);
                    }

                    this.maxRunnableTick = maxRunnableTick;

                    MyStrategy.move.setAction(ActionType.MOVE);
                    MyStrategy.move.setX(target.vector.getX());
                    MyStrategy.move.setY(target.vector.getY());
                    MyStrategy.move.setMaxSpeed(maxSpeed);

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

    public void processing(SmartVehicle vehicle) {
    }
}
