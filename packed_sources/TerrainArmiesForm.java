
import model.ActionType;
import model.VehicleType;

import java.util.*;
import java.util.function.Consumer;

class FormTask {
    public Point2D moveVector;
    public Map<Long, Point2D> finalDestination;
    public List<SmartVehicle> selectedVehicles;
    public Integer state;
    public TerrainArmiesForm form;
    public SmartVehicle markVehicle;
    public FormTask (TerrainArmiesForm form) {
        finalDestination = new HashMap<>();
        state = -1;
        this.form = form;
    }

    public boolean check () {

        if (selectedVehicles.size() == 0) {
            return false;
        }

        state = 1;

        for (SmartVehicle vehicle : selectedVehicles) {
            if (vehicle.getDurability() > 0) {
                double xDelta = Math.abs(vehicle.getPoint().getX() - finalDestination.get(vehicle.getId()).getX());
                double yDelta = Math.abs(vehicle.getPoint().getY() - finalDestination.get(vehicle.getId()).getY());

                if (xDelta > 0.00001 || yDelta > 0.00001) {
                    return false;
                }
            }
        }

        return true;
    }
}

public class TerrainArmiesForm {

    private Map<VehicleType, SmartVehicle> vehicleTypeAngle;
    private Map<VehicleType, Integer> vehicleTypeBias;

    private Point2D zeroPoint;
    private Point2D armiesFormStartPoint;
    private Set<VehicleType> expansionedTypes;

    private Map<SmartVehicle, FormTask> tasks = new HashMap<>();
    private Map<VehicleType, Integer> armyCounter = new HashMap<>();
    private Set<VehicleType> expansionCompleteVehicleType = new HashSet<>();
    private ArmyDivisions divisions;
    private List<Integer> busyVerticals = new ArrayList<>();

    public TerrainArmiesForm(ArmyDivisions divisions) {
        this.divisions = divisions;

        vehicleTypeAngle = new HashMap<>();
        zeroPoint = new Point2D(0,0);
        vehicleTypeBias = new HashMap<>();
        vehicleTypeBias.put(VehicleType.TANK, 28);
        vehicleTypeBias.put(VehicleType.ARRV, 13);
        vehicleTypeBias.put(VehicleType.IFV, 0);

        currentExpansionVehicle = new HashMap<>();
        currentExpansionVehicle.put(VehicleType.TANK, null);
        currentExpansionVehicle.put(VehicleType.ARRV, null);
        currentExpansionVehicle.put(VehicleType.IFV, null);

        expansionedTypes = new HashSet<>();
        tasks = new HashMap<>();
        armyCounter.put(VehicleType.TANK, 0);
        armyCounter.put(VehicleType.ARRV, 0);
        armyCounter.put(VehicleType.IFV, 0);
    }

    public static int armyParts = 5;
    public static double armyHeight = 60f;
    public static double armyWidth = 60f;

    private Map<VehicleType, SmartVehicle> currentExpansionVehicle;

