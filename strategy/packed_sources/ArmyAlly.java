
import model.VehicleType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmyAlly extends Army {

    /**
     * @var target enemies
     */
    private Map<Long, SmartVehicle> targetEnemyMap;

    /**
     * @var army group id
     */
    protected Integer groupId;

    /**
     * @var battle field
     */
    protected BattleField battleField;

    /**
     * @var aerial weather constant field
     */
    protected PPField constAerialPPField;

    /**
     * @var terrain constant field
     */
    protected PPField constTerrainPPField;

    /**
     * @var army track
     */
    private Track track;

    /**
     * @var last compact army tick
     */
    private Integer lastCompactTick;

    /**
     * @var ally army target vehicle
     */
    private SmartVehicle targetVehicle;

    /**
     * @var target vehicle damage factor
     */
    private double targetVehicleDamageFactor;

    /**
     * @var target army
     */
    private Army targetArmy;

    /**
     * @var is have target enemy around last state
     */
    private boolean isHaveTargetEnemyAroundLastState = false;

    /**
     * @var is have enemy around last state
     */
    private boolean isHaveEnemyAroundLastState = false;
    /**
     *
     * @param groupId
     * @param battleField
     * @param terrainField
     * @param aerialField
     */

    public ArmyAlly(Integer groupId, BattleField battleField, PPField terrainField, PPField aerialField) {
        super();
        this.groupId = groupId;
        track = new Track();
        targetEnemyMap = new HashMap<>();
        this.battleField = battleField;
        this.constTerrainPPField = terrainField;
        this.constAerialPPField = aerialField;
        lastCompactTick = 0;
    }

    public Track getTrack() {
        return track;
    }

    public Integer getGroupId () {
        return groupId;
    }

    public TargetPoint searchNearestEnemy() {
        TargetPoint target = new TargetPoint();
        try {
            PPFieldEnemy damageField = getDamageField();

            if (targetVehicle != null &&
                    targetVehicle.getDurability() > 0 &&
                    damageField.getFactorOld(damageField.getTransformedPoint(targetVehicle.getPoint())) <= targetVehicleDamageFactor &&
                    isAerial()
                    ) {
                target.vector = targetVehicle.getPoint().subtract(getForm().getAvgPoint());
                target.maxDamageValue = targetVehicleDamageFactor;
                target.targetArmy = targetArmy;
                return target;
            }
            target = new TargetPoint();


            double allyArmyMinFactor = getEdgeValues(damageField).get(0).value;

            MyStrategy.battleField.defineArmies();
            List<Army> enemyArmies = MyStrategy.battleField.getEnemyArmies();

            double minDistance = Double.MAX_VALUE;
            Point2D minDistancePoint = null;
            double minDistanceFactor = 0;
            Army minDistanceArmy = null;

            double minFactor = Double.MAX_VALUE;
            Point2D minFactorPoint = null;
            Army minFactorArmy = null;
            for (Army enemyArmy : enemyArmies) {
                for (VehicleType enemyVehicleType : enemyArmy.getVehiclesType()) {
                    if (SmartVehicle.isTargetVehicleType(getVehiclesType().iterator().next(), enemyVehicleType)){

                        for (SmartVehicle enemyEdgeVehicle : enemyArmy.getForm().getEdgesVehicles().values()) {
                            if (SmartVehicle.isTargetVehicleType(getVehiclesType().iterator().next(), enemyEdgeVehicle.getType())) {
                                double edgeVehicleDamageFactor = damageField.getFactorOld(damageField.getTransformedPoint(enemyEdgeVehicle.getPoint()));
                                if (allyArmyMinFactor + edgeVehicleDamageFactor < 0) {
                                    if (edgeVehicleDamageFactor < minFactor) {
                                        minFactor = edgeVehicleDamageFactor;
                                        minFactorPoint = enemyEdgeVehicle.getPoint();
                                        minFactorArmy = enemyArmy;
                                        targetVehicle = enemyEdgeVehicle;
                                        targetArmy = enemyArmy;
                                        targetVehicleDamageFactor = edgeVehicleDamageFactor;
                                    }

                                    double distance = enemyEdgeVehicle.getPoint().subtract(getForm().getAvgPoint()).magnitude();

                                    if (minDistance > distance) {
                                        minDistance = distance;
                                        minDistancePoint = enemyEdgeVehicle.getPoint();
                                        minDistanceFactor = edgeVehicleDamageFactor;
                                        minDistanceArmy = enemyArmy;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isAerial() && minFactorPoint != null) {
                target.vector = minFactorPoint.subtract(getForm().getAvgPoint());
                target.maxDamageValue = minFactor;
                target.targetArmy = minFactorArmy;
            } else if (minDistancePoint != null) {
                target.vector = minDistancePoint.subtract(getForm().getAvgPoint());
                target.maxDamageValue = minDistanceFactor;
                target.targetArmy = minDistanceArmy;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return target;
    }

    public BattleField getBattleField() {
        return battleField;
    }


    public Point2D dangerPoint() throws Exception {
        return MyStrategy.enemyField.onDanger(getVehiclesType(), getForm().getAvgPoint(), CustomParams.dangerRadious);
    }

    public void setEnemy(SmartVehicle enemyVehicle) {

        for (VehicleType allyType : getVehiclesType()) {
            if (SmartVehicle.isTargetVehicleType(allyType, enemyVehicle.getType())) {
                if (enemyVehicle.getDurability() == 0) {
                    targetEnemyMap.remove(enemyVehicle.getId());
                } else {
                    targetEnemyMap.put(enemyVehicle.getId(), enemyVehicle);
                }
            }
        }
    }

    public boolean canAttackedByArmy(Army army) {
        for (VehicleType enemyVehicleType : army.getVehiclesType()) {
            for (VehicleType allyVehicleType : getVehiclesType()) {
                if (SmartVehicle.isTargetVehicleType(enemyVehicleType, allyVehicleType)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isTargetArmy (Army army) {

        for (VehicleType enemyVehicleType : army.getVehiclesType()) {
            for (VehicleType allyVehicleType : getVehiclesType()) {
                if (SmartVehicle.isTargetVehicleType(allyVehicleType, enemyVehicleType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSafetyAround() {
        MyStrategy.battleField.defineArmies();
        List<Army> enemyArmies = MyStrategy.battleField.getEnemyArmies();

        PPFieldEnemy damageField = getDamageField();

        Point2D armyCenter = getForm().getEdgesVehiclesCenter();

        boolean isHaveEnemyAround = false;

        double minDistance = Double.MAX_VALUE;
        double minDistanceDamageFactorDelta = 0;

        for (Army enemyArmy : enemyArmies) {
            if (enemyArmy.getForm().getEdgesVehiclesCenter().subtract(armyCenter).magnitude() <= CustomParams.safetyDistance) {
                if (canAttackedByArmy(enemyArmy)) {
                    isHaveEnemyAround = true;
                    Collection<SmartVehicle> enemyVehiclesCollection = enemyArmy.getForm().getEdgesVehicles().values();

                    for (SmartVehicle allyVehicle : getForm().getEdgesVehicles().values()) {
                        for (SmartVehicle enemyVehicle : enemyVehiclesCollection) {
                            double distance = enemyVehicle.getPoint().subtract(allyVehicle.getPoint()).magnitude();
                            if (distance < minDistance) {
                                minDistance = distance;
                                double allyDamageFactor = damageField.getFactorOld(damageField.getTransformedPoint(allyVehicle.getPoint()));
                                double enemyDamageFactor = damageField.getFactorOld(damageField.getTransformedPoint(enemyVehicle.getPoint()));
                                minDistanceDamageFactorDelta = allyDamageFactor + enemyDamageFactor;
                            }
                        }
                    }
                }
            }
        }

        if (!isHaveEnemyAround) {
            return true;
        }

        return minDistanceDamageFactorDelta <= 0;
    }

    public boolean isHaveEnemy () {
        return targetEnemyMap.size() > 0;
    }

    public boolean isHaveTargetArmyAround(double safetyDistance) {
        if (MyStrategy.world.getTickIndex() % 10 == 0) {
            isHaveTargetEnemyAroundLastState = false;
            int safetyDist = (int)Math.ceil(safetyDistance * (MyStrategy.battleField.getWidth() / MyStrategy.world.getWidth()));
            for (SmartVehicle vehicle : getForm().getEdgesVehicles().values()) {
                if (vehicle.getDurability() > 0) {
                    Point2D transformedPoint = MyStrategy.battleField.pointTransform(vehicle.getPoint());
                    if (battleField.searchTargetEnemiesAround(safetyDist, transformedPoint, getVehiclesType()) != null) {
                        isHaveTargetEnemyAroundLastState = true;
                        break;
                    }
                }
            }
        }

        return isHaveTargetEnemyAroundLastState;
    }


    public boolean isHaveEnemyAround(double safetyDistance) {
        if (MyStrategy.world.getTickIndex() % 10 == 0) {
            isHaveEnemyAroundLastState = false;
            int safetyDist = (int)Math.ceil(safetyDistance * (MyStrategy.battleField.getWidth() / MyStrategy.world.getWidth()));
            for (SmartVehicle vehicle : getForm().getEdgesVehicles().values()) {
                if (vehicle.getDurability() > 0) {
                    Point2D transformedPoint = MyStrategy.battleField.pointTransform(vehicle.getPoint());
                    if (battleField.searchEnemiesAround(safetyDist, transformedPoint, getVehiclesType()) != null) {
                        isHaveEnemyAroundLastState = true;
                        break;
                    }
                }
            }
        }

        return isHaveEnemyAroundLastState;
    }

    public void setLastCompactTick(Integer tick) {
        lastCompactTick = tick;
    }

    public boolean isNeedToCompact() {
        if (MyStrategy.world.getTickIndex() - lastCompactTick < CustomParams.armyCompactTimeout) {
            return false;
        }

        int vehicleCount = getVehicleCount();
        Map<Point2D, SmartVehicle> edgesVehicles = getForm().getEdgesVehicles();

        double maxDistance = 0;
        for (int i = 0; i < CustomParams.borderPointsCount / 2; i++) {
            Point2D borderPoint = MyStrategy.getBorderPointList().get(i);
            Point2D oppositePoint = MyStrategy.getBorderPointList().get(i  + CustomParams.borderPointsCount / 2);

            SmartVehicle vehicle = edgesVehicles.get(borderPoint);
            SmartVehicle oppositeVehicle = edgesVehicles.get(oppositePoint);

            double distance = vehicle.getPoint().distance(oppositeVehicle.getPoint());
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }

        return maxDistance / (double)vehicleCount >= CustomParams.maxSizeVehicleInArmy;
    }

    public boolean isNeedToTurnArmyToEnemy () {

        if (targetVehicle == null || targetVehicle.getDurability() == 0) {
            return false;
        }

        if (targetVehicle.getPoint().distance(getForm().getEdgesVehiclesCenter()) > CustomParams.safetyDistance) {
            return false;
        }

        double angle = getMaxDamageVehicleTurnedAngle(targetVehicle.getPoint());
        if (Math.abs(angle) > Math.PI / 18) {
            return true;
        }

        return false;
    }

    public SmartVehicle getTargetVehicle() {
        return targetVehicle;
    }
}
