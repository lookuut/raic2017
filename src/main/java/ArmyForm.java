
import java.util.HashMap;
import java.util.Map;

public class ArmyForm {

    /**
     * @desc edges vehicles by angles
     */
    private Map<Point2D, SmartVehicle> edgesVehicles;

    private Point2D maxPoint;
    private Point2D minPoint;
    private Point2D avgPoint;
    private Integer recalculationIndex = -1;
    public ArmyForm() {
        maxPoint = new Point2D(0.0,0.0);
        minPoint = new Point2D(CustomParams.fieldMaxWidth, CustomParams.fieldMinHeight);
        edgesVehicles = new HashMap<>();
    }


    public void addPoint(Point2D point) {
        maxPoint.setX(Math.max(point.getX(), maxPoint.getX()));
        maxPoint.setY(Math.max(point.getY(), maxPoint.getY()));

        minPoint.setX(Math.min(point.getX(), minPoint.getX()));
        minPoint.setY(Math.min(point.getY(), minPoint.getY()));
    }

    public void recalc(Map<Long,SmartVehicle> vehicles) {
        if (recalculationIndex == MyStrategy.world.getTickIndex()) {
            return;
        }
        Integer count = 0;
        Point2D sumVector = new Point2D(0.0, 0.0);

        minPoint = new Point2D(Double.MAX_VALUE, Double.MAX_VALUE);
        maxPoint = new Point2D(0.0, 0.0);
        edgesVehicles.clear();
        for (Map.Entry<Long, SmartVehicle> entry : vehicles.entrySet()) {
            if (entry.getValue().getDurability() > 0) {

                maxPoint.setX(Math.max(maxPoint.getX(), entry.getValue().getX()));
                maxPoint.setY(Math.max(maxPoint.getY(), entry.getValue().getY()));

                minPoint.setX(Math.min(minPoint.getX(), entry.getValue().getX()));
                minPoint.setY(Math.min(minPoint.getY(), entry.getValue().getY()));

                sumVector.setX(entry.getValue().getPoint().getX() + sumVector.getX());
                sumVector.setY(entry.getValue().getPoint().getY() + sumVector.getY());
                updateEdgesVehicles(entry.getValue());
                count++;
            }
        }

        if (count > 0)  {
            avgPoint = sumVector.multiply((double)1/count);
        }

        recalculationIndex = MyStrategy.world.getTickIndex();
    }


    public Point2D getAvgPoint() { return avgPoint; }
    public Point2D getMaxPoint() { return maxPoint; }
    public Point2D getMinPoint() { return minPoint; }

    public Point2D getArmySize () {
        return getMaxPoint().subtract(getMinPoint());
    }


    public Point2D[] getEdgePoints(Point2D directionPoint) {
        Point2D[] edges = new Point2D[3];
        if ((directionPoint.getX() >= avgPoint.getX() && directionPoint.getY() > avgPoint.getY())
                ||
                (directionPoint.getX() <= avgPoint.getX() && directionPoint.getY() < avgPoint.getY())) {
            edges[0] = new Point2D(minPoint.getX(), maxPoint.getY());
            edges[1] = new Point2D(maxPoint.getX(), minPoint.getY());
        } else {
            edges[0] = maxPoint.clone();
            edges[1] = minPoint.clone();
        }

        if (directionPoint.getX() >= avgPoint.getX() && directionPoint.getY() >= avgPoint.getY()) {
            edges[2] = maxPoint.clone();
        } else if (directionPoint.getX() < avgPoint.getX() && directionPoint.getY() < avgPoint.getY()) {
            edges[2] = minPoint.clone();
        } else if (directionPoint.getX() < avgPoint.getX() && directionPoint.getY() >= avgPoint.getY()) {
            edges[2] = new Point2D(minPoint.getX(), maxPoint.getY());
        } else if (directionPoint.getX() >= avgPoint.getX() && directionPoint.getY() < avgPoint.getY()) {
            edges[2] = new Point2D(maxPoint.getX(), minPoint.getY());
        }

        return edges;
    }

    public void updateEdgesVehicles(SmartVehicle vehicle) {
        for (Point2D edgePoint : MyStrategy.getBorderPointList()) {
            if (!edgesVehicles.containsKey(edgePoint) || vehicle.getPoint().distance(edgePoint) < edgesVehicles.get(edgePoint).getPoint().distance(edgePoint)) {
                edgesVehicles.put(edgePoint, vehicle);
            }
        }
    }

    public void removeVehicle(Map<Long, SmartVehicle> vehicles, SmartVehicle vehicle) {
        if (edgesVehicles.values().contains(vehicle)) {
            edgesVehicles.values().remove(vehicle);
            vehicles.values().stream().filter(_vehicle -> _vehicle.getDurability() > 0).forEach(_vehicle -> {
                this.updateEdgesVehicles(_vehicle);
            });
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
}
