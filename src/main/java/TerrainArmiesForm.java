import model.Vehicle;
import model.VehicleType;

import java.util.HashMap;
import java.util.Map;

public class TerrainArmiesForm {

    private short cells[][] = new short[2][2];
    private Map<VehicleType, SmartVehicle> vehicleTypeAngle;
    private Point2D zeroPoint;
    public TerrainArmiesForm() {
        vehicleTypeAngle = new HashMap<>();
        zeroPoint = new Point2D(0,0);
    }

    public static int armyParts = 5;
    public static int armyHeight = 60;
    private int currentArmyPart = 1;
    public void expansion(VehicleType type) {
        int selectStartHeight = armyHeight / armyParts * currentArmyPart;

        Point2D armyStartPos = vehicleTypeAngle.get(type).getPoint();

        Point2D selectPoint = new Point2D(armyStartPos.getX(), armyStartPos.getIntY() + selectStartHeight);
        
    }


    public void updateVehicle(SmartVehicle vehicle) {
        if (vehicle.isTerrain()) {

            if (!vehicleTypeAngle.containsKey(vehicle.getType())) {
                vehicleTypeAngle.put(vehicle.getType(), vehicle);
            } else {
                double distanceToZero = vehicle.getPoint().distance(zeroPoint);
                if (distanceToZero > vehicleTypeAngle.get(vehicle.getType()).getPoint().distance(zeroPoint)) {
                    vehicleTypeAngle.put(vehicle.getType(), vehicle);
                }
            }

        }
    }
}
