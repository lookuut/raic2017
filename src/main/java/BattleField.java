import model.VehicleType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

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
            for (ArmyAllyOrdering army : vehicle.getArmySet()) {
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

        if (vehicle.getDurability() > 0 && ((vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell))
                || vehicle.isDurabilityChanched()
                || vehicle.isAttackCooldownChanched())) {
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

    public Army getTargetArmy(ArmyAlly allyArmy) {
        Set<VehicleType> allyTypes = allyArmy.getVehiclesType();
        MyStrategy.battleField.defineArmies();
        List<Army> enemyArmies = getEnemyArmies();
        
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(enemyArmies);

        Army targetArmy = null;
        for (Army enemyArmy : enemyArmies) {
            for (VehicleType enemyArmyType : enemyArmy.getVehiclesType()) {
                for (VehicleType allyType : allyTypes) {
                    if (SmartVehicle.isVictimType(allyType, enemyArmyType)) {
                        return enemyArmy;
                    }

                    if (SmartVehicle.isTargetVehicleType(allyType, enemyArmyType)) {
                        targetArmy = enemyArmy;
                    }
                }
            }
        }

        return targetArmy;
    }

    public void defineArmies() {

        if (armiesDefineTick == MyStrategy.world.getTickIndex()) {
            return;
        }

        Set<Point2D> visitedCells = new HashSet();
        
        Army allyArmy = new Army();
        Army enemyArmy = new Army();
        
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

    /*
    public List<Army> defineEnemiesArmy() {

        if (enemyArmiesCalcTick == MyStrategy.world.getTickIndex()) {
            return enemyArmies;
        }

        Set<Point2D> visitedCells = new HashSet();
        enemyArmies = new ArrayList();
        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                Point2D point = new Point2D(i,j);
                if (battleField[j][i].getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                    if (!visitedCells.contains(point)) {
                        Army enemyArmy = new Army();
                        recursiveDeepSearchEnemies(point, visitedCells, enemyArmy);
                        enemyArmies.add(enemyArmy);
                        if (enemyArmies.size() > CustomParams.enemyArmiesMaxSize) {
                            break;
                        }
                    }
                }
            }
        }
        return enemyArmies;
    }*/

    public void recursiveDeepSearchEnemies(Point2D point, Set<Point2D> visitedCells, Army enemyArmy, Army allyArmy) {
        for (int j = -1; j <= 1  && (point.getIntY() + j) < getHeight(); j++) {
            for (int i = -1; i <= 1 && (point.getIntX() + i) < getWidth(); i++) {
                if (point.getIntY() + j < 0 || point.getIntX() + i < 0) {
                    continue;
                }
                Point2D visitedPoint = new Point2D(point.getIntX() + i, point.getIntY() + j);
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
    }

    public BattleFieldCell searchEnemiesInRaious(int radious, Point2D point) {
        for (int i = -radious; i <= radious; i++) {
            for (int j = -radious; j <= radious; j++) {
                if (i * i + j * j <= radious * radious &&
                        point.getIntX() + i >= 0 &&
                        point.getIntX() + i < getWidth() &&
                        point.getIntY() + j >= 0 &&
                        point.getIntY() + j < getHeight()) {

                    if (getBattleFieldCell(point.getIntX() + i, point.getIntY() + j).getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                        return getBattleFieldCell(point.getIntX() + i, point.getIntY() + j);
                    }
                }
            }
        }
        return null;
    }
}