    public void expansion(VehicleType type) {

        if (armyCounter.get(type) + 1 >= armyParts && !tasks.containsKey(currentExpansionVehicle.get(type))) {//task already complete
            expansionCompleteVehicleType.add(type);
        }

        if (armyCounter.get(type) + 1 >= armyParts) {
            return;
        }


        SmartVehicle vehicle = vehicleTypeAngle.get(type);

        for (SmartVehicle vehicle_ : vehicleTypeAngle.values()) {
            if (vehicle_ != vehicle && Math.abs(vehicle.getX() - vehicle_.getX()) < 10 && vehicle.getY() < vehicle_.getY()) {
                return;
            }
        }
        if (vehicle.getPoint().getY() < (armiesFormStartPoint.getY() + vehicleTypeBias.get(type)) &&
                Math.abs(armiesFormStartPoint.getY() + vehicleTypeBias.get(type) - vehicle.getPoint().getY()) > 0.00001) {
            if (!tasks.containsKey(vehicle)) {
                Square square = new Square(vehicle.getPoint().subtract(new Point2D(2, 2)), vehicle.getPoint().add(new Point2D(armyWidth + 2, armyHeight + 2)));
                Point2D moveVector = new Point2D(0, armiesFormStartPoint.getY() + vehicleTypeBias.get(type) - vehicle.getPoint().getY());
                move(square, type, vehicle, moveVector);
            }

            return;
        }

        if (currentExpansionVehicle.containsKey(type) &&
                tasks.containsKey(currentExpansionVehicle.get(type)) || tasks.containsKey(vehicle)) {
            return;
        }

        SmartVehicle currentExpansionVehicle = searchNearestVehicle(
                new Point2D(this.currentExpansionVehicle.get(type).getPoint().getX(), this.currentExpansionVehicle.get(type).getPoint().getY() + 12),
                type
        );

        Point2D armyStartPos = vehicleTypeAngle.get(type).getPoint();

        Point2D selectPoint = new Point2D(armyStartPos.getX() - 2, currentExpansionVehicle.getPoint().getY() - 2);
        Square square = new Square(selectPoint, selectPoint.add(new Point2D(armyWidth + 2, 2 * armyHeight + 2)));
        Point2D moveVector = new Point2D(0, 30);
        move(square, type, currentExpansionVehicle, moveVector);
        armyCounter.put(type, armyCounter.get(type) + 1);
        this.currentExpansionVehicle.put(type, currentExpansionVehicle);
    }


    public void setStartPoint() {
        if (armiesFormStartPoint == null) {
            armiesFormStartPoint = new Point2D(0,0);

            for (Map.Entry<VehicleType, SmartVehicle> entry : vehicleTypeAngle.entrySet()) {
                if (entry.getValue().getPoint().getY() > armiesFormStartPoint.getY()) {
                    armiesFormStartPoint = entry.getValue().getPoint().clone();
                }
            }
        }
    }
    public void searchToMove() {

        for (Map.Entry<VehicleType, SmartVehicle> entry : vehicleTypeAngle.entrySet()) {
            if (tasks.containsKey(entry.getValue())) {
                continue;
            }

            if (expansionCompleteVehicleType.contains(entry.getKey())) {
                continue;
            }

            if (Math.floor((entry.getValue().getPoint().getY() - 20) / armyHeight) == Math.floor((armiesFormStartPoint.getY() - 20) / armyHeight)) {
                continue;
            }
            short haveOnTop = 0;

            boolean isHaveOnLeft = false;
            for (SmartVehicle vehicle : vehicleTypeAngle.values()) {
                if (vehicle != entry.getValue()) {
                    if ((
                            Math.ceil((vehicle.getPoint().getX() - 20) / armyWidth) >= Math.ceil((entry.getValue().getPoint().getX() - 20) / armyWidth)
                    ) && Math.ceil(vehicle.getPoint().getY() / armyHeight) >= Math.ceil(entry.getValue().getPoint().getY() / armyHeight)) {
                        haveOnTop++;
                    }

                    if (vehicle.getPoint().getY() < armiesFormStartPoint.getY() - 2
                            &&
                            Math.ceil(vehicle.getPoint().getX() / armyWidth) > Math.ceil(entry.getValue().getPoint().getX() / armyWidth)
                            ) {

                        isHaveOnLeft = true;
                    }
                }
            }

            Square square = new Square(entry.getValue().getPoint().subtract(new Point2D(2, 0)), entry.getValue().getPoint().add(new Point2D(armyWidth + 2, armyHeight + 2)));

            if (haveOnTop == 0 && Math.floor(armiesFormStartPoint.getY() / armyHeight) != Math.floor(entry.getValue().getPoint().getY() / armyHeight)) {
                Point2D moveVector = new Point2D(0, armiesFormStartPoint.getY() - entry.getValue().getPoint().getY());
                move(square, entry.getKey(), entry.getValue(), moveVector);
            } else if (haveOnTop > 0 && isHaveOnLeft == false){
                Point2D moveVector = new Point2D(haveOnTop * (armyWidth + 10), 0);
                move(square, entry.getKey(), entry.getValue(), moveVector);
            }
        }
    }

