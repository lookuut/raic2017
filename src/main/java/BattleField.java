
import geom.Point2D;

import java.util.List;

public class BattleField {

    protected BattleFieldCell[][] battleField;
    protected List<Army> armyList;

    protected int pFieldWidth;
    protected int pFieldHeight;

    //temporary code remove it before upload
    protected PPField typesPPfield;
    protected PPField aerialPPField;
    protected PPField terrainPPField;
    private Integer cellSize;

    public BattleField (Integer cellSize) {
        this.cellSize = cellSize;

        pFieldWidth = (int)(Math.ceil(MyStrategy.game.getWorldWidth() / cellSize));
        pFieldHeight = (int)(Math.ceil(MyStrategy.game.getWorldHeight() / cellSize));

        battleField = new BattleFieldCell[pFieldHeight][pFieldWidth];

        aerialPPField = new WeatherPPField(pFieldWidth, pFieldHeight);
        terrainPPField = new TerrainPPField(pFieldWidth, pFieldHeight);


        typesPPfield = new PPField(pFieldWidth, pFieldHeight);

        for (int y = 0; y < pFieldHeight; y++) {
            for (int x = 0; x < pFieldWidth; x++) {
                this.battleField[y][x] = new BattleFieldCell(x,y);
            }
        }
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

        if ((vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell) || vehicle.getDurability() == 0) {
            vehicleBattleFieldCell.remove(vehicle);

            Integer lastVehicleX = vehicleBattleFieldCell.getX();
            Integer lastVehicleY = vehicleBattleFieldCell.getY();

            if (vehicle.getDurability() == 0) {
                lastVehicleX = battleFieldX;
                lastVehicleY = battleFieldY;
            }

            aerialPPField.addFactor(lastVehicleX, lastVehicleY,-vehicle.getAerialPPFactor());
            terrainPPField.addFactor(lastVehicleX, lastVehicleY,-vehicle.getTerrainPPFactor());
            typesPPfield.setFactor(lastVehicleX, lastVehicleY, 0);

            if (!vehicle.isAlly()) {
                MyStrategy.enemyField.removeFromCellVehicle(lastVehicleX, lastVehicleY, vehicle);
            }
        }

        if (vehicle.getDurability() > 0 && (vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell))) {
            battleFieldCell.addVehicle(vehicle);

            aerialPPField.addFactor(battleFieldX, battleFieldY, vehicle.getAerialPPFactor());
            terrainPPField.addFactor(battleFieldX, battleFieldY, vehicle.getTerrainPPFactor());
            typesPPfield.setFactor(battleFieldX, battleFieldY, vehicle.getTypeInt());

            MyStrategy.enemyField.addVehicleToCell(battleFieldX, battleFieldY, vehicle);
        }

        vehicle.setBattleFieldCell(battleFieldCell);
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


    public BattleFieldCell getBattleFieldCell(int x, int y) {
        return battleField[y][x];
    }

    public void print() {
        typesPPfield.print();
    }
}
