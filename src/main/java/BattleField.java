
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BattleField {

    protected BattleFieldCell[][] battleField;
    protected List<Army> armyList;
    protected Integer last_calc_army_tick_index = -1;
    public static int calc_army_interval = 1;

    protected int pFieldWidth;
    protected int pFieldHeight;

    public static Integer cellSize = 16;

    public BattleField () {

        pFieldWidth = (int)(Math.ceil(MyStrategy.game.getWorldWidth() / BattleField.cellSize));
        pFieldHeight = (int)(Math.ceil(MyStrategy.game.getWorldHeight() / BattleField.cellSize));

        this.battleField = new BattleFieldCell[pFieldWidth][pFieldHeight];

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
            }

            if (vehicleBattleFieldCell == null || (vehicleBattleFieldCell != null && vehicleBattleFieldCell != battleFieldCell)) {
                battleFieldCell.addVehicle(vehicle);
            }

            vehicle.setBattleFieldCell(battleFieldCell);
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

}
