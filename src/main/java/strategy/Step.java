package strategy;

public class Step {

    private Short x;
    private Short y;
    private Float power;

    public Step(Point2D point, Float power) {
        x = (short)point.getX();
        y = (short)point.getY();
        this.power = power;
    }

    public Integer getIndex () {
        return (int)(y * (Math.ceil(MyStrategy.game.getWorldWidth() / CustomParams.tileCellSize)) + x);
    }

    public void addPower(Float power) {
        this.power += power;
    }

    public Float getPower() {
        return power;
    }

    public Point2D getPoint() {
        return new Point2D(this.x, this.y);
    }
}
