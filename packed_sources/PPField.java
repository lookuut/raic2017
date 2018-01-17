
import model.VehicleType;

import java.util.*;

/**
 * @TODO programming mirror map save logic
 */
public class PPField {
    private float field[][];

    private float minValue;
    private float maxValue;

    private int width;
    private int height;

    public PPField(int width, int height) {
        field = new float[height][width];
        this.width = width;
        this.height = height;
        minValue = Float.MAX_VALUE;
        maxValue = Float.MIN_VALUE;
    }

    public void rehreshMinValue() {
        minValue = Float.MAX_VALUE;
    }

    public double cellSize () {
        return MyStrategy.world.getWidth() / getWidth();
    }

    public float getMaxValue () {
        return maxValue;
    }

    public float getMinValue () {
        return minValue;
    }

    public void addLinearPPValue(int x, int y, double factor, Set<Point2D> exceptPoints) {

        for (int j = -CustomParams.maxLinearPPRange; j <= CustomParams.maxLinearPPRange; j++) {
            for (int i = -CustomParams.maxLinearPPRange; i <= CustomParams.maxLinearPPRange; i++) {
                if (x + i >= 0 &&
                        x + i < getWidth() &&
                        y + j >= 0 &&
                        y + j < getHeight() &&
                        i * i + j * j <= CustomParams.maxLinearPPRange * CustomParams.maxLinearPPRange &&
                        !exceptPoints.contains(new Point2D(x + i, y + j))
                        )
                {
                    float divide = (float)((1 + Math.abs(j) + Math.abs(i)) * (1 + Math.abs(j) + Math.abs(i)));
                    float value = factor >= 0 ? (float)Math.floor(factor / divide) : (float)Math.ceil(factor / divide);
                    addFactor(x + i, y + j, value);
                }
            }
        }
    }

    public void addAerialPPValue(int x, int y, double factor, Set<Point2D> exceptPoints) {

        for (int j = -CustomParams.maxLinearPPRange; j <= CustomParams.maxLinearPPRange; j++) {
            for (int i = -CustomParams.maxLinearPPRange; i <= CustomParams.maxLinearPPRange; i++) {
                if (x + i >= 0 &&
                        x + i < getWidth() &&
                        y + j >= 0 &&
                        y + j < getHeight() &&
                        i * i + j * j <= CustomParams.maxLinearPPRange * CustomParams.maxLinearPPRange &&
                        !exceptPoints.contains(new Point2D(x + i, y + j))
                        )
                {
                    float divide = (float)((1 + Math.abs(j) + Math.abs(i)));
                    float value = factor >= 0 ? (float)Math.floor(factor / divide) : (float)Math.ceil(factor / divide);
                    addFactor(x + i, y + j, value);
                }
            }
        }
    }

    public void addExponentionalFactor(int x, int y, double factor, Set<Point2D> exceptPoints) {
        for (int j = -CustomParams.maxLinearPPRange; j <= CustomParams.maxLinearPPRange; j++) {
            for (int i = -CustomParams.maxLinearPPRange; i <= CustomParams.maxLinearPPRange; i++) {
                if (x + i >= 0 &&
                        x + i < getWidth() &&
                        y + j >= 0 &&
                        y + j < getHeight() &&
                        i * i + j * j <= CustomParams.maxLinearPPRange * CustomParams.maxLinearPPRange
                        && !exceptPoints.contains(new Point2D(x + i, y + j))
                        )
                {

                    float distance = (float)Math.sqrt(i * i + j * j);

                    double divide = factor / Math.exp(Math.abs(distance));
                    double value = factor > 0 ? Math.ceil(divide * 1000) / 1000 : Math.floor(divide * 1000) / 1000;
                    addFactor(x + i, y + j, (float)value);
                }
            }
        }
    }
    public void setFactor(int x, int y, double factor) {
        if (minValue > factor) {
            minValue = (float)factor;
        }

        this.field[y][x] = (float)factor;
    }