    public void searchExpansionArmy() {
        if (complete) {
            return;
        }

        setStartPoint();
        isCompleteToMoved();

        int maxX = -1;
        int maxY = -1;
        VehicleType maxXYVehileType = null;
        SmartVehicle vehile = null;
        for (Map.Entry<VehicleType, SmartVehicle> entry : vehicleTypeAngle.entrySet()) {
            if (!expansionedTypes.contains(entry.getKey()) && !tasks.containsKey(vehile)) {
                int x = (int)Math.floor((entry.getValue().getPoint().getX()) / armyWidth);
                int y = (int)Math.floor((entry.getValue().getPoint().getY()) / armyHeight);

                if (y > maxY && x >= maxX) {
                    maxY = y;
                    maxXYVehileType = entry.getKey();
                    vehile = entry.getValue();
                }
            }
        }

        for (Map.Entry<VehicleType, SmartVehicle> entry : vehicleTypeAngle.entrySet()) {
            if ((int)Math.ceil(entry.getValue().getPoint().getY()/ armyHeight) == 2 && entry.getValue().getX() != armiesFormStartPoint.getX()) {
                currentExpansionVehicle.put(entry.getValue().getType(), vehicleTypeAngle.get(entry.getValue().getType()));
                expansionedTypes.add(entry.getValue().getType());
            }
        }

        if (maxXYVehileType != null) {
            currentExpansionVehicle.put(maxXYVehileType, vehicleTypeAngle.get(maxXYVehileType));
            expansionedTypes.add(maxXYVehileType);
            expansion(maxXYVehileType);
        }

        for (VehicleType type : expansionedTypes) {

            expansion(type);
        }

        searchToMove();
        merge();
        formArmies();
    }
    public boolean complete = false;

