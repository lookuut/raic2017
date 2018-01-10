import model.VehicleType;

import java.util.*;

public class Army {

    private Map<Long, SmartVehicle> vehicles;
    private int durabilitySum;
    private int durabilitySumRecalcTick;

    private Map<VehicleType, List<SmartVehicle>> vehiclesByType;

    private ArmyForm form;
    private Integer lastModificateTick = -1;
    private HashMap<VehicleType, Integer> vehicleTypes;
    private double maxVisionRange;

    protected HashMap<Integer, BattleFieldCell> battleFieldCellMap;

    public Army() {
        battleFieldCellMap = new HashMap<>();
        vehicles = new HashMap<>();
        form = new ArmyForm();
        vehicleTypes = new HashMap<>();
        vehiclesByType = new HashMap<>();
        durabilitySum = 0;
        durabilitySumRecalcTick = -1;
    }


    public Map<Long, SmartVehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle (SmartVehicle vehicle) {
        //set last modificated of army tick
        setLastModificateTick(MyStrategy.world.getTickIndex());
        //put vehicle to update army form
        putVehicle(vehicle);

        Integer count = 1;
        //update vehicles types
        if (vehicleTypes.containsKey(vehicle.getType())) {
            count += vehicleTypes.get(vehicle.getType());
        }

        if (!vehiclesByType.containsKey(vehicle.getType())) {
            vehiclesByType.put(vehicle.getType(), new ArrayList<>());
        }

        vehiclesByType.get(vehicle.getType()).add(vehicle);
        vehicleTypes.put(vehicle.getType(), count);
        getForm().updateEdgesVehicles(vehicle);

        //update max vision range of army
        maxVisionRange = Math.max(maxVisionRange, vehicle.getMinVisionRange());
    }

    public int getDurabilitySum () {
        if (durabilitySumRecalcTick == MyStrategy.world.getTickIndex()) {
            return durabilitySum;
        }

        durabilitySum = getVehicles().values().stream().mapToInt(SmartVehicle::getDurability).sum();
        return durabilitySum;
    }

    public double getAvgArmyDurabiluty() {
        return getDurabilitySum() / getVehicleCount();
    }

    public void putVehicle(SmartVehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
        getForm().addPoint(vehicle.getPoint());
        getForm().updateEdgesVehicles(vehicle);
        maxVisionRange = Math.max(maxVisionRange, vehicle.getMinVisionRange());
    }

    public void removeVehicle(SmartVehicle vehicle) {
        vehicles.remove(vehicle.getId());
        vehicleTypes.put(vehicle.getType(), vehicleTypes.get(vehicle.getType()) - 1);
        getForm().removeVehicle(vehicles, vehicle);
    }

    public boolean containVehicle(Long vehicleId) {
        return vehicles.containsKey(vehicleId);
    }

    public Set<VehicleType> getVehiclesType () {
        return vehicleTypes.keySet();
    }

    public int getVehicleCount() {
        return vehicles.size();
        //return vehicles.entrySet().stream().filter(entry -> entry.getValue().getDurability() > 0).count();
    }

    public ArmyForm getForm() {
        return form;
    }

    public boolean isMoving() {
        for (SmartVehicle vehicle : getForm().getEdgesVehicles().values()) {
            if (vehicle.isMoving()) {
                return true;
            }
        }
        return false;
    }

    public boolean isAlive() {
        return getVehicleCount() > 0;
    }

    public SmartVehicle getNearestVehicle(Point2D point) {
        Point2D[] points = new Point2D[1];
        points[0] = point;
        SmartVehicle[] vehicles = getNearestVehicle(points);
        return vehicles[0];
    }

    public SmartVehicle getGunnerVehicle(Point2D target) {
        double maxDistance = 0.f;
        SmartVehicle gunnerVehicle = null;

        for (SmartVehicle vehicle : getVehicles().values()) {
            if (vehicle.getDurability() > 0) {
                double distance = vehicle.distanceToPoint(target.getX(), target.getY());

                if (distance <= vehicle.getActualVisionRange() && distance > maxDistance) {
                    gunnerVehicle = vehicle;
                    maxDistance = distance;
                }
            }
        }

        double visionRange = gunnerVehicle.getActualVisionRange();
        return gunnerVehicle;
    }

