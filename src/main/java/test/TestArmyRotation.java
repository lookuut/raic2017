package test;

import model.Vehicle;
import model.VehicleType;
import strategy.Army;
import strategy.Point2D;
import strategy.SmartVehicle;

import java.util.List;

public class TestArmyRotation {

    public static int vehiclesRowCount = 10;
    public static int vehiclesColumnCount = 10;

    public static Point2D vehiclesStartPoint = new Point2D(100,100);

    public static void main(String[] args) {

        Vehicle[] vehicles = new Vehicle[vehiclesColumnCount * vehiclesRowCount];
        for (int j = 0; j < TestArmyRotation.vehiclesColumnCount; j++) {
            for (int i = 0; i < TestArmyRotation.vehiclesColumnCount; i++) {
                vehicles[j * TestArmyRotation.vehiclesColumnCount + i] = InitWorld.generateVehicle(
                        new Point2D(
                                vehiclesStartPoint.getX() + i * 2 * GameConfig.vehicleRadius ,
                                vehiclesStartPoint.getY() + j * 2 * GameConfig.vehicleRadius
                        ),
                        VehicleType.FIGHTER,
                        WorldConfig.mePlayerId, GameConfig.tankDurability,
                        false);
            }

        }
        InitWorld world = new InitWorld(vehicles);

        Army army = new Army();

        for (Vehicle vehicle : vehicles) {
            SmartVehicle smartVehicle = new SmartVehicle(vehicle);
            army.addVehicle(smartVehicle);
        }

        army.getForm().update(army.getVehicles());
        army.getDamageField().print();

        double angle = army.getMaxDamageVehicleTurnedAngle(new Point2D(0, 0));
        System.out.println(180 * angle / Math.PI);
        angle = army.getMaxDamageVehicleTurnedAngle(new Point2D(0, 400));
        System.out.println(180 * angle / Math.PI);
        angle = army.getMaxDamageVehicleTurnedAngle(new Point2D(400, 400));
        System.out.println(180 * angle / Math.PI);
        angle = army.getMaxDamageVehicleTurnedAngle(new Point2D(200, 0));
        System.out.println(180 * angle / Math.PI);

    }
}