    public void formArmies () {
        if (expansionCompleteVehicleType.size() < 3) {
            return;
        }
        boolean isReadyToCreateArmies = true;
        for (VehicleType type : expansionCompleteVehicleType) {
            if (tasks.containsKey(vehicleTypeAngle.get(type))) {
                isReadyToCreateArmies = false;
            }
        }

        if (isReadyToCreateArmies) {
            Point2D pointStart = vehicleTypeAngle.get(VehicleType.IFV).getPoint();
            double armyHeight = 41;

            for (int i = 0; i < 5; i++) {
                try {
                    Square square = new Square(
                            new Point2D(pointStart.getX() - 2, pointStart.getY() + armyHeight * i - 2),
                            new Point2D(pointStart.getX() + armyWidth + 2, pointStart.getY() + armyHeight * (i + 1) + 2));
                    divisions.addArmy(square, expansionedTypes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            complete = true;
        }
    }
    private boolean mergeComplete = false;
    public void merge () {
        if (expansionCompleteVehicleType.size() < 3 || mergeComplete) {
            return;
        }
        VehicleType middleVehicleType = null;
        for (VehicleType type : expansionCompleteVehicleType) {
            SmartVehicle completeVehicleType = vehicleTypeAngle.get(type);

            if (tasks.containsKey(completeVehicleType)) {
                continue;
            }

            boolean haveLeft = false;
            boolean haveRight = false;
            for (SmartVehicle vehicle : vehicleTypeAngle.values()) {
                if (completeVehicleType != vehicle) {

                    if (vehicle.getX() > completeVehicleType.getX()) {
                        haveRight = true;
                    }

                    if (vehicle.getX() < completeVehicleType.getX()) {
                        haveLeft = true;
                    }
                }
            }
            if ((haveLeft && haveRight)) {//there are middle
                middleVehicleType = type;
            }
        }

        if (middleVehicleType != null) {
            SmartVehicle middleVehicle = vehicleTypeAngle.get(middleVehicleType);
            for (VehicleType type : expansionCompleteVehicleType) {
                if (middleVehicleType != type) {
                    SmartVehicle completeVehicleType = vehicleTypeAngle.get(type);
                    Square square = new Square(completeVehicleType.getPoint().add(new Point2D(-2, -2)), completeVehicleType.getPoint().add(new Point2D(armyWidth + 2, armyHeight * 3)));
                    Point2D moveVector = new Point2D(middleVehicle.getX() - completeVehicleType.getX(), 0);
                    move(square, type, completeVehicleType, moveVector);
                }
            }
            armiesFormStartPoint = middleVehicle.getPoint().clone();
            mergeComplete = true;
        }
    }

    public void isCompleteToMoved() {

        Iterator<Map.Entry<SmartVehicle, FormTask>> iter = tasks.entrySet().iterator();
        while (iter.hasNext()) {

            if (iter.next().getValue().check()) {
                iter.remove();
            }
        }
    }

    public void updateVehicle(SmartVehicle vehicle) {
        if (vehicle.isTerrain()) {
            FormTask currentTask = tasks.values().stream().filter(task -> task.state == 0).findFirst().orElse(null);

            if (vehicle.getSelected() && currentTask != null) {
                currentTask.selectedVehicles.add(vehicle);
                Point2D destPoint = vehicle.getPoint().add(currentTask.moveVector);
                currentTask.finalDestination.put(vehicle.getId(), new Point2D(Math.round(destPoint.getX()), Math.round(destPoint.getY())));
            }
        }
    }

    public void addNewVehicle(SmartVehicle vehicle) {
        if (vehicle.isTerrain()) {
            if (!vehicleTypeAngle.containsKey(vehicle.getType())) {
                vehicleTypeAngle.put(vehicle.getType(), vehicle);
            } else {
                double distanceToZero = vehicle.getPoint().distance(zeroPoint);
                if (distanceToZero < vehicleTypeAngle.get(vehicle.getType()).getPoint().distance(zeroPoint)) {
                    vehicleTypeAngle.put(vehicle.getType(), vehicle);
                }
            }
        }
    }

    private SmartVehicle searchNearestVehicle (Point2D point, VehicleType type) {
        double minDict = Double.MAX_VALUE;
        SmartVehicle minDistVehicle = null;
        for (SmartVehicle vehicle : MyStrategy.getVehicles().values()) {
            if (vehicle.isAlly() && vehicle.getType() == type && vehicle.getDurability() > 0 && vehicle.getPoint().distance(point) < minDict) {
                minDistVehicle = vehicle;
                minDict = vehicle.getPoint().distance(point);
            }
        }
        return minDistVehicle;
    }

    public void move(Square square, VehicleType vehicleType, SmartVehicle markVehicle, Point2D moveVector) {
        if (moveVector.magnitude() <= 0) {
            return;
        }

        FormTask task = new FormTask(this);
        task.selectedVehicles = new ArrayList<>();
        task.moveVector = moveVector.clone();
        task.markVehicle = markVehicle;

        tasks.put(markVehicle, task);

        CommandEmpty command = new CommandEmpty();
        Consumer<Command> selectSquare = (_command) -> {
            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            MyStrategy.move.setLeft(square.getLeftBottomAngle().getX());
            MyStrategy.move.setRight(square.getRightTopAngle().getX());
            MyStrategy.move.setTop(square.getLeftBottomAngle().getY());
            MyStrategy.move.setBottom(square.getRightTopAngle().getY());
            if (vehicleType != null) {
                MyStrategy.move.setVehicleType(vehicleType);
            }

            if (tasks.containsKey(markVehicle)) {
                tasks.get(markVehicle).state = 0;
            }
        };

        Consumer<Command> move = (_command) -> {
            MyStrategy.move.setAction(ActionType.MOVE);
            System.out.println("start point" + markVehicle.getPoint() +  "move vector => " + moveVector);
            MyStrategy.move.setX(moveVector.getX());
            MyStrategy.move.setY(moveVector.getY());
        };

        CommandWrapper cw = new CommandWrapper(command, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId, CommandPriority.High);

        cw.addCommand(selectSquare);
        cw.addCommand(move);
        CommandQueue.getInstance().addCommand(cw);
    }



}
