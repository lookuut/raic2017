import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PPFieldEnemy extends PPField {

    public PPFieldEnemy(int x, int y) {
        super(x, y);
    }


    public Point2D getNearestSafetyPoint(Point2D targetPoint, LineSegment lineSegment) throws Exception {
        HashSet<Integer> visitedCells = new HashSet<>();
        return deepSearch(visitedCells, getTransformedPoint(targetPoint), lineSegment);
    }

    public Point2D[] getNearestEnemyPointAndSafetyPoint(Point2D point, float safetyDistance) {
        //@TODO bad style
        int intSafetyDistance = (int)Math.ceil(safetyDistance * getWidth() / MyStrategy.world.getWidth());

        float minEnemyDist = Float.MAX_VALUE;
        Point2D nearestEnemyPoint = new Point2D(0,0);

        float minSafetyDist = Float.MAX_VALUE;
        Point2D nearestSafetyPoint = new Point2D(0,0);

        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getHeight(); i++) {

                if (getFactor(i, j) > 0) {

                    Point2D vector = point.subtract(getWorldPoint(new Point2D(i, j)));

                    if (vector.magnitude() < minEnemyDist) {
                        minEnemyDist = (float)vector.magnitude();
                        nearestEnemyPoint = getWorldPoint(new Point2D(i,j));
                    }
                }

                if (getFactor(i, j) == 0) {
                    int startII = Math.max(0, i - intSafetyDistance);
                    int startJJ = Math.max(0, j - intSafetyDistance);

                    int endII = Math.min(getWidth(), i + intSafetyDistance);
                    int endJJ = Math.min(getHeight(), j + intSafetyDistance);

                    boolean goodShape = true;
                    for (int jj = startJJ; jj < endJJ && goodShape; jj++) {
                        for (int ii = startII; ii <  endII; ii++) {
                            if (getFactor(jj, ii) > 0) {
                                goodShape = false;
                                break;
                            }
                        }
                    }

                    if (goodShape) {
                        Point2D vector = point.subtract(getWorldPoint(new Point2D(i,j)));
                        if (vector.magnitude() < minSafetyDist) {
                            minSafetyDist = (float)vector.magnitude();
                            nearestSafetyPoint = getWorldPoint(new Point2D(i,j));
                        }
                    }

                }
            }
        }

        Point2D[] result = {nearestEnemyPoint, nearestSafetyPoint};
        return result;
    }


    public Point2D nuclearAttackTarget() {
        int maxX = 0;
        int maxY = 0;
        double maxValue = 0;
        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                double localMaxValue = 0;
                int localMaxX = 0;
                int localMaxY = 0;
                double localMax = 0;

                for (int jj = -2; jj <= 2 && jj + j >= 0 && jj + j < getHeight(); jj++){
                    for (int ii = -2; ii <= 2 && ii + i >= 0 && ii + i < getWidth(); ii++) {
                        if (getFactor(ii + i, jj + j) > localMax) {
                            localMax = getFactor(ii + i, jj + j);
                            localMaxX = ii + i;
                            localMaxY = jj + j;
                        }
                        localMaxValue += getFactor(ii + i, jj + j);
                    }
                }

                if (maxValue < localMaxValue) {
                    maxValue = localMaxValue;
                    maxX = localMaxX;
                    maxY = localMaxY;
                }
            }
        }

        return getWorldPoint(new Point2D(maxX, maxY));
    }

    /**
     *
     * @param point
     * @param radious
     * @return avg factor sum
     */
    public double getPointRadiousFactorSum(Point2D point, int radious) {
        double factor = 0;
        int count = 0;
        for (int y = point.getIntY() - radious; y <= (point.getIntY() + radious) && y < getHeight(); y++) {
            if (y < 0) {
                continue;
            }
            for (int x = point.getIntX() - radious; x <= (point.getIntX() + radious) && x < getWidth(); x++) {
                if (x >= 0) {
                    factor += getFactor(x, y);
                    count++;
                }
            }
        }

        return factor/ (double)count;
    }

    public List<PPFieldPoint> getEdgesValueInRadious(Point2D center, int radious) {

        double minFactor = Double.POSITIVE_INFINITY;
        double maxFactor = Double.NEGATIVE_INFINITY;

        Point2D maxFactorPoint = null;
        Point2D minFactorPoint = null;

        for (int y = center.getIntY() - radious; y <= (center.getIntY() + radious) && y < getHeight(); y++) {
            if (y < 0) {
                continue;
            }
            for (int x = center.getIntX() - radious; x <= (center.getIntX() + radious) && x < getWidth(); x++) {
                if (x >= 0) {
                    if (maxFactor < getFactor(x, y)) {
                        maxFactor = getFactor(x, y);
                        maxFactorPoint = new Point2D(x,y);
                    }

                    if (minFactor > getFactor(x, y)) {
                        minFactor = getFactor(x, y);
                        minFactorPoint = new Point2D(x,y);
                    }
                }
            }
        }

        List<PPFieldPoint> result = new ArrayList<>();

        result.add(new PPFieldPoint(minFactorPoint, minFactor));
        result.add(new PPFieldPoint(maxFactorPoint, maxFactor));

        return result;
    }
}

