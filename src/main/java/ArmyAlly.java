import model.Vehicle;
import model.VehicleType;

import java.util.*;

public class ArmyAlly extends Army {

    private Map<Long, SmartVehicle> targetEnemyMap;
    protected Integer groupId;
    /**
     * battle fields
     */
    protected BattleField battleField;

    /**
     *  PPField
     */
    protected PPField staticAerialPPField;
    protected PPField staticTerrainPPField;

    private Track track;
    private Integer lastCompactTick;

    public ArmyAlly(Integer groupId, BattleField battleField, PPField terrainField, PPField aerialField) {
        super();
        this.groupId = groupId;
        track = new Track();
        targetEnemyMap = new HashMap<>();
        this.battleField = battleField;
        this.staticTerrainPPField = terrainField;
        this.staticAerialPPField = aerialField;
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
            target = new TargetPoint();

            PPFieldEnemy damageField = getDamageField();
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
                            double edgeVehicleDamageFactor = damageField.getFactor(damageField.getTransformedPoint(enemyEdgeVehicle.getPoint()));
                            if (allyArmyMinFactor + edgeVehicleDamageFactor < 0) {
                                if (edgeVehicleDamageFactor < minFactor) {
                                    minFactor = edgeVehicleDamageFactor;
                                    minFactorPoint = enemyEdgeVehicle.getPoint();
                                    minFactorArmy = enemyArmy;
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

    public boolean haveEnemyWeakness() {
        MyStrategy.battleField.defineArmies();
        List<Army> enemyArmies = MyStrategy.battleField.getEnemyArmies();

        PPFieldEnemy damageField = getDamageField();

        Point2D armyCenter = getForm().getEdgesVehiclesCenter();

        double minEnemyDamageFactor = Double.MAX_VALUE;
        boolean isHaveEnemyAround = false;
        for (Army enemyArmy : enemyArmies) {
            if (enemyArmy.getForm().getEdgesVehiclesCenter().subtract(armyCenter).magnitude() <= CustomParams.safetyDistance) {
                for (SmartVehicle enemyVehicle : enemyArmy.getForm().getEdgesVehicles().values()) {
                    double enemyDamageFactor = damageField.getFactor(damageField.getTransformedPoint(enemyVehicle.getPoint()));
                    if (enemyDamageFactor < minEnemyDamageFactor) {
                        minEnemyDamageFactor = enemyDamageFactor;
                        isHaveEnemyAround = true;
                    }
                }
            }
        }

        if (!isHaveEnemyAround) {
            return true;
        }

        double maxAllyDamageFactor = Double.MAX_VALUE;
        for (SmartVehicle allyVehicle : getForm().getEdgesVehicles().values()) {
            double allyDamageFactor = damageField.getFactor(damageField.getTransformedPoint(allyVehicle.getPoint()));
            if (maxAllyDamageFactor > allyDamageFactor) {
                maxAllyDamageFactor = allyDamageFactor;
            }
        }

        return maxAllyDamageFactor + minEnemyDamageFactor < 0;
    }

    public boolean isHaveEnemy () {
        return targetEnemyMap.size() > 0;
    }

    private boolean isHaveEnemyAroundLastState;
    public boolean isHaveEnemyAround (double safetyDistance) {
        if (MyStrategy.world.getTickIndex() % 10 == 0) {
            isHaveEnemyAroundLastState = false;
            int safetyDist = (int)Math.ceil(safetyDistance * (MyStrategy.battleField.getWidth() / MyStrategy.world.getWidth()));
            for (SmartVehicle vehicle : getForm().getEdgesVehicles().values()) {
                if (vehicle.getDurability() > 0) {
                    Point2D transformedPoint = MyStrategy.battleField.pointTransform(vehicle.getPoint());
                    if (battleField.searchEnemiesInRaious(safetyDist, transformedPoint, getVehiclesType()) != null) {
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
}
