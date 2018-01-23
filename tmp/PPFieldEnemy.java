
import java.util.ArrayList;
import java.util.List;

public class PPFieldEnemy extends PPField {

    public PPFieldEnemy(int x, int y) {
        super(x, y);
    }

    public List<PPFieldPoint> getEdgesValueInRadious(Point2D center, double radious) {
        Point2D transformedPoint = getTransformedPoint(center);

        int r = (int)Math.ceil(radious * getWidth() / MyStrategy.world.getWidth());
        double minFactor = Double.POSITIVE_INFINITY;
        double maxFactor = Double.NEGATIVE_INFINITY;

        Point2D maxFactorPoint = null;
        Point2D minFactorPoint = null;

        for (int y = transformedPoint.getIntY() - r; y <= (transformedPoint.getIntY() + r) && y < getHeight(); y++) {
            if (y < 0) {
                continue;
            }
            for (int x = transformedPoint.getIntX() - r; x <= (transformedPoint.getIntX() + r) && x < getWidth(); x++) {
                if (x >= 0) {
                    if (maxFactor < getTileFactor(x, y)) {
                        maxFactor = getTileFactor(x, y);
                        maxFactorPoint = new Point2D(x,y);
                    }

                    if (minFactor > getTileFactor(x, y)) {
                        minFactor = getTileFactor(x, y);
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

