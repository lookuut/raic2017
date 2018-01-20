
public class Point2D {

    public static double defaultEps = 1.0;
    public static final Point2D ZERO = new Point2D(0.0, 0.0);

    private double x;
    private double y;

    public int getIntX() { return (int)Math.round(x); }
    public int getIntY() { return (int)Math.round(y); }

    public final double getX() { return x; }
    public final double getY() {
        return y;
    }

    public void setX(double x) {this.x = x;}
    public void setY(double y) {this.y = y;}

    public void set(Point2D point) {
        this.x = point.x;
        this.y = point.y;
    }

    private int hash = 0;

    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(double x1, double y1) {
        double a = getX() - x1;
        double b = getY() - y1;
        return Math.sqrt(a * a + b * b);
    }

    public double distance(Point2D point) {
        return distance(point.getX(), point.getY());
    }

    public Point2D add(double x, double y) {
        return new Point2D(
                getX() + x,
                getY() + y);
    }

    public Point2D add(Point2D point) {
        return add(point.getX(), point.getY());
    }

    public Point2D subtract(double x, double y) {
        return new Point2D(
                getX() - x,
                getY() - y);
    }

    public Point2D multiply(double factor) {
        return new Point2D(getX() * factor, getY() * factor);
    }

    public Point2D subtract(Point2D point) {
        return subtract(point.getX(), point.getY());
    }

    public Point2D normalize() {
        final double mag = magnitude();

        if (mag == 0.0) {
            return new Point2D(0.0, 0.0);
        }

        return new Point2D(
                getX() / mag,
                getY() / mag);
    }

    public Point2D midpoint(double x, double y) {
        return new Point2D(
                x + (getX() - x) / 2.0,
                y + (getY() - y) / 2.0);
    }

    public Point2D midpoint(Point2D point) {
        return midpoint(point.getX(), point.getY());
    }

    public double angle(double x, double y) {
        final double ax = getX();
        final double ay = getY();

        final double delta = (ax * x + ay * y) / Math.sqrt(
                (ax * ax + ay * ay) * (x * x + y * y));

        if (delta > 1.0) {
            return 0.0;
        }
        if (delta < -1.0) {
            return 180.0;
        }

        return Math.toDegrees(Math.acos(delta));
    }

    public double angle(Point2D point) {
        return angle(point.getX(), point.getY());
    }

    public double angle(Point2D p1, Point2D p2) {
        final double x = getX();
        final double y = getY();

        final double ax = p1.getX() - x;
        final double ay = p1.getY() - y;
        final double bx = p2.getX() - x;
        final double by = p2.getY() - y;

        final double delta = (ax * bx + ay * by) / Math.sqrt(
                (ax * ax + ay * ay) * (bx * bx + by * by));

        if (delta > 1.0) {
            return 0.0;
        }
        if (delta < -1.0) {
            return 180.0;
        }

        return Math.toDegrees(Math.acos(delta));
    }

    public double angleRadiance(Point2D point) {
        return angleRadiance(point.getX(), point.getY());
    }

    public double angleRadiance(double x, double y) {
        final double ax = getX();
        final double ay = getY();

        final double delta = (ax * x + ay * y) / Math.sqrt(
                (ax * ax + ay * ay) * (x * x + y * y));

        if (delta > 1.0) {
            return 0.0;
        }
        if (delta < -1.0) {
            return 180.0;
        }

        return (Math.acos(delta));
    }

    public double magnitude() {
        final double x = getX();
        final double y = getY();

        return Math.sqrt(x * x + y * y);
    }

    public double dotProduct(double x, double y) {
        return getX() * x + getY() * y;
    }

    public double dotProduct(Point2D vector) {
        return dotProduct(vector.getX(), vector.getY());
    }

    public double cross(double x, double y) {
        return getX() * y - getY() * x;
    }

    public double cross(Point2D vector) {
        return cross(vector.getX(), vector.getY());
    }

    @Override public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Point2D) {
            Point2D other = (Point2D) obj;
            return equals(other, Point2D.defaultEps);
        } else return false;
    }

    public boolean equals(Point2D point, double eps) {
        return Math.abs(point.getX() - getX()) < eps && Math.abs(point.getY() - getY()) < eps;
    }

    public Point2D turn(double angle) {
        return new Point2D(Math.cos(angle) * getX() - Math.sin(angle) * getY(), Math.sin(angle) * getX() + Math.cos(angle) * getY());
    }


    @Override public int hashCode() {
        if (hash == 0) {
            long bits = 7L;
            bits = 31L * bits + Double.doubleToLongBits(getX());
            bits = 31L * bits + Double.doubleToLongBits(getY());
            hash = (int) (bits ^ (bits >> 32));
        }
        return hash;
    }
    @Override public String toString() {
        return "Point2D [x = " + getX() + ", y = " + getY() + "]";
    }
    public Point2D clone() {
        return new Point2D(getX(), getY());
    }

    public static Point2D lineIntersect(Point2D fLineStart, Point2D fLineEnd, Point2D sLineStart, Point2D sLineEnd) {
        Point2D dir1 = fLineEnd.subtract(fLineStart);
        Point2D dir2 = sLineEnd.subtract(sLineStart);

        //считаем уравнения прямых проходящих через отрезки
        double a1 = -dir1.getY();
        double b1 = +dir1.getX();
        double d1 = -(a1 * fLineStart.getX() + b1 * fLineStart.getY());

        double a2 = -dir2.getY();
        double b2 = +dir2.getX();
        double d2 = -(a2 * sLineStart.getX() + b2 * sLineStart.getY());

        //подставляем концы отрезков, для выяснения в каких полуплоскотях они
        double seg1_line2_start = a2 * fLineStart.getX() + b2 * fLineStart.getY() + d2;
        double seg1_line2_end = a2 * fLineEnd.getX() + b2 * fLineEnd.getY() + d2;

        double seg2_line1_start = a1*sLineStart.getX() + b1*sLineStart.getY() + d1;
        double seg2_line1_end = a1*sLineEnd.getX() + b1*sLineEnd.getY() + d1;

        //если концы одного отрезка имеют один знак, значит он в одной полуплоскости и пересечения нет.
        if (seg1_line2_start * seg1_line2_end > 0 || seg2_line1_start * seg2_line1_end > 0)
            return null;

        double u = seg1_line2_start / (seg1_line2_start - seg1_line2_end);
        return fLineStart.add( dir1.multiply(u));
    }

    public void multiplySelf(double factor)
    {
        setX(getX() * factor);
        setY(getY() * factor);
    }
}
