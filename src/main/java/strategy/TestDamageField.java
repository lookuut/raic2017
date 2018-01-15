package strategy;

import model.Vehicle;
import model.VehicleType;

public class TestDamageField {

    public TestDamageField () {

    }

    public void test () {
        int[] groups = new int[0];
        Vehicle allyVehicle = new Vehicle(1, 100,100, 2, 2, 100, 100,
                1, 100, 10000, 20,
                400, 0, 0,
                10, 0, 40, 20, 60,
                0, VehicleType.TANK, false, false, groups);

        SmartVehicle sVehicle = new SmartVehicle(allyVehicle);

        BattleField battleField = new BattleField(CustomParams.tileCellSize);
        battleField.addVehicle(sVehicle);
        PPField field = MyStrategy.enemyField.getDamageField(VehicleType.TANK);

        field.print();
    }
}
