import model.VehicleType;

import java.util.*;

public class ArmyAllyOrdering extends ArmyAlly {


    /**
     * commands
     */
    protected Deque<Command> commandQueue;
    protected Command runningCommand;

    /**
     * behaviour tree
     */
    protected BehaviourTree behaviourTree;


    public ArmyAllyOrdering(Integer groupId) {
        super(groupId);
        commandQueue = new ArrayDeque<>();
    }

    public void setBehaviourTree(BehaviourTree behaviourTree) {
        this.behaviourTree = behaviourTree;
    }

    public void addCommand(Command command) {
        commandQueue.addLast(command);
    }

    public void addCommandToHead(Command command) {
        commandQueue.addFirst(command);
    }

    public Command pollCommand() {

        if (commandQueue.size() == 0) {
            //@TODO bad style, fix it
            List<Command> commands = behaviourTree.getAction().getCommand();
            commandQueue.addAll(commands);
            return commandQueue.poll();
        } else {
            return commandQueue.poll();
        }
    }

    public void run (BattleField battleField) throws Exception {

        this.battleField = battleField;
        if (runningCommand == null || runningCommand.isFinished()) {
            Command command = pollCommand();
            runningCommand = command.prepare(this);
        }

        if (runningCommand != null && runningCommand.isNew()) {
            runningCommand.run(this);
        }


    }

    public void check () {
        if (runningCommand != null && !runningCommand.isFinished()) {
            runningCommand.check(this);
        }

        getTrack().clearPast(Math.min(MyStrategy.world.getTickIndex() - CustomParams.trackMinTickInhistory, getLastModificateTick() - 1 ));
    }


    public boolean isRun() {
        return runningCommand != null && runningCommand.isRun();
    }

    public void result(SmartVehicle vehicle) {
        if (runningCommand.isRun()) {
            runningCommand.result(this, vehicle);
        }


        if (containVehicle(vehicle.getId())) {
            if ((vehicle.isVehicleMoved() ||  vehicle.getDurability() == 0)) {
                putVehicle(vehicle);
                getTrack().addStep(MyStrategy.world.getTickIndex(), new Step(battleField.pointTransform(vehicle.getPoint()), CustomParams.allyUnitPPFactor), vehicle.getType());

                if (vehicle.getDurability() == 0) {
                    removeVehicle(vehicle);
                }
            }
            setLastModificateTick(MyStrategy.world.getTickIndex());
        }
    }

    public Command getRunningCommand () {
        return runningCommand;
    }

    public CommandMove pathFinder(CommandMove command, TargetPoint target) throws Exception {
        getForm().recalc(getVehicles());

        getTrack().clearFuture(MyStrategy.world.getTickIndex() + 1);
        getTrack().clearPast(Math.min(MyStrategy.world.getTickIndex() - CustomParams.trackMinTickInhistory, getLastModificateTick() - 1 ));

        if (command.getTargetVector().magnitude() < 1.0) {
            return command;
        }

        Set<VehicleType> types = getVehiclesType();
        PPField sumPPFields = MyStrategy.enemyField.getVehicleTypesField(types);

        Track movingAerialArmyTrack = new Track();
        Track movingTerrainArmyTrack = new Track();

        Map<Integer, Step> lastAerialSteps = new HashMap<>();
        Map<Integer, Step> lastTerrainSteps = new HashMap<>();

        MyStrategy.commander.getDivisions().getArmyList().forEach(army -> {
            if (army.getGroupId() != getGroupId() && army.isArmyAlive()) {
                if (army.getLastModificateTick() < army.getTrack().getLastAerialTick()) {
                    movingAerialArmyTrack.addTrack(army.getTrack(), army.getTrack().getLastAerialTick());
                } else {
                    Track.sumSteps(lastAerialSteps, army.getTrack().getLastTickAerialSteps());
                }

                if (army.getLastModificateTick() < army.getTrack().getLastTerrainTick()) {
                    movingTerrainArmyTrack.addTrack(army.getTrack(), army.getTrack().getLastTerrainTick());
                } else {
                    Track.sumSteps(lastTerrainSteps, army.getTrack().getLastTickTerrainSteps());
                }
            }
        });


        SortedMap<Integer, Map<Integer, Step>> trackMap = null;

        if (types.contains(VehicleType.FIGHTER) || types.contains(VehicleType.HELICOPTER)) {
            sumPPFields.sumField(staticAerialPPField);
            trackMap = new TreeMap<>(movingAerialArmyTrack.getVehicleTypeTrack(VehicleType.FIGHTER));
            sumPPFields.addSteps(lastAerialSteps);
        }

        if (types.contains(VehicleType.TANK) || types.contains(VehicleType.IFV) || types.contains(VehicleType.ARRV)) {
            sumPPFields.sumField(staticTerrainPPField);
            sumPPFields.addSteps(lastTerrainSteps);

            if (trackMap != null) {
                trackMap = movingTerrainArmyTrack.sumTrackMap(trackMap, movingTerrainArmyTrack.getVehicleTypeTrack(VehicleType.IFV), 1);
            } else {
                trackMap = new TreeMap<>(movingTerrainArmyTrack.getVehicleTypeTrack(VehicleType.IFV));
            }
        }

        Point2D newDestPos = sumPPFields.searchPath(this, command.getTargetVector().add(getForm().getAvgPoint()), trackMap, target);

        //check limits
        newDestPos = new Point2D(Math.max((getForm().getMaxPoint().getX() - getForm().getMinPoint().getX())/2.0, newDestPos.getX()) , Math.max((getForm().getMinPoint().getY() - getForm().getMinPoint().getY())/2.0, newDestPos.getY()) );
        newDestPos = new Point2D(Math.min(MyStrategy.world.getWidth() - (getForm().getMaxPoint().getX() - getForm().getMinPoint().getX())/2.0, newDestPos.getX()) , Math.min(MyStrategy.world.getHeight() - (getForm().getMaxPoint().getY() - getForm().getMinPoint().getY())/2.0, newDestPos.getY()));

        return new CommandMove(newDestPos.subtract(getForm().getAvgPoint()));
    }

    public double getMinSpeed() {
        SmartVehicle vehicle = getForm().getEdgesVehicles().values().stream().findFirst().get();
        return vehicle.getMinSpeed();
    }
}
