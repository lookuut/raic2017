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

    LineSegment(int startX, int startY, int endX, int endY) {

        minx = Math.min(startX, endX);
        miny = Math.min(startY, endY);

        maxx = Math.max(startX, endX);
        maxy = Math.max(startY, endY);;

        this.startX = startX;
        this.startY = startY;

        this.endX = endX;
        this.endY = endY;

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
