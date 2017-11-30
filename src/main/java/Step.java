import geom.Point2D;

public class Step {

    private Short x;
    private Short y;
    private Integer power;

    public Step(Point2D point, Integer power) {
        x = (short)point.getX();
        y = (short)point.getY();
        this.power = power;
    }

    public Integer getIndex () {
        return (int)(y * (Math.ceil(MyStrategy.game.getWorldWidth() / CustomParams.tileCellSize)) + x);
    }

    public void addPower(Integer power) {
        this.power += power;
    }

    public Integer getPower() {
        return power;
    }

    public Point2D getPoint() {
        return new Point2D(this.x, this.y);
    }
}
