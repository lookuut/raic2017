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

            Army targetArmy = MyStrategy.battleField.getTargetArmy(this);

            if (targetArmy == null) {//no army to kill, just scan map
                return null;
            }
            targetArmy.getForm().recalc(targetArmy.getVehicles());
            target.vector = targetArmy.getForm().getAvgPoint().subtract(getForm().getAvgPoint());

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

    public void setLastCompactTick(Integer tick) {
        lastCompactTick = tick;
    }

    public boolean isNeedToCompact() {
        if (MyStrategy.world.getTickIndex() - lastCompactTick < CustomParams.armyCompactTimeout) {
            return false;
        }

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

    private Integer recalcPPFieldTick = -1;
    private PPFieldEnemy damageField;

    public PPFieldEnemy getDamageField() {
        if (recalcPPFieldTick == MyStrategy.world.getTickIndex()) {
            return damageField;
        }
        damageField = MyStrategy.enemyField.getDamageField(getVehiclesType());
        return damageField;
    }
}
