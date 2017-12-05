import model.VehicleType;

import java.util.*;
import java.util.List;

/**
 * @TODO programming mirror map save logic
 */
public class PPField {
    private float field[][];
    //dirty init here
    private float minValue;

    private int width;
    private int height;

    public PPField(int width, int height) {
        field = new float[height][width];
        this.width = width;
        this.height = height;
        minValue = Float.MAX_VALUE;
    }

    public void addLinearPPValue(int x, int y, float factor) {

        for (int j = -CustomParams.maxLinearPPRange; j <= CustomParams.maxLinearPPRange; j++) {
            for (int i = -CustomParams.maxLinearPPRange; i <= CustomParams.maxLinearPPRange; i++) {
                if (x + i >= 0 &&
                        x + i < getWidth() &&
                        y + j >= 0 &&
                        y + j < getHeight() &&
                        i * i + j * j <= CustomParams.maxLinearPPRange * CustomParams.maxLinearPPRange
                        )
                {
                    float divide = (float)(1 + Math.abs(j) + Math.abs(i));
                    double value = factor > 0 ? Math.ceil(factor / divide) : Math.floor(factor / divide);
                    addFactor(x + i, y + j, (float)value);
                }
            }
        }
    }

    public void setFactor(int x, int y, float factor) {
        if (minValue > factor) {
            minValue = factor;
        }

        this.field[y][x] = factor;
    }

    public void addFactor(Point2D point, float factor) {
        addFactor(point.getIntX(), point.getIntY(), factor);
    }
    public void addFactor (int x, int y, float factor) {
        this.field[y][x] += factor;

        if (minValue > this.field[y][x]) {
            minValue = this.field[y][x];
        }
    }
    public float getFactor (int x, int y) {
        return field[y][x];
    }

    public void sumField(PPField localField) {
        operateField(localField, 1);
    }

    public static PPField sumField(PPField field1, PPField field2) {
        PPField sum = new PPField(field1.getWidth(), field1.getHeight());

        for (int y = 0; y < sum.getHeight(); y++) {
            for (int x = 0; x < sum.getWidth(); x++) {
                if (field1.getFactor(x, y) > 0 || field2.getFactor(x, y) > 0) {
                    sum.addFactor(x, y, field1.getFactor(x, y) + field2.getFactor(x, y));
                }
            }
        }
        return sum;
    }
    public void minusField(PPField localField) {
        operateField(localField, -1);
    }

    public void operateField(PPField localField, int operate) {
        int xFactor = (getWidth() / localField.getWidth());
        int yFactor = (getHeight() / localField.getHeight());

        for (int y = 0; y < getHeight(); y++) {
            int transformedY = y / yFactor;
            for (int x = 0; x < getWidth(); x++) {
                int transformedX = x / xFactor;
                addFactor( x, y, operate * localField.getFactor(transformedX, transformedY));
            }
        }
    }


    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public Point2D getTransformedPoint(Point2D point) {
        return point.multiply(getWidth() /MyStrategy.world.getWidth());
    }

    public float recursive(HashSet<Integer> visitedCells, HashSet<Integer> intersectCells, int x, int y, LineSegment line) {
        float factor = 0;
        for (int j = -1; j <= 1 && j + y >= 0 && j + y < getHeight(); j++) {
            for (int i = -1; i <= 1 && x + i >= 0 && x + i < getWidth(); i++) {
                int number = x + i + (y + j) * width;

                if (!visitedCells.contains(number)) {
                    visitedCells.add(number);

                    if (line.isIntersectSquare(x + i, y + j, 1)) {
                        intersectCells.add(number);
                        factor += recursive(visitedCells, intersectCells, x + i, y + j, line) + getFactor(x + i, y + j);
                    }
                }
            }
        }
        return factor;
    }

    public void print() {
        System.out.println(Arrays.deepToString(field).replaceAll("],", "]," + System.getProperty("line.separator")));
        System.out.println("==========================================>");
    }