    public void addFactor(Point2D point, float factor) {
        addFactor(point.getIntX(), point.getIntY(), factor);
    }
    public void addFactor (int x, int y, float factor) {
        this.field[y][x] += factor;

        if (minValue > this.field[y][x]) {
            minValue = this.field[y][x];
        }
        if (maxValue < this.field[y][x]) {
            maxValue = this.field[y][x];
        }
    }
    public float getFactorOld(int x, int y) {
        return field[y][x];
    }
    public float getFactorOld(Point2D point) {
        return field[(int)Math.floor(point.getY())][(int)Math.floor(point.getX())];
    }

    /**
     *
     * @param worldPoint in world coordinates
     * @return damage factor
     */
    public float getFactor(Point2D worldPoint) {
        Point2D damageFieldPoint = getTransformedPoint(worldPoint);
        return field[(int)Math.floor(damageFieldPoint.getY())][(int)Math.floor(damageFieldPoint.getX())];
    }

    public void sumField(PPField localField) {
        rehreshMinValue();
        operateField(localField, 1);
    }

    public static PPField sumField(PPField field1, PPField field2, int operate) {
        PPField sum = new PPField(field1.getWidth(), field1.getHeight());

        for (int y = 0; y < sum.getHeight(); y++) {
            for (int x = 0; x < sum.getWidth(); x++) {
                if (field1.getFactorOld(x, y) > 0 || field2.getFactorOld(x, y) > 0) {
                    sum.addFactor(x, y, field1.getFactorOld(x, y) + operate * field2.getFactorOld(x, y));
                }
            }
        }
        return sum;
    }

    public void operateField(PPField localField, int operate) {
        int xFactor = (getWidth() / localField.getWidth());
        int yFactor = (getHeight() / localField.getHeight());

        for (int y = 0; y < getHeight(); y++) {
            int transformedY = y / yFactor;
            for (int x = 0; x < getWidth(); x++) {
                int transformedX = x / xFactor;
                addFactor( x, y, operate * localField.getFactorOld(transformedX, transformedY));
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
        return point.multiply(getWidth() / MyStrategy.world.getWidth());
    }

    public int transformLenght (double worldLenght) {
        return (int)Math.ceil(worldLenght * getWidth() / MyStrategy.world.getWidth());
    }

    public void print() {
        //System.out.println(Arrays.deepToString(field).replaceAll("],", "]," + System.getProperty("line.separator")));
        printCuttedFloats(100);
        System.out.println("==========================================>");
    }

    public void printCuttedFloats(int tailSize) {
        for (int y = 0; y < getHeight(); y++) {
            String row = "";
            for (int x = 0; x < getWidth(); x++) {
                row += Math.ceil(field[y][x] * tailSize)/ tailSize + " ";
            }
            System.out.println(row);
        }
    }

    public Point2D getMinValuePoint() {
        Point2D minValuePoint = null;
        double minValue = Double.MAX_VALUE;
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                if (minValue > getFactorOld(x, y)) {
                    minValue = getFactorOld(x, y);
                    minValuePoint = new Point2D(x,y);
                }
            }
        }

        return getWorldPoint(minValuePoint);

    }

