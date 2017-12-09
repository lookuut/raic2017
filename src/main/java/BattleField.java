
import model.Vehicle;
import model.VehicleType;

import java.util.List;
import java.util.Map;

public class BattleField {

    protected BattleFieldCell[][] battleField;
    protected List<Army> armyList;

    protected int pFieldWidth;
    protected int pFieldHeight;

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

            MyStrategy.enemyField.removeFromCellVehicle(lastVehicleX, lastVehicleY, vehicle);
        }

        if (vehicle.getDurability() > 0 && (vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell))) {
            battleFieldCell.addVehicle(vehicle);
            MyStrategy.enemyField.addVehicleToCell(battleFieldX, battleFieldY, vehicle);
        }

        vehicle.setBattleFieldCell(battleFieldCell);
     }


    public int getWidth() {
        return pFieldWidth;
    }

    public PPField calcEnemyFieldAvgValues(PPField field, float damage, boolean isAerial) {
        PPField avgField = new PPField(field.getWidth(), field.getHeight());
        for (int y = 0; y < field.getHeight(); y++) {
            for (int x = 0; x < field.getWidth(); x++) {
                if (field.getFactor(x, y) > 0) {
                    BattleFieldCell cell = getBattleFieldCell(x,y);
                    Map<Long, SmartVehicle> vehicles = cell.getVehicles(MyStrategy.getEnemyPlayerId());
                    Long count = vehicles.values().stream().filter(vehicle -> vehicle.getDurability() > 0 && vehicle.isAerial() == isAerial).count();
                    avgField.setFactor(x, y,   field.getFactor(x,y) / count.intValue() - damage);
                }
            }
        }
        return avgField;
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

    public PPFieldEnemy getDamageField(VehicleType type) {
        PPFieldEnemy damageField = new PPFieldEnemy(getWidth(), getHeight());
        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getHeight(); i++) {
                if (getBattleFieldCell(i, j).getVehicles(MyStrategy.getEnemyPlayerId()).size() > 0) {
                    int damage = 0;
                    for (Map.Entry<VehicleType, Integer> entry : getBattleFieldCell (i, j).getEnemyVehiclesTypeCountMap().entrySet()) {
                        damage += SmartVehicle.getEnemyDamage(type, entry.getKey()) * entry.getValue();
                    }
                    damageField.addLinearPPValue(i, j, damage);
                }
            }
        }
        return damageField;
    }
}
