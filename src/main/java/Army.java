import java.util.HashMap;

public class Army {

    protected HashMap<Integer, BattleFieldCell> battleFieldCellMap;

    protected double maxX;
    protected double maxY;

    protected double minX = Double.MAX_VALUE;
    protected double minY = Double.MAX_VALUE;


    public Army() {
        battleFieldCellMap = new HashMap<>();
    }

    public void addBattleFieldCell (Integer index, BattleFieldCell cell, Integer pid) {

        maxX = Math.max(cell.getMaxX(pid), maxX);
        maxY = Math.max(cell.getMaxY(pid), maxY);
        minX = Math.min(cell.getMinX(pid), minX);
        minY = Math.min(cell.getMinY(pid), minY);

        battleFieldCellMap.put(index, cell);
    }

    protected void minMaxUpdate(SmartVehicle vehicle) {
        this.maxX = Math.max(vehicle.getX(), maxX);
        this.maxY = Math.max(vehicle.getY(), maxY);

        this.minX = Math.min(vehicle.getX(), minX);
        this.minY = Math.min(vehicle.getY(), minY);
    }


    public double getMaxX() {
        return maxX;
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinY() {
        return minY;
    }

    public float getAvgX () {
        return (float)((getMinX() + getMaxX()) / 2.0);
    }

    public float getAvgY () {
        return (float) ((getMinY() + getMaxY()) / 2.0);
    }

}
