
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BattleField {

    protected MyStrategy strategy;
    protected BattleFieldCell[][] battleField;

    protected int pFieldWidth;
    protected int pFieldHeight;

    public static Integer cellSize = 16;

    public BattleField (MyStrategy strategy) {
        this.strategy = strategy;

        pFieldWidth = (int)(Math.ceil(strategy.getGame().getWorldWidth() / BattleField.cellSize));
        pFieldHeight = (int)(Math.ceil(strategy.getGame().getWorldHeight() / BattleField.cellSize));

        this.battleField = new BattleFieldCell[pFieldWidth][pFieldHeight];

        for (int y = 0; y < pFieldHeight; y++) {
            for (int x = 0; x < pFieldWidth; x++) {
                this.battleField[y][x] = new BattleFieldCell(x,y, strategy);
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
            }

            if (vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell)) {
                battleFieldCell.addVehicle(vehicle);
            }

            vehicle.setBattleFieldCell(battleFieldCell);
        }
    }

    public List<Army> formArmies () {
        List<Army> armyList = new ArrayList();
        Collection<Integer> visitedPoints = new ArrayList();

        for (Integer playerId = 0; playerId < MyStrategy.max_player_index; playerId++) {
            for (int y = 0; y < pFieldHeight; y++) {
                for (int x = 0; x < pFieldWidth; x++ ) {
                    Integer point = y * pFieldWidth + x;
                    if (battleField[y][x].isHaveVehicles(playerId) && !visitedPoints.contains(point)) {
                        Army army = (playerId == this.strategy.getMyPlayerId() - 1) ? new AllyArmy() : new EnemyArmy();
                        this.recursiveCircumVention(x, y, army, visitedPoints, playerId);
                        armyList.add(army);
                    }
                }
            }
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

}
