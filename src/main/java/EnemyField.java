import model.VehicleType;

import java.util.*;
import java.util.function.Function;


public class EnemyField {


    private Integer lastUpdateNuclearAttackRatingTick = -1;
    private SortedSet<NuclearAttackPoint> nuclearAttackPointsRating;

    private PPFieldEnemy enemyField;

    private Integer width;
    private Integer height;
    private BattleField battleField;

    private DamageField enemyDamageField;
    private DamageField allyDamageField;

    public EnemyField(BattleField battleField) {
        this.battleField = battleField;
        width = battleField.getWidth();
        height = battleField.getHeight();

        enemyField = new PPFieldEnemy(width, height);

        nuclearAttackPointsRating = new TreeSet<>();
        enemyDamageField = new DamageField(width, height);
        allyDamageField = new DamageField(width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * @param types
     * @return
     */
    public PPField getEnemyDamageField(Set<VehicleType> types) throws Exception {
        PPFieldEnemy damageField = new PPFieldEnemy(getWidth(), getHeight());

        for (VehicleType type : types) {
            damageField.sumField(enemyDamageField.getVehicleTypeDamageField(type));
        }

        return damageField;
    }

    /**
     * @param types
     * @return
     */
    public PPFieldEnemy getDamageField(Set<VehicleType> types) {
        PPFieldEnemy damageField = new PPFieldEnemy(getWidth(), getHeight());

        for (VehicleType type : types) {
            if (type == VehicleType.ARRV) {
                damageField.sumField(enemyDamageField.getVehicleTypeDamageField(VehicleType.TANK));
            } else {
                damageField.sumField(enemyDamageField.getVehicleTypeDamageField(type));
                damageField.operateField(allyDamageField.getVehicleTypeDamageField(type), -1);
            }
        }

        return damageField;
    }

    /**
     * @param type
     * @return
     */
    public PPField getDamageField(VehicleType type) {
        return PPField.sumField(enemyDamageField.getVehicleTypeDamageField(type), allyDamageField.getVehicleTypeDamageField(type), -1);
    }



    public void removeFromCellVehicle(int x, int y, SmartVehicle vehicle) {
        updateCell(x, y, vehicle, -1);
    }

    public void addVehicleToCell(int x, int y, SmartVehicle vehicle) {
        updateCell(x, y, vehicle, 1);
    }

    protected void updateCell(int x, int y, SmartVehicle vehicle, int operator) {
        if (vehicle.isAlly()) {
            allyDamageField.addFactor(x, y, vehicle, operator);
        } else {
            enemyDamageField.addFactor(x, y, vehicle, operator);
            enemyField.addFactor(x, y, operator);
        }
    }

    public Point2D onDanger(Set<VehicleType> types, Point2D armyCenter, double dangerRadious) throws Exception {

        int propose = (int)(MyStrategy.world.getWidth() / getWidth());
        int intDangerRadoius = (int)Math.floor(dangerRadious / propose);
        Point2D armyTransformedCentre = new Point2D(Math.round(armyCenter.getX() / propose), Math.round(armyCenter.getY() / propose));
        PPFieldEnemy damageField = getDamageField(types);

        Function<Integer, Point2D> xAxisFunction = (y) -> {
            try {
                for (int x = 1; x <= intDangerRadoius; x++) {
                    for (VehicleType type : types) {

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
            if (armyTransformedCentre.getIntY() + y < getHeight() && armyTransformedCentre.getIntY() + y >= 0) {
                Point2D point = xAxisFunction.apply(armyTransformedCentre.getIntY() + y);
                if (point != null) {
                    return point;
                }
            }

            if (armyTransformedCentre.getIntY() - y >= 0 && armyTransformedCentre.getIntY() - y < getHeight()) {
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
        PPFieldEnemy field = getDamageField(vehicleTypes);
        for (int angleSector = 0; angleSector < CustomParams.searchSafetyZoneSectorCount; angleSector++) {
            double angle = (angleSector % 2 == 1 ? -1 : 1) * angleSector * (2 * Math.PI) / CustomParams.searchSafetyZoneSectorCount;
            Point2D safetyPointVector = direction.turn(angle).multiply(CustomParams.safetyDistance);
            Point2D destPoint = escapePoint.add(safetyPointVector);

            if (destPoint.getX() >= MyStrategy.world.getWidth() || destPoint.getY() >= MyStrategy.world.getHeight() || destPoint.getX() < 0 || destPoint.getY() < 0) {
                continue;
            }

            double factor = field.getPointRadiousFactorSum( field.getTransformedPoint(escapePoint.add(safetyPointVector)), CustomParams.safetyDistance);;

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
        //enemyDamageField.print();
        System.out.println("Ally=================================================>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        allyDamageField.print();
    }
}
