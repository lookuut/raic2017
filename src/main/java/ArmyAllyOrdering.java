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

        getTrack().clearPast(MyStrategy.world.getTickIndex() - CustomParams.trackMinTickInhistory);
    }


    public boolean isRun() {
        return runningCommand != null && runningCommand.isRun();
    }

    public void result(SmartVehicle vehicle) {
        if (runningCommand.isRun()) {
            runningCommand.result(this, vehicle);
        }

        if (containVehicle(vehicle.getId()) && (vehicle.isVehicleMoved() ||  vehicle.getDurability() == 0)) {
            setLastModificateTick(MyStrategy.world.getTickIndex());
            getTrack().addStep(MyStrategy.world.getTickIndex(), new Step(battleField.pointTransform(vehicle.getPoint()), CustomParams.allyUnitPPFactor), vehicle.getType());

            if (vehicle.getDurability() == 0) {
                removeVehicle(vehicle);
            }
        }
    }

    public Command getRunningCommand () {
        return runningCommand;
    }

    public CommandMove pathFinder (CommandMove command) throws Exception {
        getForm().recalc(getVehicles());

        Set<VehicleType> types = getVehiclesType();

        PPField sumPPFields = MyStrategy.enemyField.getVehicleTypesField(types);
        getTrack().clearFuture(MyStrategy.world.getTickIndex() + 1);

        Track resultTrack = new Track();
        Track endResultTrack = new Track();
        MyStrategy.commander.getDivisions().values().stream().forEach(army -> {
            resultTrack.addTrack(army.getTrack(), army.getLastModificateTick());

            if (army.getGroupId() != this.getGroupId()) {
                endResultTrack.addLastTick(army.getTrack());
            }
        });


        SortedMap<Integer, Map<Integer, Step>> trackMap = null;

        if (types.contains(VehicleType.FIGHTER) || types.contains(VehicleType.HELICOPTER)) {
            sumPPFields.sumField(staticAerialPPField);
            trackMap = resultTrack.getVehicleTypeTrack(VehicleType.FIGHTER);
            sumPPFields.addAerialTrack(endResultTrack);
        }

        if (types.contains(VehicleType.TANK) || types.contains(VehicleType.IFV) || types.contains(VehicleType.ARRV)) {
            sumPPFields.sumField(staticTerrainPPField);
            sumPPFields.addTerrainTrack(endResultTrack);

            if (trackMap != null) {
                trackMap = resultTrack.sumTrackMap(trackMap, resultTrack.getVehicleTypeTrack(VehicleType.IFV), getLastModificateTick(), 1);
            } else {
                trackMap = resultTrack.getVehicleTypeTrack(VehicleType.IFV);
            }
        }

        Point2D newDestPos = sumPPFields.searchPath(this, command.getTargetPosition(), trackMap);

        //check limits
        newDestPos = new Point2D(Math.max((getForm().getMaxPoint().getX() - getForm().getMinPoint().getX())/2.0, newDestPos.getX()) , Math.max((getForm().getMinPoint().getY() - getForm().getMinPoint().getY())/2.0, newDestPos.getY()) );
        newDestPos = new Point2D(Math.min(MyStrategy.world.getWidth() - (getForm().getMaxPoint().getX() - getForm().getMinPoint().getX())/2.0, newDestPos.getX()) , Math.min(MyStrategy.world.getHeight() - (getForm().getMaxPoint().getY() - getForm().getMinPoint().getY())/2.0, newDestPos.getY()));

        return new CommandMove(newDestPos);
    }
}