    public List<Point2D> getMinValueCells() {
        List<Point2D> minValueList = new ArrayList<>();

        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                if (getFactor(i, j) == minValue) {
                    minValueList.add(new Point2D((double)i, (double)j));
                }
            }
        }
        return minValueList;
    }

    public Point2D getMinValueCell(List<Point2D> searchPoints) {

        float minValue = Float.MAX_VALUE;
        Point2D minValuePoint = null;
        for (Point2D point : searchPoints) {
            if (getFactor(point.getIntX(), point.getIntY()) < minValue) {
                minValue = getFactor(point.getIntX(), point.getIntY());
                minValuePoint = point;
            }
        }

        return minValuePoint;
    }

    public Point2D getMinValueCell() {
        Point2D maxPoint = null;
        Point2D minPoint = null;

        float max = field[0][0];
        float min = Float.MAX_VALUE;

        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                if (field[j][i] != 0) {
                    if (field[j][i] > max) {
                        max = field[j][i];
                        maxPoint = new Point2D(i, j);
                    }

                    if (field[j][i] < min) {
                        min = field[j][i];
                        minPoint = new Point2D(i, j);
                    }
                }
            }
        }

        return minPoint;
    }

    public Point2D getWorldPoint(Point2D point) {
        return point != null ? point.multiply(MyStrategy.world.getWidth()/(float)getWidth()) : null;
    }

    public Point2D deepSearch (HashSet<Integer> visitedCells, Point2D point, LineSegment line) {
        int x = point.getIntX();
        int y = point.getIntY();
        for (int j = -1; j <= 1 && j + y >= 0 && j + y < getHeight(); j++) {
            for (int i = -1; i <= 1 && x + i >= 0 && x + i < getWidth(); i++) {
                int number = x + i + (y + j) * getWidth();

                if (!visitedCells.contains(number)) {
                    visitedCells.add(number);

                    if (line.isIntersectSquare(x + i, y + j, 1)) {
                        if (getFactor(x + i, y + j) == 0) {
                            return deepSearch(visitedCells, new Point2D(x + i, y + j), line);
                        } else {
                            return new Point2D(x + i, y + j);
                        }
                    }
                }
            }
        }

        return null;
    }

    public void addSteps(Map<Integer, Step> steps) {
        steps.entrySet().forEach(mapEntry -> {
            addFactor(mapEntry.getValue().getPoint(), mapEntry.getValue().getPower());
        });
    }

    public Point2D searchPath(ArmyAlly army, Point2D targetPoint, SortedMap<Integer, Map<Integer, Step>> trackMap, TargetPoint target) throws Exception {
        Point2D startPoint = army.getForm().getAvgPoint();

        Point2D pathVector = targetPoint.subtract(startPoint);

        if (pathVector.magnitude() > CustomParams.pathSegmentLenght) {
            pathVector = pathVector.multiply(CustomParams.pathSegmentLenght / pathVector.magnitude());
        }
        Integer minPathJourneyTick = 0;
        double minPathFactor = Double.MAX_VALUE;
        Point2D minPathVector = null;

        for (int angleSector = 0; angleSector < CustomParams.pathFinderSectorCount; angleSector++) {
            double angle = (angleSector % 2 == 1 ? -1 : 1) * angleSector * (2 * Math.PI) / CustomParams.pathFinderSectorCount;
            Point2D turnedPathVector = pathVector.turn(angle);
            //@TODO optimize it!!!

            double pathSegmentLenght = turnedPathVector.magnitude() / CustomParams.calculationPathInterval;

            double factor = 0.0;
            Integer pathJourneyTick = 0;
            Point2D[] edgesPoints = army.getForm().getEdgePoints(startPoint.add(turnedPathVector));
            SmartVehicle vehicles[] = army.getNearestVehicle(edgesPoints);

            for (double pathSegment = pathSegmentLenght; pathSegment <= turnedPathVector.magnitude(); pathSegment += pathSegmentLenght ) {

                Point2D pathSegmentVector = turnedPathVector.multiply(pathSegment / turnedPathVector.magnitude());
                for (int i = 0; i < vehicles.length; i++) {
                    Point2D vehiclePoint = vehicles[i].getPoint();
                    Integer tick = vehicles[i].getVehiclePointAtTick(pathSegmentVector);
                    if (tick > pathJourneyTick) {
                        pathJourneyTick = tick;
                    }

                    tick += MyStrategy.world.getTickIndex();
                    Point2D transformedPoint = getTransformedPoint(vehiclePoint.add(pathSegmentVector));

                    if (trackMap.containsKey(tick)) {
                        Map<Integer, Step> tickTrackMap = trackMap.get(tick);
                        int trackMapIndex = transformedPoint.getIntX() + transformedPoint.getIntY() * getWidth();
                        if (tickTrackMap.containsKey(trackMapIndex)) {
                            factor += tickTrackMap.get(trackMapIndex).getPower();
                        }
                    }
                }
            }

            for (int i = 0; i < vehicles.length; i++) {
                Point2D vehiclePoint = vehicles[i].getPoint();
                factor += getPathFactor(vehiclePoint, vehiclePoint.add(turnedPathVector));
            }

            if (minPathFactor > factor && factor < CustomParams.minPathFactor) {
                minPathFactor = factor;
                minPathVector = turnedPathVector;
                minPathJourneyTick = pathJourneyTick;
            }

            if (factor < target.maxDamageValue) {//@TODO workaround boolshit
                minPathFactor = factor;
                minPathVector = turnedPathVector;
                minPathJourneyTick = pathJourneyTick;
                break;
            }
        }

        if (minPathVector == null) {//do nothing
            return startPoint;
        }

        addPathTrack(army, minPathVector, minPathJourneyTick);
        return minPathVector.add(startPoint);
    }

    public void addPathTrack (ArmyAlly army, Point2D pathVector, Integer pathTime) {

        double lenght = pathVector.magnitude();
        double pathInterval = lenght / (double)pathTime;

        SortedMap<Integer, Map<Integer, Step>> terrainMap = army.getTrack().getVehicleTypeTrack(VehicleType.TANK);
        SortedMap<Integer, Map<Integer, Step>> aerialMap = army.getTrack().getVehicleTypeTrack(VehicleType.FIGHTER);
        Map<Integer, Step> terrainTrack = terrainMap.get(army.getTrack().getLastTerrainTick());
        Map<Integer, Step> aerialTrack = aerialMap.get(army.getTrack().getLastAerialTick());

        int proposeX = (int)MyStrategy.world.getWidth() / getWidth();
        int proposeY = (int)MyStrategy.world.getHeight() / getHeight();

        Integer tick = MyStrategy.world.getTickIndex() + 1;

        for (double path = pathInterval; path < lenght; path += pathInterval) {
            Point2D intervalVec = pathVector.multiply(path / lenght);

            if (terrainTrack != null && terrainTrack.size() > 0) {

                for (Step step : terrainTrack.values()) {
                    Point2D stepPoint = step.getPoint();
                    stepPoint = stepPoint.multiply(proposeX);
                    Point2D cellAvgPoint = stepPoint.add(new Point2D(proposeX / 2, proposeY / 2));
                    Point2D sumPoint = cellAvgPoint.add(intervalVec);

                    sumPoint = sumPoint.multiply(1 / (double)proposeX);

                    int x = (int)sumPoint.getX();
                    int y = (int)sumPoint.getY();
                    if (x >= getWidth() || y >= getHeight() || x < 0 || y < 0) {
                        break;
                    }
                    army.getTrack().addStep(tick , new Step(new Point2D((int)sumPoint.getX(), (int)sumPoint.getY()) , step.getPower()), VehicleType.TANK);
                }
            }

            if (aerialTrack != null && aerialTrack.size() > 0) {

                for (Step step : aerialTrack.values()) {
                    Point2D stepPoint = step.getPoint();
                    stepPoint = stepPoint.multiply(proposeX);
                    Point2D cellAvgPoint = stepPoint.add(new Point2D(proposeX / 2, proposeY / 2));
                    Point2D sumPoint = cellAvgPoint.add(intervalVec);

                    sumPoint = sumPoint.multiply(1 / (double)proposeX);

                    int x = (int)sumPoint.getX();
                    int y = (int)sumPoint.getY();
                    if (x >= getWidth() || y >= getHeight() || x < 0 || y < 0) {
                        break;
                    }
                    army.getTrack().addStep(tick , new Step(new Point2D((int)sumPoint.getX(), (int)sumPoint.getY()) , step.getPower()), VehicleType.FIGHTER);
                }
            }
            tick += 1;
        }
    }

    public Double getPathFactor(Point2D startPoint, Point2D endPoint) throws Exception {
        int propose = (int)MyStrategy.world.getWidth() / getWidth();

        Point2D direction = endPoint.subtract(startPoint);
        Point2D voxelStartPoint = startPoint.multiply(1/(double)propose);

        Point2D voxelEndPoint = endPoint.multiply(1/(double)propose);

        Integer tStartX = (int)Math.floor(voxelStartPoint.getX()) * propose;
        Integer tStartY = (int)Math.floor(voxelStartPoint.getY()) * propose;

        Integer stepX = propose * (direction.getX() < 0  ? -1 : 1);
        Integer stepY = propose * (direction.getY() < 0  ? -1 : 1);

        double maxWidth = MyStrategy.world.getWidth();
        double maxHeight = MyStrategy.world.getHeight();
        double factorSum = getFactor((int)Math.floor(tStartX / propose), (int)Math.floor(tStartY / propose));
        Integer intersectCellsCount = 1;

        while (
                (
                        Math.floor(tStartX / propose) < Math.floor(voxelEndPoint.getX())
                                ||
                        Math.floor(tStartY / propose) < Math.floor(voxelEndPoint.getY())
                ) &&
                    tStartX + stepX < maxWidth
                    &&
                    tStartX + stepX >= 0
                    &&
                    tStartY + stepY < maxHeight
                    &&
                    tStartY + stepY >= 0
                ) {

            Point2D horIntersectPoint = Point2D.lineIntersect(startPoint, endPoint, new Point2D(0, tStartY + stepY) , new Point2D(maxWidth, tStartY + stepY ));
            Point2D verIntersectPoint = Point2D.lineIntersect(startPoint, endPoint, new Point2D(tStartX + stepX, 0) , new Point2D(tStartX + stepX, maxHeight));

            if (horIntersectPoint == null && verIntersectPoint == null) {
                throw new Exception("Cant intersect lines found");
            }

            if (horIntersectPoint == null || (verIntersectPoint != null && startPoint.distance(verIntersectPoint) < startPoint.distance(horIntersectPoint))) {
                tStartX += stepX;
            } else {
                tStartY += stepY;
            }

            int x = (int)Math.floor(tStartX / propose);
            int y = (int)Math.floor(tStartY / propose);
            factorSum += getFactor(x, y);
            intersectCellsCount++;
        }

        return factorSum / intersectCellsCount;
    }


    public Float sumXAxis (Point2D point, int yCentre, int y, int radius, List<Integer> visitedCells) {
        float factor = 0;
        try {

            for (int x = 0; x <= radius; x++) {
                if (y * y + x * x <= radius) {
                    if (point.getIntX() + x < getWidth() && getFactor(point.getIntX() + x, yCentre + y ) > 0) {
                        factor += getFactor(point.getIntX() + x, yCentre + y );
                        visitedCells.add(point.getIntX() + x  + (yCentre + y) * getWidth() );
                    }

                    if (x != 0 && point.getIntX() - x >= 0 && getFactor(point.getIntX() - x, yCentre + y ) > 0) {
                        factor += getFactor(point.getIntX() - x, yCentre + y );
                        visitedCells.add(point.getIntX() - x  + (yCentre + y) * getWidth());
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return factor;
    }

    public float sumFactorInPointRadious(Point2D point, int radius, List<Integer> visitedCells) {
        float factorSum = 0;
        for (int y = 0; y <= radius; y++) {
            if (point.getIntY() + y < getHeight()) {
                factorSum += sumXAxis(point, point.getIntY(), y , radius, visitedCells);
            }

            if (point.getIntY() - y >= 0) {
                factorSum += sumXAxis(point, point.getIntY(), -y , radius, visitedCells);
            }
        }

        return factorSum;
    }

    public void setAvg(int x, int y, int count) {
        setFactor(x, y, getFactor(x, y) / (float)count);
    }
}
