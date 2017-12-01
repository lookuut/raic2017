import model.VehicleType;

import java.util.Set;
import java.util.function.Function;


public class EnemyField {

    private EnemyPPField tankDamageField;
    private EnemyPPField fighterDamageField;
    private EnemyPPField helicopterDamageField;
    private EnemyPPField ifvDamageField;
    private EnemyPPField arrvDamageField;

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
        arrvDamageField = new EnemyPPField(width, height);
        enemyField = new EnemyPPField(width, height);
    }


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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
            case ARRV:
                return arrvDamageField;
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
        arrvDamageField.addFactor(x, y, vehicle.getDamagePPFactor(VehicleType.ARRV, true) * operator);
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

    public Point2D onDanger(Set<VehicleType> types, Point2D armyCenter, double dangerRadious) {

        int propose = (int)(MyStrategy.world.getWidth() / getWidth());
        int intDangerRadoius = (int)Math.floor(dangerRadious / propose);
        Point2D armyTransformedCentre = new Point2D(Math.round(armyCenter.getX() / propose), Math.round(armyCenter.getY() / propose));

        Function<Integer, Point2D> xAxisFunction = (y) -> {
            try {
                for (int x = 1; x <= intDangerRadoius; x++) {
                    for (VehicleType type : types) {
                        EnemyPPField damageField = getDamageField(type);
                        if (armyTransformedCentre.getIntX() + x < getWidth() && damageField.getFactor(armyTransformedCentre.getIntX() + x, y) > 0) {
                            return damageField.getWorldPoint(new Point2D(armyTransformedCentre.getIntX() + x, y));
                        }

                        if (armyTransformedCentre.getIntX() - x >= 0 && damageField.getFactor(armyTransformedCentre.getIntX() - x, y) > 0) {
                            return damageField.getWorldPoint(new Point2D(armyTransformedCentre.getIntX() - x, y));
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        };

        for (int y = 0; y <= intDangerRadoius; y++) {
            if (armyTransformedCentre.getIntY() + y < getHeight()) {
                Point2D point = xAxisFunction.apply(armyTransformedCentre.getIntY() + y);
                if (point != null) {
                    return point;
                }
            }

            if (armyTransformedCentre.getIntY() - y >= 0) {
                Point2D point = xAxisFunction.apply(armyTransformedCentre.getIntY() - y);
                if (point != null) {
                    return point;
                }
            }
        }

        return null;
    }

    public Point2D searchNearestSafetyPoint(Set<VehicleType> vehicleTypes, Point2D fromPoint, Point2D escapePoint) throws Exception {
        Point2D direction = fromPoint.subtract(escapePoint).normalize();

        double minFactor = Double.MAX_VALUE;
        Point2D minFactorVec = null;
        for (int angleSector = 0; angleSector < CustomParams.searchSafetyZoneSectorCount; angleSector++) {
            double angle = (angleSector % 2 == 1 ? -1 : 1) * angleSector * (2 * Math.PI) / CustomParams.searchSafetyZoneSectorCount;
            Point2D safetyPointVector = direction.turn(angle).multiply(CustomParams.safetyDistance);
            Point2D destPoint = escapePoint.add(safetyPointVector);

            if (destPoint.getX() >= MyStrategy.world.getWidth() || destPoint.getY() >= MyStrategy.world.getHeight() || destPoint.getX() < 0 || destPoint.getY() < 0) {
                continue;
            }

            double factor = 0;
            for (VehicleType vehicleType : vehicleTypes) {
                EnemyPPField field = getDamageField(vehicleType);
                factor += field.getPointRadiousFactorSum( field.getTransformedPoint(escapePoint.add(safetyPointVector)), CustomParams.safetyDistance);
            }

            if (factor < minFactor) {
                minFactorVec = destPoint;
                minFactor = factor;
            }
        }

        return minFactorVec;
    }
}
