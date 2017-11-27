import javafx.geometry.Point2D;
import model.VehicleType;

import java.util.*;
import java.util.List;

public class AllyArmy extends Army {

    /**
     * commands
     */
    protected Deque<Command> commandQueue;
    protected Command runningCommand;

    /**
     * behaviour tree
     */
    protected BehaviourTree behaviourTree;

    protected Map<Long, SmartVehicle> vehicles;
    protected Integer groupId;

    /**
     * battle fields
     */
    protected BattleField battleField;

    /**
     *  PPField
     */
    protected PPField staticAerialPPField;
    protected PPField staticTerrainPPField;

    protected PPField aerialPPField;
    protected PPField terrainPPField;

    protected boolean isReadyToNuclearAttack = false;
    protected SmartVehicle nuclearVehicle;
    public AllyArmy() {
        super();
        vehicles = new HashMap<>();
        commandQueue = new ArrayDeque<>();
    }

    public void setBehaviourTree(BehaviourTree behaviourTree) {
        this.behaviourTree = behaviourTree;
    }

    public void setGroupId (Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupId () {
        return groupId;
    }

    public Map<Long, SmartVehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle (SmartVehicle vehicle) {
        minMaxUpdate(vehicle);
        this.putVehicle(vehicle);
    }

    public void putVehicle(SmartVehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
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
    }

    public CommandMove pathFinder (CommandMove command) throws Exception {
        double destX = command.getX();
        double destY = command.getY();

        recalculationMaxMin();
        double fromX = getAvgX();
        double fromY = getAvgY();

        refreshPPFields();

        HashSet<VehicleType> types = getVehiclesType();
        List<PPField> ppFields = new ArrayList<>();
        PPField sum = new PPField(battleField.getPFieldWidth(), battleField.getPFieldHeight());

        if (types.contains(VehicleType.FIGHTER) || types.contains(VehicleType.HELICOPTER)) {
            ppFields.add(battleField.getAerialPPField());
            ppFields.add(staticAerialPPField);
            sum.minusField(aerialPPField);
        }

        if (types.contains(VehicleType.TANK) || types.contains(VehicleType.IFV) || types.contains(VehicleType.ARRV)) {
            ppFields.add(battleField.getTerrainPPField());
            ppFields.add(staticTerrainPPField);
            sum.minusField(terrainPPField);
        }

        sum.sumFields(ppFields);

        float factor = Float.MAX_VALUE;
        double newDestX = fromX;
        double newDestY = fromY;

        float x = (float)(destX - fromX);
        float y = (float)(destY - fromY);
        boolean isChangedXY = false;
        int minAngleSector = 0;

        double length = Math.sqrt(x * x + y * y);
        if (length > CustomParams.pathSegmentLenght) {
            x = x * (float)(CustomParams.pathSegmentLenght / length);
            y = y * (float)(CustomParams.pathSegmentLenght / length);
            isChangedXY = true;
        }

        double maxArmyX = this.getMaxX();
        double maxArmyY = this.getMaxY();

        double minArmyX = this.getMinX();
        double minArmyY = this.getMinY();

        for (int angleSector = 0; angleSector < CustomParams.pathFinderSectorCount; angleSector++) {

            double angle = (angleSector % 2 == 1 ? -1 : 1) * angleSector * (2 * Math.PI) / CustomParams.pathFinderSectorCount;
            double newVectorX = Math.cos(angle) * x - Math.sin(angle) * y;
            double newVectorY = Math.sin(angle) * x + Math.cos(angle) * y;
            double newx = newVectorX + fromX;
            double newy = newVectorY + fromY;

            double leftStartX;
            double leftStartY;

            double leftEndX;
            double leftEndY;

            double rightStartX;
            double rightStartY;

            double rightEndX;
            double rightEndY;
            double avgX = (maxArmyX + minArmyX) / 2;
            double avgY = (maxArmyY + minArmyY) / 2;
            if ((newx >= avgX && newy > avgY) || (newx <= avgX && newy < avgY)) {
                leftStartX = minArmyX;
                leftStartY = maxArmyY;
                leftEndX = minArmyX + newVectorX;
                leftEndY = maxArmyY + newVectorY;

                rightStartX = maxArmyX;
                rightStartY = minArmyY;
                rightEndX = maxArmyX + newVectorX;
                rightEndY = minArmyY + newVectorY;
            } else {
                leftStartX = maxArmyX;
                leftStartY = maxArmyY;
                leftEndX = maxArmyX + newVectorX;
                leftEndY = maxArmyY + newVectorY;

                rightStartX = minArmyX;
                rightStartY = minArmyY;
                rightEndX = minArmyX + newVectorX;
                rightEndY = minArmyY + newVectorY;
            }

            LineSegment leftLineSegment = new LineSegment(
                    sum.getTransformedXCoordinat(leftStartX),
                    sum.getTransformedYCoordinat(leftStartY),
                    sum.getTransformedXCoordinat(leftEndX),
                    sum.getTransformedYCoordinat(leftEndY)
            );

            LineSegment rightLineSegment = new LineSegment(
                    sum.getTransformedXCoordinat(rightStartX),
                    sum.getTransformedYCoordinat(rightStartY),
                    sum.getTransformedXCoordinat(rightEndX),
                    sum.getTransformedYCoordinat(rightEndY)
            );

            float localFactor = sum.getPathAvgFactor(leftLineSegment, rightLineSegment);

            if (localFactor < factor) {
                factor = localFactor;

                newDestX = newx;
                newDestY = newy;
                minAngleSector = angleSector;
            }
        }


        if (newDestX < (getMaxX() - getMinX())/2.0) {
            newDestX = (getMaxX() - getMinX())/2.0;
        }

        if (newDestY < (getMaxY() - getMinY())/2.0) {
            newDestY = (getMaxY() - getMinY())/2.0;
        }

        if (newDestX > MyStrategy.world.getWidth() - (getMaxX() - getMinX())/2.0) {
            newDestX = MyStrategy.world.getWidth() - (getMaxX() - getMinX())/2.0;
        }

        if (newDestY > MyStrategy.world.getWidth() - (getMaxY() - getMinY())/2.0) {
            newDestY = MyStrategy.world.getWidth() - (getMaxY() - getMinY())/2.0;
        }

        if (minAngleSector > 0) {
            isChangedXY = true;
        }

        if (isChangedXY) {
            return new CommandMove(new Point2D(Math.ceil(newDestX), Math.ceil(newDestY)));
        } else {
            return command;
        }
    }


    public boolean isRun() {
        return runningCommand != null && runningCommand.isRun();
    }

    public void result(SmartVehicle vehicle) {
        if (runningCommand.isRun()) {
            runningCommand.result(this, vehicle);
        }
    }

    public boolean containVehicle(Long vehicleId) {
        return vehicles.containsKey(vehicleId);
    }

    public SmartVehicle getNearestVehicle(Point2D point) {
        Map.Entry<Long, SmartVehicle> item = vehicles.entrySet().stream().min(
                (entry1, entry2) ->  Double.compare(
                        Math.pow((entry1.getValue().getX() - point.getX()) , 2) + Math.pow((entry1.getValue().getY()) - point.getY() , 2),
                        Math.pow(entry2.getValue().getX() - point.getX(), 2) + Math.pow(entry2.getValue().getY() - point.getY() , 2)
                )
        ).get();

        return item.getValue();
    }

    /**
     * @TODO rewrite with local cache
     * @desc
     * @return
     */
    public HashSet<VehicleType> getVehiclesType () {
        HashSet<VehicleType> set = new HashSet<>();
        for (Map.Entry<Long, SmartVehicle> entry : vehicles.entrySet()) {
            set.add(entry.getValue().getType());
        }
        return set;
    }

    public void selected() {
        Commander.selectGroupId = getGroupId();
    }

    public boolean isSelected() {
        return Commander.selectGroupId == getGroupId();
    }

    public void selectCommand() {
        try {
            new CommandSelect(this).run(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean needHeal() {
        return false;
    }

    public boolean canAttack () {
        return true;
    }

    public void setAerialPPField (PPField field) {
        staticAerialPPField = field;
    }

    public void setTerrainPPField (PPField field) {
        staticTerrainPPField = field;
    }

    public void init (PPField terrainField, PPField aerialField) {
        setTerrainPPField(terrainField);
        setAerialPPField(aerialField);
        refreshPPFields();
    }

    public void refreshPPFields () {
        aerialPPField = new PPField((int)(Math.ceil(MyStrategy.game.getWorldWidth() / BattleField.cellSize)), (int)(Math.ceil(MyStrategy.game.getWorldHeight() / BattleField.cellSize)));
        terrainPPField = new PPField((int)(Math.ceil(MyStrategy.game.getWorldWidth() / BattleField.cellSize)), (int)(Math.ceil(MyStrategy.game.getWorldHeight() / BattleField.cellSize)));

        for (Map.Entry<Long, SmartVehicle> entry : getVehicles().entrySet()) {
            SmartVehicle vehicle = entry.getValue();

            aerialPPField.addLinearPPValue(aerialPPField.getTransformedXCoordinat(vehicle.getX()), aerialPPField.getTransformedXCoordinat(vehicle.getY()), vehicle.getAerialPPFactor());
            terrainPPField.addLinearPPValue(terrainPPField.getTransformedXCoordinat(vehicle.getX()), terrainPPField.getTransformedXCoordinat(vehicle.getY()), vehicle.getTerrainPPFactor());
        }
    }

    public double[] searchNearestEnemy() {
        try {
            HashSet<VehicleType> types = getVehiclesType();

            PPField sum = new PPField(battleField.getPFieldWidth(), battleField.getPFieldHeight());

            for (VehicleType type : types) {
                sum.sumField(battleField.getDamageField(type));
            }

            double[] target = sum.getMaxValueCell();
            return target;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @TODO bad style rewrite it
     * @return
     */
    public float getAvgX () {
        return (float)((maxX + minX) / 2.0);
    }

    public Point2D getAvgPoint() {
        return new Point2D(getAvgX(), getAvgY());
    }

    /**
     * @TODO bad style rewrite it
     * @return
     */
    public float getAvgY () {
        return (float)((maxY + minY) / 2.0);
    }

    protected double avgX = 0;
    protected double avgY = 0;

    public void recalculationMaxMin() {
        if (vehicles.size() > 0) {
            double sumX = 0;
            double sumY = 0;
            super.minX = Double.MAX_VALUE;
            super.minY = Double.MAX_VALUE;

            super.maxX = 0;
            super.maxY = 0;
            for (Map.Entry<Long, SmartVehicle> entry : vehicles.entrySet()) {
                if (entry.getValue().getDurability() > 0) {
                    super.maxX = Math.max(super.maxX, entry.getValue().getX());
                    super.maxY = Math.max(super.maxY, entry.getValue().getY());

                    super.minX = Math.min(super.minX, entry.getValue().getX());
                    super.minY = Math.min(super.minY, entry.getValue().getY());

                    sumX += entry.getValue().getX();
                    sumY += entry.getValue().getY();
                }
            }

            avgX = sumX / vehicles.size();
            avgY = sumY / vehicles.size();
        }
    }

    public boolean isOnCoordinates(double x, double y) {
        return x >= (int)Math.floor(getMinX()) && x <= (int)Math.ceil(getMaxX()) && y >= (int)Math.floor(getMinY()) && y <= (int)Math.ceil(getMaxY());
    }

    public boolean isOnCoordinates(Point2D point) {
        return point.getX() >= (int)Math.floor(getMinX()) && point.getX() <= (int)Math.ceil(getMaxX()) && point.getY() >= (int)Math.floor(getMinY()) && point.getY() <= (int)Math.ceil(getMaxY());
    }

    public Point2D getNuclearAttackTarget() {
        return battleField.nuclearAttackTarget();
    }

    public Point2D getNearestSafetyPointForVehicle(SmartVehicle vehicle, Point2D target) throws Exception {

        PPField damageField = battleField.getDamageField(vehicle.getType());

        LineSegment lineSegment = new LineSegment(
                damageField.getTransformedXCoordinat(vehicle.getX()),
                damageField.getTransformedYCoordinat(vehicle.getY()),
                damageField.getTransformedXCoordinat(target.getX()),
                damageField.getTransformedYCoordinat(target.getY())
        );

        Point2D point = damageField.getNearestSafetyPoint(vehicle.getX(), vehicle.getY(), lineSegment);

        if (point == null) {
            point = new Point2D(damageField.getTransformedXCoordinat(target.getX()), damageField.getTransformedYCoordinat(target.getY()));
        }

        return battleField.getNearestEnemyToVehicleInCell(vehicle, point);
    }

    public double getVehicleVisionRange(SmartVehicle vehicle) {
        return battleField.getVisionRange(vehicle);
    }

    public Command getRunningCommand () {
        return runningCommand;
    }

    public boolean isHaveNuclearAttackCommand() {
        if (commandQueue.size() == 0) {
            return false;
        }

        if (runningCommand instanceof  CommandNuclearAttack) {
            return true;
        }

        for (Command command : commandQueue) {
            if (command instanceof CommandNuclearAttack) {
                return true;
            }
        }
        return false;
    }

    public Point2D[] getNearestEnemyPointAndSafetyPoint(int safetyDistance) {
        return battleField.getNearestEnemyPointAndSafetyPoint(new Point2D(getAvgX(), getAvgY()), safetyDistance);
    }

    public void printEnemyField() {
        battleField.print();
    }

    public float percentOfDeathVehicles() {
        return (float)vehicles.entrySet().stream().filter(entry -> entry.getValue().getDurability() == 0).count() / vehicles.size();
    }

    public Long getVehicleCount() {
        return vehicles.entrySet().stream().filter(entry -> entry.getValue().getDurability() > 0).count();
    }

    public boolean isNeedToScale() {

        double maxValue = 0;
        for (SmartVehicle vehicle : vehicles.values()) {
            if (vehicle.getDurability() > 0) {
                double max = vehicles.values().stream().filter(_vehicle -> _vehicle != vehicle && _vehicle.getDurability() > 0).map(_vehicle -> _vehicle.getPoint().subtract(vehicle.getPoint()).magnitude()).max(Double::compare).get();
                if (maxValue < max) {
                    maxValue = max;
                }
            }
        }

        if (maxValue >= CustomParams.maxVehicleDistInArmy) {
            return true;
        }

        return false;
    }

    public Point2D getArmySize () {
        return new Point2D((getMaxX() - getMinX()) ,(getMaxY() - getMinY()));
    }

    public boolean isArmyAlive () {
        return vehicles.size() == 0 || getVehicleCount() > 0;
    }
}
