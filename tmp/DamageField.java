
import model.VehicleType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class DamageField {

    private Map<VehicleType, PPFieldEnemy> vehicleDamageByType;

    private Integer width;
    private Integer height;

    public DamageField(Integer width, Integer height) {
        this.width = width;
        this.height = height;

        vehicleDamageByType = new HashMap<>();
        vehicleDamageByType.put(VehicleType.HELICOPTER, new PPFieldEnemy(width, height));
        vehicleDamageByType.put(VehicleType.FIGHTER, new PPFieldEnemy(width, height));
        vehicleDamageByType.put(VehicleType.TANK, new PPFieldEnemy(width, height));
        vehicleDamageByType.put(VehicleType.IFV, new PPFieldEnemy(width, height));
    }

    public void addFactor (int x, int y, SmartVehicle vehicle, int operator) {
        HashSet<Point2D> addedPoints = new HashSet();

        Predicate<VehicleType> nonZero = (type) -> SmartVehicle.getEnemyDamage(vehicle.getType(), type) > 0;
        Function<VehicleType, Integer> counterAttack = (type) -> !nonZero.test(type) ? 1 : SmartVehicle.getEnemyDamage(vehicle.getType(), type);
        Function<Double, Float> doubleFactorToFloat = (factor) -> factor >= 0 ? (float)Math.floor(factor) : (float)Math.ceil(factor);

        double factor = vehicle.getAttackCooldownTicks() > 0 ? vehicle.getDurability() * (vehicle.getAttackCooldownTicks() - vehicle.getRemainingAttackCooldownTicks()) / vehicle.getAttackCooldownTicks() : 0;
        for (int j = -2; j <= 2 && j + y < height; j++) {
            for (int i = -2; i <= 2 && i + x < width; i++ ) {
                if (x + i < 0 || y + j < 0) {
                    continue;
                }
                int newX = x + i;
                int newY = y + j;

                for (VehicleType type : VehicleType.values()) {
                    if (vehicleDamageByType.containsKey(type)) {
                        vehicleDamageByType.
                                get(type).
                                addFactor(newX, newY, doubleFactorToFloat.apply(factor * SmartVehicle.getEnemyDamage(type, vehicle.getType()) / counterAttack.apply(type)) * operator);
                    }
                }
                addedPoints.add(new Point2D(newX, newY));
            }
        }
        for (VehicleType type : VehicleType.values()) {
            if (vehicleDamageByType.containsKey(type)) {
                if (vehicle.isTerrain()) {
                    vehicleDamageByType.
                            get(type).
                            addLinearPPValue(x, y, (factor * SmartVehicle.getEnemyDamage(type, vehicle.getType()) / counterAttack.apply(type)) * operator, addedPoints);
                } else {
                    vehicleDamageByType.
                            get(type).
                            addLinearPPValue(x, y, (factor * SmartVehicle.getEnemyDamage(type, vehicle.getType()) / counterAttack.apply(type)) * operator, addedPoints);
                }

            }
        }
    }

    public PPFieldEnemy getVehicleTypeDamageField(VehicleType type) {
        return vehicleDamageByType.get(type);
    }

    public void print() {

        vehicleDamageByType.entrySet().stream().forEach(ppFieldEnemy -> {
            System.out.println("---------------->" + ppFieldEnemy.getKey());
            ppFieldEnemy.getValue().print();
        });
    }
}