    public List<Point2D> getMinValueCells() {
        List<Point2D> minValueList = new ArrayList<>();

        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                if (getFactorOld(i, j) == minValue) {
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
            if (getFactorOld(point.getIntX(), point.getIntY()) < minValue) {
                minValue = getFactorOld(point.getIntX(), point.getIntY());
                minValuePoint = point;
            }
        }

        return minValuePoint;
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
                        if (getFactorOld(x + i, y + j) == 0) {
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
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    Point2D point = mapEntry.getValue().getPoint();
                    if (point.getIntX() + i >= 0 && point.getIntX() + i < getWidth() &&
                            point.getIntY() + j >= 0 && point.getIntY() + j < getHeight()) {
                        addFactor(point.getIntX() + i, point.getIntY() + j, mapEntry.getValue().getPower());
                    }
                }
            }
        });
    }

    public Point2D searchPath(ArmyAlly army, Point2D pathVector, SortedMap<Integer, Map<Integer, Step>> trackMap, TargetPoint target) throws Exception {
        Point2D startPoint = army.getForm().getAvgPoint();

        if (pathVector.magnitude() > CustomParams.pathSegmentLenght) {
            pathVector = pathVector.multiply(2.5 * CustomParams.pathSegmentLenght / pathVector.magnitude());
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
            Map<Point2D, SmartVehicle> vehicles = army.getForm().getEdgesVehicles();
            for (double pathSegment = pathSegmentLenght; pathSegment <= turnedPathVector.magnitude(); pathSegment += pathSegmentLenght ) {

                Point2D pathSegmentVector = turnedPathVector.multiply(pathSegment / turnedPathVector.magnitude());
                for (SmartVehicle vehicle : vehicles.values()) {
                    if (vehicle.getDurability() > 0) {
                        Point2D vehiclePoint = vehicle.getPoint();
                        Integer tick = vehicle.getVehiclePointAtTick(pathSegmentVector);
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
            }

            for (SmartVehicle vehicle : vehicles.values()) {
                if (vehicle.getDurability() > 0) {
                    Point2D vehiclePoint = vehicle.getPoint();
                    factor += getPathFactor(vehiclePoint, vehiclePoint.add(turnedPathVector));
                }
            }

            if (minPathFactor > factor) {
                minPathFactor = factor;
                minPathVector = turnedPathVector;
                minPathJourneyTick = pathJourneyTick;
            }

            if ((target.maxDamageValue != null && factor <= target.maxDamageValue) || factor <= CustomParams.minPathFactor) {
                minPathFactor = factor;
                minPathVector = turnedPathVector;
                minPathJourneyTick = pathJourneyTick;
                break;
            }
        }

        if (minPathVector != null && minPathVector.magnitude() > CustomParams.pathSegmentLenght) {
            minPathVector = minPathVector.normalize().multiply(CustomParams.pathSegmentLenght);
        }
        if (minPathVector == null) {//do nothing
            return startPoint;
        }

        addPathTrack(army, minPathVector, minPathJourneyTick);
        return minPathVector;
    }

    public void addPathTrack (ArmyAlly army, Point2D pathVector, Integer pathTime) {

        double lenght = pathVector.magnitude();
        double pathInterval = lenght / (double)pathTime;

        SortedMap<Integer, Map<Integer, Step>> terrainMap = army.getTrack().getVehicleTypeTrack(VehicleType.TANK);
        SortedMap<Integer, Map<Integer, Step>> aerialMap = army.getTrack().getVehicleTypeTrack(VehicleType.FIGHTER);
        Map<Integer, Step> terrainTrack = terrainMap.get(army.getTrack().getLastTerrainTick());
        Map<Integer, Step> aerialTrack = aerialMap.get(army.getTrack().getLastAerialTick());

        int proposeX = (int) MyStrategy.world.getWidth() / getWidth();
        int proposeY = (int) MyStrategy.world.getHeight() / getHeight();

        Integer tick = MyStrategy.world.getTickIndex() + 1;

        for (double path = pathInterval; path < lenght; path += pathInterval) {
            Point2D intervalVec = pathVector.multiply(path / lenght);

            if (terrainTrack != null) {
                Set<Point2D> alreadyAddedCells = new HashSet<>();
                for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {

                    Point2D stepPoint = vehicle.getPoint();
                    stepPoint = stepPoint.multiply(proposeX);
                    Point2D cellAvgPoint = stepPoint.add(new Point2D(proposeX / 2, proposeY / 2));
                    Point2D sumPoint = cellAvgPoint.add(intervalVec);

                    sumPoint = sumPoint.multiply(1 / (double)proposeX);
                    Point2D addPoint = new Point2D((int)Math.floor(sumPoint.getX()), (int)Math.floor(sumPoint.getY()));

                    if (addPoint.getX() >= getWidth() || addPoint.getY() >= getHeight() || addPoint.getX() < 0 || addPoint.getY() < 0) {
                        break;
                    }
                    if (!alreadyAddedCells.contains(addPoint)) {
                        alreadyAddedCells.add(addPoint);
                        army.getTrack().addStep(tick , new Step(addPoint , CustomParams.allyUnitPPFactor), VehicleType.TANK);
                    }
                }
            }

            if (aerialTrack != null) {
                Set<Point2D> alreadyAddedCells = new HashSet<>();

                for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {
                    Point2D stepPoint = vehicle.getPoint();
                    stepPoint = stepPoint.multiply(proposeX);
                    Point2D cellAvgPoint = stepPoint.add(new Point2D(proposeX / 2, proposeY / 2));
                    Point2D sumPoint = cellAvgPoint.add(intervalVec);

                    sumPoint = sumPoint.multiply(1 / (double)proposeX);

                    Point2D addPoint = new Point2D((int)Math.floor(sumPoint.getX()), (int)Math.floor(sumPoint.getY()));

                    if (addPoint.getX() >= getWidth() || addPoint.getY() >= getHeight() || addPoint.getX() < 0 || addPoint.getY() < 0) {
                        break;
                    }

                    if (!alreadyAddedCells.contains(addPoint)) {
                        alreadyAddedCells.add(addPoint);
                        army.getTrack().addStep(tick , new Step(addPoint , CustomParams.allyUnitPPFactor), VehicleType.FIGHTER);
                    }
                }
            }
            tick += 1;
        }
    }

    public Double getPathFactor(Point2D startPoint, Point2D endPoint) throws Exception {
        int propose = (int) MyStrategy.world.getWidth() / getWidth();

        Point2D direction = endPoint.subtract(startPoint);
        if (direction.getY() < 0) {
            direction = direction.multiply(-1);
            Point2D temp = startPoint.clone();
            startPoint = endPoint.clone();
            endPoint = temp;
        }

        startPoint.setX(Math.max(startPoint.getX(), 0));
        startPoint.setY(Math.max(startPoint.getY(), 0));

        startPoint.setX(Math.min(startPoint.getX(), MyStrategy.world.getWidth() - 1));
        startPoint.setY(Math.min(startPoint.getY(), MyStrategy.world.getHeight() - 1));

        Point2D voxelStartPoint = startPoint.multiply(1/(double)propose);

        Point2D voxelEndPoint = endPoint.multiply(1/(double)propose);

        Integer tStartX = (int)Math.floor(voxelStartPoint.getX()) * propose;
        Integer tStartY = (int)Math.floor(voxelStartPoint.getY()) * propose;

        Integer stepX = propose * (direction.getX() < 0  ? -1 : 1);
        Integer stepY = propose * (direction.getY() < 0  ? -1 : 1);

        double maxWidth = MyStrategy.world.getWidth();
        double maxHeight = MyStrategy.world.getHeight();
        double factorSum = getFactorOld((int)Math.floor(tStartX / propose), (int)Math.floor(tStartY / propose));
        Integer intersectCellsCount = 1;

        while (
                ((
                stepX > 0 && Math.floor((tStartX) / propose) < Math.floor(voxelEndPoint.getX())
                        ||
                stepY > 0 && Math.floor((tStartY) / propose) < Math.floor(voxelEndPoint.getY())
                )
                        ||
                (
                stepX < 0 && Math.floor((tStartX + stepX) / propose) > Math.floor(voxelEndPoint.getX())
                        ||
                stepY < 0 && Math.floor((tStartY + stepY) / propose) > Math.floor(voxelEndPoint.getY())
                ))

                        &&
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
            factorSum += getFactorOld(x, y);
            intersectCellsCount++;
        }

        return factorSum / intersectCellsCount;
    }


    public Float sumXAxis (Point2D point, int yCentre, int y, int radius, List<Integer> visitedCells) {
        float factor = 0;
        try {

            for (int x = 0; x <= radius; x++) {
                if (y * y + x * x <= radius) {
                    if (point.getIntX() + x < getWidth() && getFactorOld(point.getIntX() + x, yCentre + y ) > 0) {
                        factor += getFactorOld(point.getIntX() + x, yCentre + y );
                        visitedCells.add(point.getIntX() + x  + (yCentre + y) * getWidth() );
                    }

                    if (x != 0 && point.getIntX() - x >= 0 && getFactorOld(point.getIntX() - x, yCentre + y ) > 0) {
                        factor += getFactorOld(point.getIntX() - x, yCentre + y );
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
}
