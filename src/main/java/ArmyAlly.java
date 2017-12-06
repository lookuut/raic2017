import model.VehicleType;

import java.util.*;

public class ArmyAlly extends Army {

    private Map<Long, SmartVehicle> targetEnemyMap;
    protected Integer groupId;
    private boolean isHaveFacility = false;
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

    public ArmyAlly(Integer groupId) {
        super();
        this.groupId = groupId;
        track = new Track();
        targetEnemyMap = new HashMap<>();
    }

    public Track getTrack() {
        return track;
    }

    public Map<Integer, Step> getActualTrackSteps() {
        Set<VehicleType> types = getVehiclesType();

        return track.sumTracks(types, this.getLastModificateTick());
    }

    public boolean isContainVehicleId(Long vehicleId) {
        return getVehicles().containsKey(vehicleId);
    }

    public Integer getGroupId () {
        return groupId;
    }

    public void setAerialPPField (PPField field) {
        staticAerialPPField = field;
    }

    public void setTerrainPPField (PPField field) {
        staticTerrainPPField = field;
    }

    public void init (PPField terrainField, PPField aerialField) {
        setTerrainPPField(terrainField);
        setAerialPPField(aerialField);
    }


    public TargetPoint searchNearestEnemy() {
        TargetPoint target = null;
        try {
            target = new TargetPoint();
            Set<VehicleType> types = getVehiclesType();

            PPField damageFieldsSum = new PPField(battleField.getWidth(), battleField.getHeight());
            PPField enemyDefenceFieldsAvg = new PPField(battleField.getWidth(), battleField.getHeight());
            SmartVehicle allyAttackVehicle = getForm().getEdgesVehicles().values().stream().findFirst().get();

            if (types.size() == 1) {
                damageFieldsSum = MyStrategy.enemyField.getDamageField(types.iterator().next());
                enemyDefenceFieldsAvg = MyStrategy.enemyField.getEnemyField(allyAttackVehicle);
            } else if (types.size() > 1) {
                damageFieldsSum = new PPField(battleField.getWidth(), battleField.getHeight());
                enemyDefenceFieldsAvg = new PPField(battleField.getWidth(), battleField.getHeight());
                for (VehicleType type : types) {
                    damageFieldsSum.sumField(MyStrategy.enemyField.getDamageField(type));
                    enemyDefenceFieldsAvg.sumField(MyStrategy.enemyField.getEnemyField(allyAttackVehicle));
                }
            }

            List<Point2D> minValueCells = enemyDefenceFieldsAvg.getMinValueCells();
            Point2D minValuePoint = damageFieldsSum.getMinValueCell(minValueCells);

            if (minValuePoint == null) {
                return null;
            }
            target.maxDamageValue = damageFieldsSum.getFactor(minValuePoint.getIntX(), minValuePoint.getIntY()) + 100;
            //target.maxDamageValue = 1000;

            BattleFieldCell cell = battleField.getBattleFieldCell(minValuePoint.getIntX(), minValuePoint.getIntY());
            Point2D enemyCellPoint = damageFieldsSum.getWorldPoint(cell.getPoint());

            if (enemyCellPoint.subtract(getForm().getAvgPoint()).magnitude() >  CustomParams.safetyDistance) {
                target.vector = enemyCellPoint.subtract(getForm().getAvgPoint());
                return target;
            }
            Map<Long,SmartVehicle> enemyVehicles = cell.getVehicles(MyStrategy.world.getOpponentPlayer().getId());

            Map<Point2D, SmartVehicle> allyVehicles = getForm().getEdgesVehicles();
            double minDistance = Double.MAX_VALUE;
            SmartVehicle minDistanceEnemyVehicle = null;
            SmartVehicle minDistanceAllyVehicle = null;
            for (SmartVehicle enemyVehicle : enemyVehicles.values()) {
                for (SmartVehicle allyVehicle : allyVehicles.values()) {
                    double distance = allyVehicle.getPoint().distance(enemyVehicle.getPoint());
                    if (distance < minDistance) {
                        minDistanceEnemyVehicle = enemyVehicle;
                        minDistanceAllyVehicle = allyVehicle;
                        minDistance = distance;
                    }
                }
            }
            if (minDistanceEnemyVehicle == null || minDistanceAllyVehicle == null) {
                throw new Exception("cant find enemy with min distance");
            }



            double attackRange = minDistanceAllyVehicle.getAttackRange(minDistanceEnemyVehicle.isAerial());
            Point2D fromPoint = minDistanceAllyVehicle.getPoint();
            Point2D targetPoint = minDistanceEnemyVehicle.getPoint();

            Point2D targetVector = targetPoint.subtract(fromPoint);
            double targetVectorMagnitude = targetVector.magnitude();

            /*
            if (targetVectorMagnitude > attackRange) {
                targetVector = targetVector.multiply((targetVectorMagnitude - attackRange) / targetVectorMagnitude);
            }*/

            if (targetVectorMagnitude > attackRange && Math.abs(targetVectorMagnitude - attackRange) <= CustomParams.nearestEnemyEps) {
                targetVector.multiply(1.01);
                double epsx = 0.01;
                if (targetVector.getX() < 0) {
                    epsx = -0.01;
                }

                double epsy = 0.01;
                if (targetVector.getY() < 0) {
                    epsy = -0.01;
                }

                targetVector.setX(targetVector.getX() + epsx);
                targetVector.setY(targetVector.getY() + epsy);
            }
            target.vector = targetVector;
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

    public boolean isHaveEnemy () {
        return targetEnemyMap.size() > 0;
    }


    public boolean isNeedToCompact() {
        Long vehicleCount = getVehicleCount();
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
