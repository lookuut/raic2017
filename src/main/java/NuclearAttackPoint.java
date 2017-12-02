public class NuclearAttackPoint implements Comparable<NuclearAttackPoint> {
    private Integer pos;
    private Point2D point;
    private float weight;

    public NuclearAttackPoint(Point2D point, float weight, Integer maxWidth) {
        this.point = point;
        this.weight = weight;
        pos = point.getIntY() * maxWidth + point.getIntX();
    }

    public int compareTo(NuclearAttackPoint attackPoint) {
        return Float.compare(attackPoint.weight, this.weight);
    }

    public Point2D getPoint() {
        return point;
    }

    public int hashCode() {
        return pos.hashCode();
    }
}
