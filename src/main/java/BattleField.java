
import javafx.geometry.Point2D;
import model.TerrainType;
import model.VehicleType;
import model.WeatherType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BattleField {

    protected BattleFieldCell[][] battleField;
    protected List<Army> armyList;
    protected Integer last_calc_army_tick_index = -1;
    public static int calc_army_interval = 1;

    protected int pFieldWidth;
    protected int pFieldHeight;

    protected PPField aerialPPField;
    protected PPField terrainPPField;

    protected PPField tankDamageField;
    protected PPField fighterDamageField;
    protected PPField helicopterDamageField;
    protected PPField ifvDamageField;

    protected PPField enemyField;

    public static Integer cellSize = 16;

    public BattleField () {

        pFieldWidth = (int)(Math.ceil(MyStrategy.game.getWorldWidth() / BattleField.cellSize));
        pFieldHeight = (int)(Math.ceil(MyStrategy.game.getWorldHeight() / BattleField.cellSize));

        battleField = new BattleFieldCell[pFieldHeight][pFieldWidth];

        aerialPPField = new PPField(pFieldWidth, pFieldHeight);
        terrainPPField = new PPField(pFieldWidth, pFieldHeight);

        tankDamageField = new PPField(pFieldWidth, pFieldHeight);
        fighterDamageField = new PPField(pFieldWidth, pFieldHeight);
        helicopterDamageField = new PPField(pFieldWidth, pFieldHeight);
        ifvDamageField = new PPField(pFieldWidth, pFieldHeight);

        enemyField = new PPField(pFieldWidth, pFieldHeight);
        for (int y = 0; y < pFieldHeight; y++) {
            for (int x = 0; x < pFieldWidth; x++) {
                this.battleField[y][x] = new BattleFieldCell(x,y);
            }
        }
    }

    public void addVehicle(SmartVehicle vehicle) {
        if (vehicle.isAlly() && (vehicle.getDurability() == 0 || vehicle.isVehicleMoved())) {
            for (AllyArmy army : vehicle.getArmySet()) {
                Command command = army.getRunningCommand();
                if (command != null) {
                    army.getRunningCommand().processing(vehicle);
                }
            }
        }

        if (vehicle.getDurability() > 0 && vehicle.isVehicleMoved()) {
            Integer battleFieldX = (int)Math.floor(vehicle.getX() / cellSize);
            Integer battleFieldY = (int)Math.floor(vehicle.getY() / cellSize);

            BattleFieldCell battleFieldCell =  this.battleField[battleFieldY][battleFieldX];
            BattleFieldCell vehicleBattleFieldCell = vehicle.getBattleFieldCell();


            if (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell) {
                vehicleBattleFieldCell.remove(vehicle);
                aerialPPField.addLinearPPValue(vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY(),-vehicle.getAerialPPFactor());
                terrainPPField.addLinearPPValue(vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY(),-vehicle.getTerrainPPFactor());

                if (!vehicle.isAlly()) {
                    tankDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.TANK, false), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                    fighterDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.FIGHTER, true), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                    ifvDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.IFV, false), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                    helicopterDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.HELICOPTER, true), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                    enemyField.addFactor(-1, vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                }

            }

            if (vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell)) {
                battleFieldCell.addVehicle(vehicle);

                aerialPPField.addLinearPPValue(battleFieldX, battleFieldY, vehicle.getAerialPPFactor());
                terrainPPField.addLinearPPValue(battleFieldX, battleFieldY, vehicle.getTerrainPPFactor());

                if (!vehicle.isAlly()) {
                    tankDamageField.addFactor(vehicle.getDamagePPFactor(VehicleType.TANK, false), battleFieldX, battleFieldY);
                    fighterDamageField.addFactor(vehicle.getDamagePPFactor(VehicleType.FIGHTER, true), battleFieldX, battleFieldY);
                    ifvDamageField.addFactor(vehicle.getDamagePPFactor(VehicleType.IFV, false), battleFieldX, battleFieldY);
                    helicopterDamageField.addFactor(vehicle.getDamagePPFactor(VehicleType.HELICOPTER, true), battleFieldX, battleFieldY);
                    enemyField.addFactor(1, battleFieldX, battleFieldY);
                }
            }

            vehicle.setBattleFieldCell(battleFieldCell);
        } else if (vehicle.getDurability() == 0) {
            BattleFieldCell vehicleBattleFieldCell = vehicle.getBattleFieldCell();
            aerialPPField.addLinearPPValue(vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY(),-vehicle.getAerialPPFactor());
            terrainPPField.addLinearPPValue(vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY(),-vehicle.getTerrainPPFactor());

            if (!vehicle.isAlly()) {
                tankDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.TANK, false), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                fighterDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.FIGHTER, true), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                ifvDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.IFV, false), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                helicopterDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.HELICOPTER, true), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());

                enemyField.addFactor(-1, vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
            }
        }
     }

    public void addPPValue(int x, int y, int value) {

    }

    public List<Army> formArmies () {
        if (last_calc_army_tick_index + calc_army_interval <=  MyStrategy.world.getTickIndex()) {
            armyList = new ArrayList();
            Collection<Integer> visitedPoints = new ArrayList();

            for (Integer playerId = 0; playerId < MyStrategy.max_player_index; playerId++) {
                for (int y = 0; y < pFieldHeight; y++) {
                    for (int x = 0; x < pFieldWidth; x++ ) {
                        Integer point = y * pFieldWidth + x;
                        if (battleField[y][x].isHaveVehicles(playerId) && !visitedPoints.contains(point)) {
                            Army army = (playerId == MyStrategy.player.getId() - 1) ? new AllyArmy() : new EnemyArmy();
                            this.recursiveCircumVention(x, y, army, visitedPoints, playerId);
                            armyList.add(army);
                        }
                    }
                }
            }

            last_calc_army_tick_index = MyStrategy.world.getTickIndex();
        }

        return armyList;
    }


    protected void recursiveCircumVention (int sourceX, int sourceY, Army army, Collection<Integer> visitedPoints, Integer playerId) {

        visitedPoints.add(sourceY * pFieldWidth + sourceX);

        int[] xCoordinates = {0, 1, 0, -1};
        int[] yCoordinates = {-1, 0, 1, 0};


        for (int index = 0; index < xCoordinates.length; index++) {
            int xCoor = sourceX + xCoordinates[index];
            int yCoor = sourceY + yCoordinates[index];

            if (xCoor >= 0 && yCoor >= 0 && xCoor < pFieldWidth && yCoor < pFieldHeight &&
                    !visitedPoints.contains((yCoor) * pFieldWidth + xCoor) &&
                    this.battleField[yCoor][xCoor].isHaveVehicles(playerId))
            {
                recursiveCircumVention(xCoor, yCoor, army, visitedPoints, playerId);
            }
        }

        army.addBattleFieldCell(sourceY * pFieldWidth + sourceX, this.battleField[sourceY][sourceX], playerId);
    }

    public int getPFieldWidth() {
        return pFieldWidth;
    }

    public int getPFieldHeight() {
        return pFieldHeight;
    }

    public PPField getAerialPPField() {
        return aerialPPField;
    }

    public PPField getTerrainPPField() {
        return terrainPPField;
    }

    /**
     * @desc bad style, rewrite it
     * @param type
     * @return
     */
    public PPField getDamageField(VehicleType type) throws Exception{
        switch (type) {
            case HELICOPTER:
                return helicopterDamageField;
            case TANK:
                return tankDamageField;
            case FIGHTER:
                return fighterDamageField;
            case IFV:
                return ifvDamageField;
        }

        throw new Exception("Unknown vehicle type " + type.toString());
    }


    public Point2D[] getNearestEnemyPointAndSafetyPoint(Point2D point, float safetyDistance) {
        //@TODO bad style
        int intSafetyDistance = (int)Math.ceil(safetyDistance * enemyField.getWidth() / MyStrategy.world.getWidth());

        float minEnemyDist = Float.MAX_VALUE;
        Point2D nearestEnemyPoint = new Point2D(0,0);

        float minSafetyDist = Float.MAX_VALUE;
        Point2D nearestSafetyPoint = new Point2D(0,0);

        for (int j = 0; j < getPFieldHeight(); j++) {
            for (int i = 0; i < getPFieldWidth(); i++) {

                if (enemyField.getFactor(i, j) > 0) {
                    Point2D vector = new Point2D(point.getX() - enemyField.getWorldX(i), point.getY() - enemyField.getWorldY(j));

                    if (vector.magnitude() < minEnemyDist) {
                        minEnemyDist = (float)vector.magnitude();
                        nearestEnemyPoint = new Point2D(enemyField.getWorldY(i), enemyField.getWorldY(j));
                    }
                }

                if (enemyField.getFactor(i, j) == 0) {
                    int startII = Math.max(0, i - intSafetyDistance);
                    int startJJ = Math.max(0, j - intSafetyDistance);

                    int endII = Math.min(enemyField.getWidth(), i + intSafetyDistance);
                    int endJJ = Math.min(enemyField.getHeight(), j + intSafetyDistance);

                    boolean goodShape = true;
                    for (int jj = startJJ; jj < endJJ && goodShape; jj++) {
                        for (int ii = startII; ii <  endII; ii++) {
                            if (enemyField.getFactor(jj, ii) > 0) {
                                goodShape = false;
                                break;
                            }
                        }
                    }

                    if (goodShape) {
                        Point2D vector = new Point2D(point.getX() - enemyField.getWorldX(i), point.getY() - enemyField.getWorldY(j));

                        if (vector.magnitude() < minSafetyDist) {
                            minSafetyDist = (float)vector.magnitude();
                            nearestSafetyPoint = new Point2D(enemyField.getWorldY(i), enemyField.getWorldY(j));
                        }
                    }

                }
            }
        }

        Point2D[] result = {nearestEnemyPoint, nearestSafetyPoint};
        return result;
    }


    public Point2D nuclearAttackTarget() {
        int maxX = 0;
        int maxY = 0;
        double maxValue = 0;
        for (int j = 0; j < getPFieldHeight(); j++) {
            for (int i = 0; i < getPFieldWidth(); i++) {
                double localMaxValue = 0;
                int localMaxX = 0;
                int localMaxY = 0;
                double localMax = 0;

                for (int jj = -2; jj <= 2 && jj + j >= 0 && jj + j < getPFieldHeight(); jj++){
                    for (int ii = -2; ii <= 2 && ii + i >= 0 && ii + i < getPFieldHeight(); ii++) {
                        if (enemyField.getFactor(ii + i, jj + j) > localMax) {
                            localMax = enemyField.getFactor(ii + i, jj + j);
                            localMaxX = ii + i;
                            localMaxY = jj + j;
                        }
                        localMaxValue += enemyField.getFactor(ii + i, jj + j);
                    }
                }

                if (maxValue < localMaxValue) {
                    maxValue = localMaxValue;
                    maxX = localMaxX;
                    maxY = localMaxY;
                }
            }
        }

        return new Point2D(enemyField.getWorldX(maxX), enemyField.getWorldY(maxY));
    }

    /**
     * @desc get nearest safety point for vehicle in cell point
     * @param allyVehicle
     * @param point
     * @return
     * @throws Exception
     */
    public Point2D getNearestEnemyToVehicleInCell (SmartVehicle allyVehicle, Point2D point) throws Exception {
        SmartVehicle enemyVehicle = battleField[(int)point.getX()][(int)point.getY()].getNearestVehicle((int)point.getX(), (int)point.getY());

        double attackRange = 0;
        Point2D vector = point;

        //@TODO workaround, use terrain and weather factor
        if (enemyVehicle != null) {
            attackRange = enemyVehicle.getAttackRange(allyVehicle);
            vector = enemyVehicle.getPoint().subtract(allyVehicle.getPoint());
        }

        if (enemyVehicle == null) {
            System.out.println("Cant find enemy in " + point);
        }

        double distance = vector.magnitude();
        double safetyDistance = (distance - attackRange) / distance;

        return vector.multiply(safetyDistance).add(allyVehicle.getPoint());
    }

    public double getVisionRange(SmartVehicle vehicle) {

        if (vehicle.isAerial()) {
            WeatherType[][] weather = MyStrategy.world.getWeatherByCellXY();
            int x = (int)Math.floor(vehicle.getX() * (MyStrategy.game.getTerrainWeatherMapColumnCount() / MyStrategy.world.getWidth()));
            int y = (int)Math.floor(vehicle.getY() * (MyStrategy.game.getTerrainWeatherMapRowCount() / MyStrategy.world.getHeight()));
            if (weather[y][x] == WeatherType.CLOUD ) {
                return MyStrategy.game.getCloudWeatherVisionFactor() * vehicle.getVisionRange();
            } else if (weather[y][x] == WeatherType.RAIN) {
                return MyStrategy.game.getRainWeatherVisionFactor() * vehicle.getVisionRange();
            }
            return vehicle.getVisionRange();
        } else {

            TerrainType[][] terrain = MyStrategy.world.getTerrainByCellXY();
            int x = (int)Math.floor(vehicle.getX() * (MyStrategy.game.getTerrainWeatherMapColumnCount() / MyStrategy.world.getWidth()));
            int y = (int)Math.floor(vehicle.getY() * (MyStrategy.game.getTerrainWeatherMapRowCount() / MyStrategy.world.getHeight()));
            if (terrain[y][x] == TerrainType.FOREST ) {
                return MyStrategy.game.getForestTerrainVisionFactor() * vehicle.getVisionRange();
            } else if (terrain[y][x] == TerrainType.SWAMP) {
                return MyStrategy.game.getSwampTerrainVisionFactor() * vehicle.getVisionRange();
            }
            return vehicle.getVisionRange();
        }
    }

    public void print() {
        helicopterDamageField.print();
    }
}
