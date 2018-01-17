
import java.util.ArrayList;
import java.util.List;

public class PPFieldEnemy extends PPField {

    public PPFieldEnemy(int x, int y) {
        super(x, y);
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
                    if (maxFactor < getFactorOld(x, y)) {
                        maxFactor = getFactorOld(x, y);
                        maxFactorPoint = new Point2D(x,y);
                    }

                    if (minFactor > getFactorOld(x, y)) {
                        minFactor = getFactorOld(x, y);
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

