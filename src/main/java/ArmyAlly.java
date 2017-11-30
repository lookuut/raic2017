import geom.LineSegment;
import geom.Point2D;
import model.VehicleType;

import java.util.*;

public class ArmyAlly extends Army {

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

    public ArmyAlly(Integer groupId) {
        super();
        this.groupId = groupId;
        track = new Track();
    }

    public Track getTrack() {
        return track;
    }

    public Map<Integer, Step> getActualTrackSteps() {
        Set<VehicleType> types = getVehiclesType();

        return track.sumTracks(types, this.getLastModificateTick());
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


    public Point2D searchNearestEnemy() {
        try {
            Set<VehicleType> types = getVehiclesType();

            PPField sum = new PPField(battleField.getPFieldWidth(), battleField.getPFieldHeight());

            for (VehicleType type : types) {
                sum.sumField(MyStrategy.enemyField.getDamageField(type));
            }

            Point2D[] result = sum.getMaxMinValueCell();
            return result[1];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Point2D getNuclearAttackTarget() {
        return MyStrategy.enemyField.nuclearAttackTarget();
    }

    public Point2D getNearestSafetyPointForVehicle(SmartVehicle vehicle, Point2D target) throws Exception {

        EnemyPPField damageField = MyStrategy.enemyField.getDamageField(vehicle.getType());

        LineSegment lineSegment = new LineSegment(
                damageField.getTransformedPoint(vehicle.getPoint()),
                damageField.getTransformedPoint(target)
        );

        Point2D point = damageField.getNearestSafetyPoint(vehicle.getPoint(), lineSegment);

        if (point == null) {
            point = damageField.getTransformedPoint(target);
        }

        return MyStrategy.enemyField.getNearestEnemyToVehicleInCell(vehicle, point);
    }

    public Point2D[] getNearestEnemyPointAndSafetyPoint(int safetyDistance) {
        return MyStrategy.enemyField.getNearestEnemyPointAndSafetyPoint(getForm().getAvgPoint(), safetyDistance);
    }

    public BattleField getBattleField() {
        return battleField;
    }
}
