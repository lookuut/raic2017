
import model.VehicleType;

import java.util.List;
import java.util.Map;

public class NuclearAttackPoint implements Comparable<NuclearAttackPoint> {
    private Army army;
    private double avgDurability;
    private double arrvPercentage;
    private double speed;
    private Point2D point;
    private double vehiclesPercentage;

    public NuclearAttackPoint(Army army) {
        army.getForm().update(army.getVehicles());
        this.army = army;
        this.point = army.getForm().getAvgPoint();

        avgDurability = army.averageDurability() / 100.0;
        arrvPercentage = 0;

        Map<VehicleType, List<SmartVehicle>> vehiclesByType = army.getVehiclesByType();
        if (vehiclesByType.containsKey(VehicleType.ARRV)) {
            arrvPercentage = vehiclesByType.get(VehicleType.ARRV).size() / army.getVehicleCount();
        }
        vehiclesPercentage = 1 - army.getVehicleCount() / (double) MyStrategy.getEnemyVehicles().size();
        speed = army.getSpeed();
    }

    public double getPointFactor() {
        return arrvPercentage + avgDurability + speed + vehiclesPercentage;
    }

    public int compareTo(NuclearAttackPoint attackPoint) {
        return Double.compare(getPointFactor(), attackPoint.getPointFactor());
    }

    public Point2D getPoint() {
        return point;
    }

    public int hashCode() {
        return army.hashCode();
    }
}
