package strategy;

import model.VehicleType;

import java.util.*;

public class BattleField {

    protected BattleFieldCell[][] battleField;

    protected int pFieldWidth;
    protected int pFieldHeight;
    
    private Map<Long, List<Army>> armies;
    private int armiesDefineTick;

    //temporary code remove it before upload
    private Integer cellSize;

    public BattleField (Integer cellSize) {
        this.cellSize = cellSize;

        pFieldWidth = (int)(Math.ceil(MyStrategy.game.getWorldWidth() / cellSize));
        pFieldHeight = (int)(Math.ceil(MyStrategy.game.getWorldHeight() / cellSize));

        battleField = new BattleFieldCell[pFieldHeight][pFieldWidth];

        for (int y = 0; y < pFieldHeight; y++) {
            for (int x = 0; x < pFieldWidth; x++) {
                this.battleField[y][x] = new BattleFieldCell(x,y);
            }
        }
        armies = new HashMap<>();
        armies.put(MyStrategy.getEnemyPlayerId(), new ArrayList<>());
        armies.put(MyStrategy.player.getId(), new ArrayList<>());
    }

    public Point2D pointTransform(Point2D point) {
        return new Point2D((int)Math.floor(point.getX() / cellSize), (int)Math.floor(point.getY() / cellSize));
    }

    public void addVehicle(SmartVehicle vehicle) {

        if (vehicle.isAlly() && (vehicle.getDurability() == 0 || vehicle.isVehicleMoved())) {
            ArmyAllyOrdering army = vehicle.getArmy();
            if (army != null) {
                Command command = army.getRunningCommand();
                if (command != null) {
                    army.getRunningCommand().processing(vehicle);
                }
            }
        }

        Integer battleFieldX = (int)Math.floor(vehicle.getX() / cellSize);
        Integer battleFieldY = (int)Math.floor(vehicle.getY() / cellSize);

        BattleFieldCell battleFieldCell =  this.battleField[battleFieldY][battleFieldX];
        BattleFieldCell vehicleBattleFieldCell = vehicle.getBattleFieldCell();

        if ((
                vehicleBattleFieldCell != null &&
                        (vehicleBattleFieldCell != battleFieldCell
                        || vehicle.getDurability() == 0
                        || vehicle.isDurabilityChanched()
                        || vehicle.isAttackCooldownChanched()
                        ))
                ) {

            vehicleBattleFieldCell.remove(MyStrategy.getVehiclePrevState(vehicle.getId()));

            Integer lastVehicleX = vehicleBattleFieldCell.getX();
            Integer lastVehicleY = vehicleBattleFieldCell.getY();

            MyStrategy.enemyField.removeFromCellVehicle(lastVehicleX, lastVehicleY, MyStrategy.getVehiclePrevState(vehicle.getId()));

        }

        if (vehicle.getDurability() == 0) {
            vehicleBattleFieldCell.remove(vehicle);
        }

        if (vehicle.getDurability() > 0 &&
                (
                        (
                                vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell)
                        )
                    || vehicle.isDurabilityChanched()
                    || vehicle.isAttackCooldownChanched()
                )
                ) {
            battleFieldCell.addVehicle(vehicle);
            MyStrategy.enemyField.addVehicleToCell(battleFieldX, battleFieldY, vehicle);
        }

