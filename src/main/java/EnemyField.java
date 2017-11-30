import geom.Point2D;
import model.Vehicle;
import model.VehicleType;

import java.util.HashSet;
import java.util.Set;

public class EnemyField {

    private EnemyPPField tankDamageField;
    private EnemyPPField fighterDamageField;
    private EnemyPPField helicopterDamageField;
    private EnemyPPField ifvDamageField;
    private EnemyPPField enemyField;

    private Integer width;
    private Integer height;

    public EnemyField(BattleField battleField) {
        width = battleField.getPFieldWidth();
        height = battleField.getPFieldHeight();

        tankDamageField = new EnemyPPField(width, height);
        fighterDamageField = new EnemyPPField(width, height);
        helicopterDamageField = new EnemyPPField(width, height);
        ifvDamageField = new EnemyPPField(width, height);
        enemyField = new EnemyPPField(width, height);
    }


    /**
     * @desc bad style, rewrite it
     * @param type
     * @return
     */
    public EnemyPPField getDamageField(VehicleType type) throws Exception{
        switch (type) {
            case HELICOPTER:
                return helicopterDamageField;
            case TANK:
                return tankDamageField;
            case FIGHTER:
                return fighterDamageField;
            case IFV:
                return ifvDamageField;
        }

        throw new Exception("Unknown vehicle type " + type.toString());
    }

    /**
     * @desc get nearest safety point for vehicle in cell point
     * @param allyVehicle
     * @param point
     * @return
     * @throws Exception
     */
    public Point2D getNearestEnemyToVehicleInCell (SmartVehicle allyVehicle, Point2D point) throws Exception {
        SmartVehicle enemyVehicle = MyStrategy.battleField.getBattleFieldCell((int)point.getX(),(int)point.getY()).getNearestVehicle((int)point.getX(), (int)point.getY());

        double attackRange = 0;
        Point2D vector = point;

        //@TODO workaround, use terrain and weather factor
        if (enemyVehicle != null) {
            attackRange = enemyVehicle.getAttackRange(allyVehicle);
            vector = enemyVehicle.getPoint().subtract(allyVehicle.getPoint());
        }

        if (enemyVehicle == null) {
            System.out.println("Cant find enemy in " + point);
        }

        double distance = vector.magnitude();
        double safetyDistance = (distance - attackRange) / distance;

        return vector.multiply(safetyDistance).add(allyVehicle.getPoint());
    }

    public void removeFromCellVehicle(int x, int y, SmartVehicle vehicle) {
        if (!vehicle.isAlly()) {
            updateCell(x, y, vehicle, -1);
        }
    }

    public void addVehicleToCell(int x, int y, SmartVehicle vehicle) {
        if (!vehicle.isAlly()) {
            updateCell(x, y, vehicle, 1);
        }
    }

    protected void updateCell(int x, int y, SmartVehicle vehicle, int operator) {
        tankDamageField.addFactor(x, y, vehicle.getDamagePPFactor(VehicleType.TANK, false) * operator);
        fighterDamageField.addFactor(x, y, vehicle.getDamagePPFactor(VehicleType.FIGHTER, true) * operator);
        ifvDamageField.addFactor(x, y, vehicle.getDamagePPFactor(VehicleType.IFV, false) * operator);
        helicopterDamageField.addFactor(x, y, vehicle.getDamagePPFactor(VehicleType.HELICOPTER, true) * operator);
        enemyField.addFactor(x, y, operator);
    }


    public Point2D nuclearAttackTarget () {
        return enemyField.nuclearAttackTarget();
    }

    public Point2D[] getNearestEnemyPointAndSafetyPoint(Point2D point, float safetyDistance) {
        return enemyField.getNearestEnemyPointAndSafetyPoint(point, safetyDistance);
    }

    public PPField getVehicleTypesField (Set<VehicleType> vehicleTypes) throws Exception{
        PPField sum = new PPField(width, height);
        for (VehicleType type : vehicleTypes) {
            sum.sumField(getDamageField(type));
        }

        return sum;
    }
}
