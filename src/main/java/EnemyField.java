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
            if (type == VehicleType.ARRV) {
                damageField.sumField(enemyDamageField.getVehicleTypeDamageField(VehicleType.TANK));
            } else {
                damageField.sumField(enemyDamageField.getVehicleTypeDamageField(type));
            }
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


        List<PPFieldPoint> edges = damageField.getEdgesValueInRadious(armyTransformedCentre, intDangerRadoius);
        if (edges.get(1).point != null && damageField.getFactor(edges.get(1).point) > 0) {
            return damageField.getWorldPoint(edges.get(1).point);
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

            MyStrategy.battleField.defineArmies();
            List<Army> enemyArmies = MyStrategy.battleField.getEnemyArmies();
            nuclearAttackPointsRating.clear();
            int enemyVehiclesCount = MyStrategy.getEnemyVehicles().size();

            for (Army army : enemyArmies) {
                if (army.getVehicleCount() / (float)enemyVehiclesCount > CustomParams.minEnemyPercentageToNuclearAttack) {
                    nuclearAttackPointsRating.add(new NuclearAttackPoint(army));
                }
            }

            lastUpdateNuclearAttackRatingTick = MyStrategy.world.getTickIndex();
        }

        return nuclearAttackPointsRating;
    }

    public void print() {
        //enemyDamageField.print();
        System.out.println("Ally=================================================>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        allyDamageField.print();
    }
}
