package strategy;

import java.util.*;

public class ArmyForm {

    /**
     * @desc edges vehicles by angles
     */
    private Map<Point2D, SmartVehicle> edgesVehicles;
    private Collection<Point2D> edgesPoints;

    private Point2D maxPoint;
    private Point2D minPoint;
    private Point2D avgPoint;
    private Integer recalculationIndex = -1;

    public ArmyForm() {
        maxPoint = new Point2D(0.0,0.0);
        minPoint = new Point2D(CustomParams.fieldMaxWidth, CustomParams.fieldMinHeight);
        edgesVehicles = new HashMap<>();
        edgesPoints = new ArrayList<>();
    }


    public void addPoint(Point2D point) {
        maxPoint.setX(Math.max(point.getX(), maxPoint.getX()));
        maxPoint.setY(Math.max(point.getY(), maxPoint.getY()));

        minPoint.setX(Math.min(point.getX(), minPoint.getX()));
        minPoint.setY(Math.min(point.getY(), minPoint.getY()));
    }

    private void updateEdgesPoints() {
        edgesPoints.clear();

        Point2D direction = new Point2D(0, maxPoint.subtract(minPoint).multiply(0.5).magnitude());

        for (int i = 0; i < CustomParams.borderPointsCount; i++) {
            double angle = i * 2 * Math.PI / CustomParams.borderPointsCount;
            Point2D edgePoint = direction.turn(angle);
            edgesPoints.add(getAvgPoint().add(edgePoint));
        }
    }

    public void update(Map<Long, SmartVehicle> vehicles) {

        if (recalculationIndex == MyStrategy.world.getTickIndex()) {
            return;
        }

        Point2D sumVector = new Point2D(0.0, 0.0);

        minPoint = new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
        maxPoint = new Point2D(0.0, 0.0);

        for (Map.Entry<Long, SmartVehicle> entry : vehicles.entrySet()) {
            maxPoint.setX(Math.max(maxPoint.getX(), entry.getValue().getX()));
            maxPoint.setY(Math.max(maxPoint.getY(), entry.getValue().getY()));

            minPoint.setX(Math.min(minPoint.getX(), entry.getValue().getX()));
            minPoint.setY(Math.min(minPoint.getY(), entry.getValue().getY()));

            sumVector.setX(entry.getValue().getPoint().getX() + sumVector.getX());
            sumVector.setY(entry.getValue().getPoint().getY() + sumVector.getY());
        }

        avgPoint = sumVector.multiply((double)1/ vehicles.size());
        updateEdgesPoints();
        updateEdgesVehicles(vehicles);

        recalculationIndex = MyStrategy.world.getTickIndex();
    }


    public Point2D getAvgPoint() { return avgPoint; }

