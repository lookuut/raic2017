import model.VehicleType;

import java.util.*;
import java.util.function.Function;


public class EnemyField {


    private Integer lastUpdateNuclearAttackRatingTick = -1;
    private SortedSet<NuclearAttackPoint> nuclearAttackPointsRating;

    private PPFieldEnemy aerialDamageField;
    private PPFieldEnemy terrainDamageField;

    //be attention with calc avg values
    private PPFieldEnemy aerialToTerrainEnemyField;
    private PPFieldEnemy aerialToAerialEnemyField;
    private PPFieldEnemy terrainToAerialEnemyField;
    private PPFieldEnemy terrainToTerrainEnemyField;


    private PPFieldEnemy enemyField;

    private Integer width;
    private Integer height;
    private BattleField battleField;

    public EnemyField(BattleField battleField) {
        this.battleField = battleField;
        width = battleField.getWidth();
        height = battleField.getHeight();

        enemyField = new PPFieldEnemy(width, height);

        aerialDamageField = new PPFieldEnemy(width, height);
        terrainDamageField = new PPFieldEnemy(width, height);

        aerialToTerrainEnemyField = new PPFieldEnemy(width, height);
        aerialToAerialEnemyField = new PPFieldEnemy(width, height);
        terrainToAerialEnemyField = new PPFieldEnemy(width, height);
        terrainToTerrainEnemyField = new PPFieldEnemy(width, height);

        nuclearAttackPointsRating = new TreeSet<>();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * @param allyVehicle
     * @return
     */
    public PPField getEnemyField(SmartVehicle allyVehicle) throws Exception {
        if (allyVehicle.getType() == VehicleType.FIGHTER) {
            return battleField.calcEnemyFieldAvgValues(aerialToAerialEnemyField, allyVehicle.getAerialDamage());
        }
        if (allyVehicle.getType() == VehicleType.HELICOPTER) {

            PPField aeToAeAvgEnemyField = battleField.calcEnemyFieldAvgValues(aerialToAerialEnemyField, allyVehicle.getAerialDamage());
            PPField aeToTerAvgEnemyField = battleField.calcEnemyFieldAvgValues(aerialToTerrainEnemyField, allyVehicle.getGroundDamage());
            aeToAeAvgEnemyField.sumField(aeToTerAvgEnemyField);
            return aeToAeAvgEnemyField;
        }

        if (allyVehicle.getType() == VehicleType.TANK || allyVehicle.getType() == VehicleType.IFV) {
            PPField aeToAeAvgEnemyField = battleField.calcEnemyFieldAvgValues(terrainToAerialEnemyField, allyVehicle.getAerialDamage());
            PPField aeToTerAvgEnemyField = battleField.calcEnemyFieldAvgValues(terrainToTerrainEnemyField, allyVehicle.getGroundDamage());
            aeToAeAvgEnemyField.sumField(aeToTerAvgEnemyField);
            return aeToAeAvgEnemyField;
        }

        throw new Exception("Cant attack type search enemy " + allyVehicle.getType().toString());
    }

    /**
     * @param type
     * @return
     */
    public PPFieldEnemy getDamageField(VehicleType type) throws Exception {
        if (SmartVehicle.isTerrain(type)) {
            return terrainDamageField;
        }
        return aerialDamageField;
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
        terrainDamageField.addLinearPPValue(x, y, vehicle.getDamagePPFactor(false) * operator);
        aerialDamageField.addLinearPPValue(x, y, vehicle.getDamagePPFactor( true) * operator);

        aerialToAerialEnemyField.addFactor(x, y, vehicle.getDefencePPFactor(true, true) * operator);
        aerialToTerrainEnemyField.addFactor(x, y, vehicle.getDefencePPFactor(true, false) * operator);

        terrainToAerialEnemyField.addFactor(x, y, vehicle.getDefencePPFactor(false, true) * operator);
        terrainToTerrainEnemyField.addFactor(x, y, vehicle.getDefencePPFactor(false, false) * operator);

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
                        PPFieldEnemy damageField = getDamageField(type);
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
                PPFieldEnemy field = getDamageField(vehicleType);
                factor += field.getPointRadiousFactorSum( field.getTransformedPoint(escapePoint.add(safetyPointVector)), CustomParams.safetyDistance);
            }

            if (factor < minFactor) {
                minFactorVec = destPoint;
                minFactor = factor;
            }
        }