        vehicle.setBattleFieldCell(battleFieldCell);
     }


    public int getWidth() {
        return pFieldWidth;
    }

    public int getHeight() {
        return pFieldHeight;
    }

    public BattleFieldCell getBattleFieldCell(int x, int y) {
        return battleField[y][x];
    }

    public BattleFieldCell getBattleFieldCell(Point2D point) {
        return battleField[point.getIntY()][point.getIntX()];
    }

    public void defineArmies() {

        if (armiesDefineTick == MyStrategy.world.getTickIndex()) {
            return;
        }

        if (MyStrategy.world.getTickIndex() - armiesDefineTick < CustomParams.enemyArmiesDefineInterval) {
            return;
        }

        Set<Point2D> visitedCells = new HashSet();
        
        Army allyArmy = new Army();
        Army enemyArmy = new Army();

        armies.get(MyStrategy.getEnemyPlayerId()).clear();
        armies.get(MyStrategy.player.getId()).clear();

        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                Point2D point = new Point2D(i,j);

                if (!visitedCells.contains(point)) {

                    if (battleField[j][i].getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0 
                        || 
                        battleField[j][i].getVehicles(MyStrategy.player.getId()).size() > 0
                        ) {
                        
                        recursiveDeepSearchEnemies(point, visitedCells, enemyArmy, allyArmy);
                        
                        if (allyArmy.getVehicles().size() > 0) {
                            armies.get(MyStrategy.player.getId()).add(allyArmy);
                            allyArmy = new Army();
                        }

                        if (enemyArmy.getVehicles().size() > 0) {
                            armies.get(MyStrategy.getEnemyPlayerId()).add(enemyArmy);
                            enemyArmy = new Army();
                        }
                    }
                }
            }
        }

        armiesDefineTick = MyStrategy.world.getTickIndex();
    }

    public List<Army> getEnemyArmies() {
        return armies.get(MyStrategy.getEnemyPlayerId());
    }

    public List<Army> getAllyArmies() {
        return armies.get(MyStrategy.player.getId());
    }

    public void recursiveDeepSearchEnemies(Point2D center, Set<Point2D> visitedCells, Army enemyArmy, Army allyArmy) {

        Point2D[] points = new Point2D[5];
        points[0] = new Point2D(0,-1);
        points[1] = new Point2D(-1,0);
        points[2] = new Point2D(1,0);
        points[3] = new Point2D(0,1);
        points[4] = new Point2D(0,0);

        for (Point2D point : points) {
            Point2D visitedPoint = center.add(point);
            if (visitedPoint.getY() < 0 || visitedPoint.getX() < 0 || visitedPoint.getX() >= getWidth() || visitedPoint.getY() >= getHeight()) {
                continue;
            }

            BattleFieldCell cell = getBattleFieldCell(visitedPoint);

            if (!visitedCells.contains(visitedPoint)) {
                if (cell.getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                    cell.getVehicles(MyStrategy.getEnemyPlayerId()).values().forEach(vehicle -> enemyArmy.addVehicle(vehicle));
                    visitedCells.add(visitedPoint);
                    recursiveDeepSearchEnemies(visitedPoint, visitedCells, enemyArmy, allyArmy);
                }

                if (cell.getVehicles(MyStrategy.player.getId()).size() > 0) {
                    cell.getVehicles(MyStrategy.player.getId()).values().forEach(vehicle -> allyArmy.addVehicle(vehicle));
                    visitedCells.add(visitedPoint);
                    recursiveDeepSearchEnemies(visitedPoint, visitedCells, enemyArmy, allyArmy);
                }
            }
        }

    }

    public BattleFieldCell searchTargetEnemiesAround(int radious, Point2D point, Set<VehicleType> vehiclesTypes) {
        for (int i = -radious; i <= radious; i++) {
            for (int j = -radious; j <= radious; j++) {
                if (i * i + j * j <= radious * radious &&
                        point.getIntX() + i >= 0 &&
                        point.getIntX() + i < getWidth() &&
                        point.getIntY() + j >= 0 &&
                        point.getIntY() + j < getHeight()) {

                    for (VehicleType vehicleType : vehiclesTypes) {
                        if (getBattleFieldCell(point.getIntX() + i, point.getIntY() + j).getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                            VehicleType enemyVehicleType = getBattleFieldCell(point.getIntX() + i, point.getIntY() + j).getVehicles(MyStrategy.getEnemyPlayerId()).values().iterator().next().getType();
                            if (SmartVehicle.isTargetVehicleType(vehicleType, enemyVehicleType)) {
                                return getBattleFieldCell(point.getIntX() + i, point.getIntY() + j);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public BattleFieldCell searchEnemiesAround(int radious, Point2D point, Set<VehicleType> vehiclesTypes) {
        for (int i = -radious; i <= radious; i++) {
            for (int j = -radious; j <= radious; j++) {
                if (i * i + j * j <= radious * radious &&
                        point.getIntX() + i >= 0 &&
                        point.getIntX() + i < getWidth() &&
                        point.getIntY() + j >= 0 &&
                        point.getIntY() + j < getHeight()) {

                    for (VehicleType vehicleType : vehiclesTypes) {
                        if (getBattleFieldCell(point.getIntX() + i, point.getIntY() + j).getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                            return getBattleFieldCell(point.getIntX() + i, point.getIntY() + j);
                        }
                    }
                }
            }
        }
        return null;
    }
}
