package geom;

public class LineSegment {
    protected float a;
    protected float c;

    protected int startX;
    protected int startY;

    protected int endX;
    protected int endY;

    protected int minx;
    protected int maxx;

    protected int miny;
    protected int maxy;

    public LineSegment(Point2D start, Point2D end) {

        minx = Math.min(start.getIntX(), end.getIntX());
        miny = Math.min(start.getIntY(), end.getIntY());

        maxx = Math.max(start.getIntX(), end.getIntX());
        maxy = Math.max(start.getIntY(), end.getIntY());;

        this.startX = start.getIntX();
        this.startY = start.getIntY();

        this.endX = end.getIntX();
        this.endY = end.getIntY();

        if (endX == startX) {//except situation
            a = Float.POSITIVE_INFINITY;
        } else {
            a = ((endY - startY) / (float)(endX - startX));
            c = startY - startX * ((endY - startY) / (float)(endX - startX));
        }

    }

    public boolean isIntersectSquare(int x, int y, int l) {
        if (x < minx || x > maxx || y < miny || y > maxy) {
            return false;
        }

        if (a == Float.POSITIVE_INFINITY) {
            if (x == minx && y >= miny && y <= maxy) {
                return true;
            } else {
                return false;
            }
        }

        if (a == 0.0) {
            if (y == miny && x >= minx && x <= maxx) {
                return true;
            } else {
                return false;
            }
        }

        float casex = ((y - c) / a);
        if ( x <= casex && casex <= x + l) {
            return true;
        }

        casex = (y + l - c) / a;
        if ( x <= casex && casex <= x + l) {
            return true;
        }

        casex = (a * x + c);
        if (casex >= y && casex <= y + l) {
            return true;
        }

        casex = (a * (x + l) + c);
        if (casex >= y && casex <= y + l) {
            return true;
        }

        return false;
    }

    public int getStartX () {
        return startX;
    }

    public int getStartY () {
        return startY;
    }
}
