package strategy;

import model.VehicleType;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


public class EnemyField {


    private Integer lastUpdateNuclearAttackRatingTick = -1;
    private SortedSet<NuclearAttackPoint> nuclearAttackPointsRating;

    private PPFieldEnemy enemyField;

    private Integer width;
    private Integer height;

    private DamageField enemyDamageField;
    private DamageField allyDamageField;

    public EnemyField() {
        width = MyStrategy.battleField.getWidth();
        height = MyStrategy.battleField.getHeight();

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
        if (edges.get(1).point != null && damageField.getFactorOld(edges.get(1).point) > 0) {
            return damageField.getWorldPoint(edges.get(1).point);
        }

        return null;
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