    private void updateEdgesVehicles(Map<Long, SmartVehicle> vehiclesMap) {
        edgesVehicles.clear();

        Collection<SmartVehicle> vehicles = vehiclesMap.values();

        for (Point2D edgePoint : edgesPoints) {
            Iterator<SmartVehicle> iterator = vehicles.iterator();
            SmartVehicle minDistanceVehicle = iterator.next();
            double minDistance = edgePoint.subtract(minDistanceVehicle.getPoint()).magnitude();
            while (iterator.hasNext()) {
                SmartVehicle vehicle = iterator.next();
                double distance = edgePoint.subtract(vehicle.getPoint()).magnitude();
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceVehicle = vehicle;
                }
            }

            edgesVehicles.put(edgePoint, minDistanceVehicle);
        }
    }

    public void removeVehicle(Map<Long, SmartVehicle> vehicles, SmartVehicle vehicle) {
        if (edgesVehicles.values().contains(vehicle)) {
            edgesVehicles.values().remove(vehicle);
        }
    }


    public boolean isPointInDistance(Point2D point, double distance) {

        for (SmartVehicle vehicle : edgesVehicles.values()) {
            if (vehicle.getDurability() > 0 && vehicle.getPoint().distance(point) <= distance) {
                return true;
            }
        }
        return false;
    }

    public boolean isPointInNuclearAttackRadious(Point2D point) {
        for (SmartVehicle vehicle : edgesVehicles.values()) {
            if (vehicle.getDurability() > 0 && vehicle.getPoint().distance(point) <= vehicle.getActualVisionRange() + MyStrategy.game.getTacticalNuclearStrikeRadius()) {
                return true;
            }
        }
        return false;
    }

    public boolean isPointInVisionRange(Point2D point) {
        for (SmartVehicle vehicle : edgesVehicles.values()) {
            if (vehicle.getDurability() > 0 && vehicle.getPoint().distance(point) <= vehicle.getActualVisionRange()) {
                return true;
            }
        }
        return false;
    }

    public Map<Point2D, SmartVehicle> getEdgesVehicles () {
        return edgesVehicles;
    }

    public double getMinDamageFactor(Army army) {
        PPFieldEnemy damageField = army.getDamageField();

        double minFactor = Double.NEGATIVE_INFINITY;
        for (SmartVehicle vehicle : getEdgesVehicles().values()) {
            double factor = damageField.getFactorOld(damageField.getTransformedPoint(vehicle.getPoint()));
            if (minFactor < factor) {
                minFactor = factor;
            }
        }

        return minFactor;
    }

    public Point2D getMaxDistanceVec(Point2D fromPoint) {
        Point2D maxDistancePoint = null;
        double maxDist = 0;
        for (SmartVehicle vehicle : edgesVehicles.values()) {
            if (vehicle.getDurability() > 0) {
                double dist = vehicle.getPoint().distance(fromPoint);
                if (dist > maxDist) {
                    maxDistancePoint = vehicle.getPoint();
                    maxDist = dist;
                }
            }
        }

        return maxDistancePoint.subtract(fromPoint);
    }

    public boolean isDamagedByNuclearAttack(Point2D nuclearAttackTarget) {
        for (SmartVehicle vehicle : edgesVehicles.values()) {
            if (vehicle.getDurability() > 0) {
                if (vehicle.getPoint().subtract(nuclearAttackTarget).magnitude() <= MyStrategy.game.getTacticalNuclearStrikeRadius()) {
                    return true;
                }
            }
        }

        return false;
    }

    public Point2D getEdgesVehiclesCenter() {
        int edgesVehicleCount = (int)edgesVehicles.values().stream().filter(vehicle -> vehicle.getDurability() > 0).count();
        Point2D avgPoint = new Point2D(0,0);

        getEdgesVehicles().values().stream().
                filter(vehicle -> vehicle.getDurability() > 0).
                forEach(vehicle -> {
                    avgPoint.setX(vehicle.getPoint().getX() + avgPoint.getX());
                    avgPoint.setY(vehicle.getPoint().getY() + avgPoint.getY());
                });

        avgPoint.setX(avgPoint.getX() / edgesVehicleCount);
        avgPoint.setY(avgPoint.getY() / edgesVehicleCount);

        return avgPoint;
    }

    public List<Point2D> getNearestEdgesPoints(ArmyForm enemyArmyForm) {

        Point2D minDistanceAllyPoint = null;
        Point2D minDistanceEnemyPoint = null;
        double minDistance = Double.MAX_VALUE;
        for (SmartVehicle vehicle : getEdgesVehicles().values()) {
            for (SmartVehicle enemyVehicle : enemyArmyForm.getEdgesVehicles().values()) {
                double distance = vehicle.getPoint().subtract(enemyVehicle.getPoint()).magnitude();
                if (distance < minDistance) {
                    minDistanceAllyPoint = vehicle.getPoint();
                    minDistanceEnemyPoint = enemyVehicle.getPoint();
                }
            }
        }

        List<Point2D> result = new ArrayList<>();
        result.add(minDistanceAllyPoint);
        result.add(minDistanceEnemyPoint);
        return result;
    }

    public SmartVehicle getNearestEdgeVehicle(Point2D point) {
        Iterator<SmartVehicle> iterator = getEdgesVehicles().values().iterator();
        SmartVehicle minDistanceVehicle = iterator.next();
        double minDistance = minDistanceVehicle.getPoint().distance(point);

        while (iterator.hasNext()) {
            SmartVehicle vehicle = iterator.next();
            double distance = vehicle.getPoint().distance(point);
            if (minDistance > distance) {
                minDistanceVehicle = vehicle;
                minDistance = distance;
            }
        }

        return minDistanceVehicle;
    }

    public Point2D getMaxPoint() {
        return maxPoint;
    }

    public Point2D getMinPoint() {
        return minPoint;
    }
}
