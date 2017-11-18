import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

public class BattleFieldCell {

    protected MyStrategy strategy;
    protected SortedMap<Long, SmartVehicle> vehicles[];
    protected Integer x;
    protected Integer y;

    protected Double minMaxXY[][];

    protected Function<Integer, Integer> playerIdToIndex;

    public static final int max_x = 0;
    public static final int max_y = 1;
    public static final int min_x = 2;
    public static final int min_y = 3;

    public BattleFieldCell(Integer x, Integer y, MyStrategy strategy) {

        this.strategy = strategy;
        this.x = x;
        this.y = y;

        minMaxXY = new Double[MyStrategy.max_player_index][4];
        vehicles = new SortedMap[2];

        for (int playerIndex = 0; playerIndex < MyStrategy.max_player_index; playerIndex++) {
            vehicles[playerIndex] = new TreeMap<>();
            minMaxXY[playerIndex] = new Double[4];

            minMaxXY[playerIndex][max_x] = 0.0;
            minMaxXY[playerIndex][max_y] = 0.0;
            minMaxXY[playerIndex][min_x] = strategy.getWorld().getWidth();
            minMaxXY[playerIndex][min_y] = strategy.getWorld().getHeight();
        }

        playerIdToIndex = (playerId) -> playerId.intValue() - 1;
    }

    public void addVehicle(SmartVehicle vehicle) {
        int pId = playerIdToIndex.apply((int)vehicle.getPlayerId());
        minMaxUpdate(vehicle);
        vehicles[pId].put(vehicle.getId(), vehicle);
    }

    protected void minMaxUpdate(SmartVehicle vehicle) {
        int pId = playerIdToIndex.apply((int)vehicle.getPlayerId());
        minMaxXY[pId][max_x] = Math.max(vehicle.getX(), minMaxXY[pId][max_x]);
        minMaxXY[pId][max_y] = Math.max(vehicle.getY(), minMaxXY[pId][max_y]);
        minMaxXY[pId][min_x] = Math.min(vehicle.getX(), minMaxXY[pId][min_x]);
        minMaxXY[pId][min_y] = Math.min(vehicle.getY(), minMaxXY[pId][min_y]);
    }

    public Map<Long, SmartVehicle> getVehicles(Integer playerIndex) {
        return vehicles[playerIndex];
    }

    public void remove(SmartVehicle vehicle) {
        vehicles[playerIdToIndex.apply((int)vehicle.getPlayerId())].remove(vehicle.getId());
        recalculationMaxMin(playerIdToIndex.apply((int)vehicle.getPlayerId()));
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

    public double getMaxX(Integer pid) {
        return minMaxXY[pid][max_x];
    }

    public double getMaxY(Integer pid) {
        return minMaxXY[pid][max_y];
    }

    public double getMinX(Integer pid) {
        return minMaxXY[pid][min_x];
    }

    public double getMinY(Integer pid) {
        return minMaxXY[pid][min_y];
    }

    public boolean isHaveVehicles (Integer playerIndex) {
        return vehicles[playerIndex].size() > 0;
    }
}
