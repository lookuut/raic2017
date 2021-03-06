package strategy;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class BattleFieldCell {

    private Map<Long, SmartVehicle> vehicles[];

    protected Integer x;
    protected Integer y;

    protected Double minMaxXY[][];

    protected Function<Long, Integer> playerIdToIndex;

    public static final int max_x = 0;
    public static final int max_y = 1;
    public static final int min_x = 2;
    public static final int min_y = 3;

    public BattleFieldCell(Integer x, Integer y) {
        this.x = x;
        this.y = y;

        minMaxXY = new Double[CustomParams.max_player_index][4];
        vehicles = new SortedMap[2];

        for (int playerIndex = 0; playerIndex < CustomParams.max_player_index; playerIndex++) {
            vehicles[playerIndex] = new TreeMap<>();
            minMaxXY[playerIndex] = new Double[4];

            minMaxXY[playerIndex][max_x] = 0.0;
            minMaxXY[playerIndex][max_y] = 0.0;
            minMaxXY[playerIndex][min_x] = MyStrategy.world.getWidth();
            minMaxXY[playerIndex][min_y] = MyStrategy.world.getHeight();
        }

        playerIdToIndex = (playerId) -> playerId.intValue() - 1;
    }

    public void addVehicle(SmartVehicle vehicle) {
        int pId = playerIdToIndex.apply(vehicle.getPlayerId());
        minMaxUpdate(vehicle);
        vehicles[pId].put(vehicle.getId(), vehicle);
    }


    protected void minMaxUpdate(SmartVehicle vehicle) {
        int pId = playerIdToIndex.apply(vehicle.getPlayerId());
        minMaxXY[pId][max_x] = Math.max(vehicle.getX(), minMaxXY[pId][max_x]);
        minMaxXY[pId][max_y] = Math.max(vehicle.getY(), minMaxXY[pId][max_y]);
        minMaxXY[pId][min_x] = Math.min(vehicle.getX(), minMaxXY[pId][min_x]);
        minMaxXY[pId][min_y] = Math.min(vehicle.getY(), minMaxXY[pId][min_y]);
    }

    public Map<Long, SmartVehicle> getVehicles(Long playerId) {
        return vehicles[playerIdToIndex.apply(playerId)];
    }

    public void remove(SmartVehicle vehicle) {
        if (vehicles[playerIdToIndex.apply(vehicle.getPlayerId())].containsKey(vehicle.getId())) {
            vehicles[playerIdToIndex.apply(vehicle.getPlayerId())].remove(vehicle.getId());
        }

        recalculationMaxMin(playerIdToIndex.apply(vehicle.getPlayerId()));
    }

    protected void recalculationMaxMin (Integer playerId) {
        for (Map.Entry<Long, SmartVehicle> entry : vehicles[playerId].entrySet()) {
            minMaxUpdate(entry.getValue());
        }
    }

    public Integer getX() {
        return x;
    }
    public Integer getY() {
        return y;
    }

    public Point2D getPoint() {
        return new Point2D(x,y);
    }

}