    public SmartVehicle[] getNearestVehicle(Point2D[] points) {

        Double[] minLenght = new Double[points.length];
        Arrays.fill(minLenght, Double.MAX_VALUE);//boolshit get first element length

        SmartVehicle[] vehicles = new SmartVehicle[points.length];
        getVehicles().entrySet().stream().filter(vehicle -> vehicle.getValue().getDurability() > 0).forEach(
            item -> {
                for (int i = 0; i < points.length; i++) {
                    Point2D point = points[i];
                    if (point.subtract(item.getValue().getPoint()).magnitude() < minLenght[i]) {
                        minLenght[i] = point.subtract(item.getValue().getPoint()).magnitude();
                        vehicles[i] = item.getValue();
                    }
                }
            }
        );

        return vehicles;
    }

    public void setLastModificateTick(Integer tick) {
        lastModificateTick = tick;
    }

    public Integer getLastModificateTick() {
        return lastModificateTick;
    }


    public boolean timeToGoHeal() {
        Integer durabilitySum = getVehicles().values().stream().
                filter(vehicle -> vehicle.getDurability() > 0).map(SmartVehicle::getDurability).reduce(0, Integer::sum);
        return durabilitySum / (double)(getVehicleCount() * 100) < CustomParams.percentOfHeatedVehicles;
    }

    public boolean isAerial () {
        return getVehiclesType().contains(VehicleType.HELICOPTER) || getVehiclesType().contains(VehicleType.FIGHTER);
    }

    public boolean isTerrain() {
        return !isAerial();
    }

    public double getMinSpeed() {
        double minSpeed = 10;
        for (VehicleType type : getVehiclesType()) {
            double factor = 1;
            if (SmartVehicle.isTerrain(type)) {
                factor = MyStrategy.game.getSwampTerrainSpeedFactor();
            }

            if (vehiclesByType.get(type).get(0).getMaxSpeed() * factor < minSpeed) {
                minSpeed = vehiclesByType.get(type).get(0).getMaxSpeed() * factor;
            }
        }

        return minSpeed;
    }

    public double getSpeed() {
        double speed = 0.0;
        for (VehicleType type : getVehiclesType()) {

            if (speed < vehiclesByType.get(type).get(0).getMaxSpeed()) {
                speed = vehiclesByType.get(type).get(0).getMaxSpeed();
            }
        }

        return speed;
    }

    public List<PPFieldPoint> getEdgeValues(PPFieldEnemy damageField) {

        double minFactor = Double.POSITIVE_INFINITY;
        double maxFactor = Double.NEGATIVE_INFINITY;

        Point2D maxFactorPoint = null;
        Point2D minFactorPoint = null;

        Collection<SmartVehicle> edgesVehicles = getForm().getEdgesVehicles().values();
        Set<Point2D> visitedCells = new HashSet<>();

        Point2D centerPoint = getForm().getEdgesVehiclesCenter();

        for (SmartVehicle vehicle : edgesVehicles) {
            Point2D transformPoint = damageField.getTransformedPoint(vehicle.getPoint());
            for (int y = -1; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    Point2D point = new Point2D(transformPoint.getIntX() + x, transformPoint.getIntY() + y);

                    if (point.getIntX() >= 0 && point.getIntX() < damageField.getWidth() &&
                            point.getIntY() >= 0 && point.getIntY() < damageField.getHeight() &&
                            !visitedCells.contains(point)) {

                        if (maxFactor < damageField.getFactor(point)) {
                            maxFactor = damageField.getFactor(point);
                            maxFactorPoint = point;
                        }

                        if (minFactor > damageField.getFactor(point)) {
                            minFactor = damageField.getFactor(point);
                            minFactorPoint = point;
                        }
                    }
                }
            }
        }

        Point2D transformPoint = damageField.getTransformedPoint(centerPoint);

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                Point2D point = new Point2D(transformPoint.getIntX() + x, transformPoint.getIntY() + y);

                if (point.getIntX() >= 0 && point.getIntX() < damageField.getWidth() &&
                        point.getIntY() >= 0 && point.getIntY() < damageField.getHeight() &&
                        !visitedCells.contains(point)) {

                    if (maxFactor < damageField.getFactor(point)) {
                        maxFactor = damageField.getFactor(point);
                        maxFactorPoint = point;
                    }

                    if (minFactor > damageField.getFactor(point)) {
                        minFactor = damageField.getFactor(point);
                        minFactorPoint = point;
                    }
                }
            }
        }

        List<PPFieldPoint> result = new ArrayList<>();

        result.add(new PPFieldPoint(minFactorPoint, minFactor));
        result.add(new PPFieldPoint(maxFactorPoint, maxFactor));

        return result;
    }

    public Map<VehicleType, List<SmartVehicle>> getVehiclesByType () {
        return this.vehiclesByType;
    }
}
