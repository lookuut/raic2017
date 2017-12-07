
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
                maxPoint = new Point2D(Math.max(maxPoint.getX(), entry.getValue().getX()) , Math.max(maxPoint.getY(), entry.getValue().getY()));
                minPoint = new Point2D(Math.min(minPoint.getX(), entry.getValue().getX()) , Math.min(minPoint.getY(), entry.getValue().getY()));
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
            if (vehicle.getPoint().distance(point) <= distance) {
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
            double dist = vehicle.getPoint().distance(fromPoint);
            if (dist > maxDist) {
                maxDistancePoint = vehicle.getPoint();
                maxDist = dist;
            }
        }

        return maxDistancePoint.subtract(fromPoint);
    }
}