        return minFactorVec;
    }


    public SortedSet<NuclearAttackPoint> getNuclearAttackPointsRating() {
        boolean recalcRating = false;
        if (nuclearAttackPointsRating.size() == 0) {
            recalcRating = true;
        }

        if (MyStrategy.world.getTickIndex() - lastUpdateNuclearAttackRatingTick >= CustomParams.nuclearAttackRatingRecalcTickInterval) {
            recalcRating = true;
        }

        if (recalcRating) {
            recalcNuclearAttackPointsRating();
            lastUpdateNuclearAttackRatingTick = MyStrategy.world.getTickIndex();
        }

        return nuclearAttackPointsRating;
    }

    private void recalcNuclearAttackPointsRating() {
        nuclearAttackPointsRating.clear();
        float prevValue = 0;
        HashSet<Integer> visitedCells = new HashSet<>();
        int radius = (int)Math.ceil(MyStrategy.game.getTacticalNuclearStrikeRadius() * getWidth() / MyStrategy.world.getWidth());
        for (int y = 0; y < enemyField.getHeight(); y++) {
            for (int x = 0; x < enemyField.getWidth(); x++) {

                if (!visitedCells.contains(y * enemyField.getWidth() + x)) {

                    if ((enemyField.getFactor(x, y) < prevValue || (enemyField.getFactor(x, y) > 0 && x == (getWidth() - 1)))) {
                        float maxValueInCircle = 0;
                        int maxValueX = 0;
                        int maxValueY = 0;
                        List<Integer> maxValueVisitedCells = null;
                        for (int j = -radius; j <= radius && y + j < enemyField.getHeight(); j++) {
                            for (int i = -radius; i <= radius && x + i < enemyField.getWidth(); i++) {
                                int localX = x + i;
                                int localY = y + j;

                                if (i * i + j * j <= radius * radius && localX >= 0 && localY >= 0 && !visitedCells.contains(localY * enemyField.getWidth() + localX)) {
                                    List<Integer> localVisitedCells = new ArrayList<>();
                                    float maxValueInCircleLocal = enemyField.sumFactorInPointRadious(new Point2D(localX, localY), radius, localVisitedCells);
                                    if (maxValueInCircleLocal > maxValueInCircle) {
                                        maxValueInCircle = maxValueInCircleLocal;
                                        maxValueX = localX;
                                        maxValueY = localY;
                                        maxValueVisitedCells = localVisitedCells;
                                    }
                                }
                            }
                        }
                        if (maxValueInCircle > 0) {
                            visitedCells.addAll(maxValueVisitedCells);
                            nuclearAttackPointsRating.add(new NuclearAttackPoint(enemyField.getWorldPoint(new Point2D(maxValueX, maxValueY)), maxValueInCircle, enemyField.getWidth()));
                        }
                    }

                    prevValue = enemyField.getFactor(x, y);
                }
            }
        }


        if (nuclearAttackPointsRating.size() > CustomParams.nuclearAttackRatingItemCount) {// cut tail
            Iterator it = nuclearAttackPointsRating.iterator();
            Integer count = 0;
            NuclearAttackPoint point = null;
            while (it.hasNext() && count < CustomParams.nuclearAttackRatingItemCount) {
                point = (NuclearAttackPoint)it.next();
                count++;
            }

            nuclearAttackPointsRating.tailSet(point).clear();
        }
    }

    public void print() {
        aerialDamageField.print();
    }
}
