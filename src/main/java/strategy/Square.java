package strategy;

public class Square {

    private Point2D leftBottomAngle;

    private Point2D rightTopAngle;


    public Square(Point2D leftBottomAngle, Point2D rightTopAngle) {
        this.leftBottomAngle = leftBottomAngle;
        this.rightTopAngle = rightTopAngle;
    }

    public void addPoint(Point2D point) {
        if (point.getX() < leftBottomAngle.getX()) {
            leftBottomAngle.setX(point.getX());
        }
        if (point.getY() < leftBottomAngle.getY()) {
            leftBottomAngle.setY(point.getY());
        }

        if (point.getX() > rightTopAngle.getX()) {
            rightTopAngle.setX(point.getX());
        }
        if (point.getY() > rightTopAngle.getY()) {
            rightTopAngle.setY(point.getY());
        }
    }

    public Point2D getLeftBottomAngle() {
        return leftBottomAngle;
    }

    public Point2D getRightTopAngle() {
        return rightTopAngle;
    }
}
