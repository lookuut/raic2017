
import model.TerrainType;
import model.VehicleType;
import model.WeatherType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

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

        if (vehicle.getDurability() > 0 && vehicle.isVehicleMoved()) {
            Integer battleFieldX = (int)Math.floor(vehicle.getX() / cellSize);
            Integer battleFieldY = (int)Math.floor(vehicle.getY() / cellSize);

            BattleFieldCell battleFieldCell =  this.battleField[battleFieldY][battleFieldX];
            BattleFieldCell vehicleBattleFieldCell = vehicle.getBattleFieldCell();


            if (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell) {
                vehicleBattleFieldCell.remove(vehicle);
                aerialPPField.addFactor(-vehicle.getAerialPPFactor(), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                terrainPPField.addFactor(-vehicle.getTerrainPPFactor(), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());

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
                aerialPPField.addFactor(vehicle.getAerialPPFactor(), battleFieldX, battleFieldY);
                terrainPPField.addFactor(vehicle.getTerrainPPFactor(), battleFieldX, battleFieldY);
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
            aerialPPField.addFactor(-vehicle.getAerialPPFactor(), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
            terrainPPField.addFactor(-vehicle.getTerrainPPFactor(), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());

            if (!vehicle.isAlly()) {
                tankDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.TANK, false), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                fighterDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.FIGHTER, true), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                ifvDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.IFV, false), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
                helicopterDamageField.addFactor(-vehicle.getDamagePPFactor(VehicleType.HELICOPTER, true), vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());

                enemyField.addFactor(-1, vehicleBattleFieldCell.getX(), vehicleBattleFieldCell.getY());
            }
        }
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

    public double[] getNearestEnemyPoint(double x, double y, double interval) {
        //@TODO bad style


        for (int j = 0; j < getPFieldHeight(); j++) {
            for (int i = 0; i < getPFieldWidth(); i++) {
                if (enemyField.getFactor(i, j) > 0) {
                    double[] result = {enemyField.getWorldY(i), enemyField.getWorldY(j)};
                    return result;
                }
            }
        }
        return null;
    }

    public double[] nuclearAttackTarget() {
        int maxX = 0;
        int maxY = 0;
        double maxValue = 0;
        for (int j = 0; j < getPFieldHeight(); j++) {
            for (int i = 0; i < getPFieldWidth(); i++) {
                double localMaxValue = 0;
                for (int jj = -2; jj <= 2 && jj + j >= 0 && jj + j < getPFieldHeight(); jj++){
                    for (int ii = -2; ii <= 2 && ii + i >= 0 && ii + i < getPFieldHeight(); ii++) {
                        localMaxValue += enemyField.getFactor(ii + i, jj + j);
                    }
                }

                if (maxValue < localMaxValue) {
                    maxValue = localMaxValue;
                    maxX = i;
                    maxY = j;
                }
            }
        }

        double[] result = {enemyField.getWorldX(maxX), enemyField.getWorldY(maxY)};
        return result;
    }

    public double[] getNearestEnemyToVehicleInCell (SmartVehicle vehicle, int x, int y) throws Exception {
        SmartVehicle enemyVehicle = battleField[y][x].getNearestVehicle(x, y);

        if (enemyVehicle == null) {
            throw new Exception("Blya");
        }
        //@TODO workaround, wotk with terrain and weather factor
        enemyVehicle.getAerialAttackRange();

        double vectorX = enemyVehicle.getX() - vehicle.getX();
        double vectorY = enemyVehicle.getY() - vehicle.getY();


        double localX = (enemyVehicle.getX() - vehicle.getX());
        double localY = (enemyVehicle.getY() - vehicle.getY());

        double distance = Math.sqrt(localX * localX + localY * localY);

        double safetyDistance = (distance - enemyVehicle.getAerialAttackRange()) / distance;

        double safetyX = safetyDistance * vectorX + vehicle.getX();
        double safetyY = safetyDistance * vectorY + vehicle.getY();

        double[] result = {safetyX, safetyY};
        return result;
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
        enemyField.print();
        fighterDamageField.print();
    }
}
