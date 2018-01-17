
import model.Facility;
import model.FacilityType;
import model.VehicleType;

import java.util.HashSet;
import java.util.Set;

public class SmartFacility {
    private long id;
    private FacilityType type;
    private long ownerPlayerId;
    private double left;
    private double top;
    private double capturePoints;
    private VehicleType vehicleType;
    private int productionProgress;

    private Set<ArmyAllyOrdering> goingToFacilityArmies;

    public SmartFacility(Facility facility) {
        this.id = facility.getId();
        this.type = facility.getType();
        this.ownerPlayerId = facility.getOwnerPlayerId();
        this.left = facility.getLeft();
        this.top = facility.getTop();
        this.capturePoints = facility.getCapturePoints();
        this.vehicleType = facility.getVehicleType();
        this.productionProgress = facility.getProductionProgress();
        goingToFacilityArmies = new HashSet<>();
    }

    public long getId() {
        return id;
    }

    public FacilityType getType() {
        return type;
    }

    public long getOwnerPlayerId() {
        return ownerPlayerId;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getCapturePoints() {
        return capturePoints;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public int getProductionProgress() {
        return productionProgress;
    }

    public void update(Facility facility) {
        this.id = facility.getId();
        this.type = facility.getType();
        this.ownerPlayerId = facility.getOwnerPlayerId();
        this.left = facility.getLeft();
        this.top = facility.getTop();
        this.capturePoints = facility.getCapturePoints();
        this.vehicleType = facility.getVehicleType();
        this.productionProgress = facility.getProductionProgress();
    }

    public Set<ArmyAllyOrdering> getGoingToFacilityArmies () {
        return goingToFacilityArmies;
    }

    public void addGoingToFacilityArmy(ArmyAllyOrdering army) {
        goingToFacilityArmies.add(army);
    }

    public Point2D getFacilityCentre() {
        double left = getLeft();
        double top = getTop();

        double width = MyStrategy.game.getFacilityWidth();
        double height = MyStrategy.game.getFacilityHeight();

        return new Point2D(left + width / 2, top + height / 2);
    }

    public boolean isPointInFacility(Point2D point) {
        if (getLeft() <= point.getX() &&
                point.getX() <= getLeft() + MyStrategy.game.getFacilityWidth() &&
                getTop() <= point.getY() &&
                point.getY() <= getTop() + MyStrategy.game.getFacilityHeight()) {
            return true;
        }
        return false;
    }

    public void clearGoingToFacilityArmies() {
        goingToFacilityArmies.clear();
    }
}
