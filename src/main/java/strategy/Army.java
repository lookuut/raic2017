package strategy;

import model.VehicleType;
import java.util.*;

public class Army {
    /**
     * @var vehicles in army
     */
    private Map<Long, SmartVehicle> vehicles;

    /**
     * @var vehicles in army durability sum
     */
    private int durability;

    /**
     * @var durability update tick
     */
    private int durabilityUpdateTick;

    /**
     * @var vehicles in army by vehicle type
     */
    private Map<VehicleType, List<SmartVehicle>> vehiclesByType;

    /**
     * @desc army form
     */
    private ArmyForm form;

    /**
     * @var last update tick
     */
    private Integer lastUpdateTick;

    /**
     * @var vehicles type count
     */
    private HashMap<VehicleType, Integer> vehicleTypes;

    /**
     * @var army max vision range
     */
    private double maxVisionRange;

    /**
     * @var update damage field tick
     */
    private Integer updateDamageFieldTick;

    /**
     * @var PP damage field
     */
    private PPFieldEnemy damageField;
    private Set<ArmyAllyOrdering> vehiclesContainedArmies;

    public Army() {
        form = new ArmyForm();

        vehicles = new HashMap<>();
        vehicleTypes = new HashMap<>();
        vehiclesByType = new HashMap<>();

        durability = 0;
        durabilityUpdateTick = -1;
        updateDamageFieldTick = -1;
        lastUpdateTick = -1;
        vehiclesContainedArmies = new HashSet<>();
    }

    /**
     * @desc get army vehicles
     * @return map of vehicles, where keys are identifiers of vehicle
     */
    public Map<Long, SmartVehicle> getVehicles() {
        return vehicles;
    }

    /**
     * @desc add vehicle to army
     * @param vehicle
     */
    public void addVehicle (SmartVehicle vehicle) {
        //set last modificated of army tick
        setLastUpdateTick(MyStrategy.world.getTickIndex());
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

        if (vehicle.getArmy() != null) {
            addArmy(vehicle.getArmy());
        }

        //update max vision range of army
        maxVisionRange = Math.max(maxVisionRange, vehicle.getMinVisionRange());
    }

    public int getDurability() {
        if (durabilityUpdateTick == MyStrategy.world.getTickIndex()) {
            return durability;
        }

        durability = getVehicles().
                    values().
                    stream().
                    mapToInt(SmartVehicle::getDurability).
                    sum();

        return durability;
    }

    public double averageDurability() {
        return getDurability() / getVehicleCount();
    }

    public void putVehicle(SmartVehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
        getForm().addPoint(vehicle.getPoint());
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
        return getVehicles().size();
    }

    public ArmyForm getForm() {
        return form;
    }


    public boolean isAlive() {
        return getVehicleCount() > 0;
    }

    public SmartVehicle getGunnerVehicle(Point2D target) {
        double maxDistance = 0.f;
        SmartVehicle gunnerVehicle = null;

        double maxSafetyDistance = 0.f;
        SmartVehicle safetyDistanceVehicle = null;

        for (SmartVehicle vehicle : getVehicles().values()) {
            if (vehicle.getDurability() > 0) {
                double distance = vehicle.distanceToPoint(target.getX(), target.getY());

                if (distance <= vehicle.getActualVisionRange() && distance > maxDistance) {
                    gunnerVehicle = vehicle;
                    maxDistance = distance;
                }

                if (distance <= vehicle.getActualVisionRange() + MyStrategy.game.getTacticalNuclearStrikeRadius() && distance > maxSafetyDistance) {
                    maxSafetyDistance = distance;
                    safetyDistanceVehicle = vehicle;
                }
            }
        }

        return gunnerVehicle != null ? gunnerVehicle : safetyDistanceVehicle;
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

    public void setLastUpdateTick(Integer tick) {
        lastUpdateTick = tick;
    }

    public Integer getLastUpdateTick() {
        return lastUpdateTick;
    }


    public boolean timeToGoHeal() {
        return getDurability() / (double)(getVehicleCount() * 100) < CustomParams.percentOfHeatedVehicles;
    }

    public boolean isAerial () {
        return getVehiclesType().contains(VehicleType.HELICOPTER) || getVehiclesType().contains(VehicleType.FIGHTER);
    }

    public boolean isTerrain() {
        return !isAerial();
    }

    public double getMinSpeed() {

        Iterator<VehicleType> typeIterator = getVehiclesType().iterator();
        VehicleType type = typeIterator.next();

        double terrainFactor = MyStrategy.game.getSwampTerrainSpeedFactor();
        double aerialFactor = MyStrategy.game.getRainWeatherSpeedFactor();

        double minSpeed = vehiclesByType.get(type).iterator().next().getMaxSpeed() *
                                (SmartVehicle.isTerrain(type) ? terrainFactor : aerialFactor);

        while (typeIterator.hasNext()) {
            double factor = SmartVehicle.isTerrain(type) ? terrainFactor : aerialFactor;
            double speed = vehiclesByType.get(type).get(0).getMaxSpeed() * factor;
            if (speed < minSpeed) {
                minSpeed = speed;
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

                        if (maxFactor < damageField.getFactorOld(point)) {
                            maxFactor = damageField.getFactorOld(point);
                            maxFactorPoint = point;
                        }

                        if (minFactor > damageField.getFactorOld(point)) {
                            minFactor = damageField.getFactorOld(point);
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

                    if (maxFactor < damageField.getFactorOld(point)) {
                        maxFactor = damageField.getFactorOld(point);
                        maxFactorPoint = point;
                    }

                    if (minFactor > damageField.getFactorOld(point)) {
                        minFactor = damageField.getFactorOld(point);
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

    public PPFieldEnemy getDamageField() {
        if (updateDamageFieldTick == MyStrategy.world.getTickIndex()) {
            return damageField;
        }
        damageField = MyStrategy.enemyField.getDamageField(getVehiclesType());
        return damageField;
    }

    public List<SmartVehicle> getMaxDamageVehicle() {
        List<SmartVehicle> maxDamageVehicles = new ArrayList<>();
        PPFieldEnemy damageField = getDamageField();
        Collection<SmartVehicle> edgesVehicles = getForm().getEdgesVehicles().values();
        SmartVehicle maxDamageVehicle = edgesVehicles.iterator().next();
        double maxDamage = damageField.getFactor(maxDamageVehicle.getPoint());

        for (SmartVehicle vehicle : edgesVehicles) {
            if (maxDamage > damageField.getFactor(vehicle.getPoint())) {
                maxDamage = damageField.getFactor(vehicle.getPoint());
            }
        }

        for (SmartVehicle vehicle : edgesVehicles) {
            if (Math.abs(maxDamage - damageField.getFactor(vehicle.getPoint())) <= CustomParams.damageDelta) {
                maxDamageVehicles.add(vehicle);
            }
        }

        return maxDamageVehicles;
    }

    public double getMaxDamageVehicleTurnedAngle(Point2D target) {

        List<SmartVehicle> maxDamageVehicles = getMaxDamageVehicle();

        Iterator<SmartVehicle> iterator = maxDamageVehicles.iterator();
        SmartVehicle maxDamageVehicle = iterator.next();
        double minDistance = target.subtract(maxDamageVehicle.getPoint()).magnitude();

        while (iterator.hasNext()) {
            SmartVehicle vehicle = iterator.next();
            double distance = target.subtract(vehicle.getPoint()).magnitude();
            if (distance < minDistance) {
                maxDamageVehicle = vehicle;
            }
        }

        Iterator<SmartVehicle> edgeVehiclesIterator = getForm().getEdgesVehicles().values().iterator();

        SmartVehicle nearestToTargetEdgeVehicle = edgeVehiclesIterator.next();
        minDistance = target.distance(nearestToTargetEdgeVehicle.getPoint());

        while (edgeVehiclesIterator.hasNext()) {
            SmartVehicle edgeVehicle = edgeVehiclesIterator.next();
            double distance = target.distance(edgeVehicle.getPoint());

            if (distance < minDistance) {
                nearestToTargetEdgeVehicle = edgeVehicle;
                minDistance = distance;
            }
        }
        Point2D armyCenter = getForm().getEdgesVehiclesCenter();

        Point2D maxDamageArmyCenterVector = maxDamageVehicle.getPoint().subtract(armyCenter).normalize();
        Point2D nearestToTargetVehicleVector = nearestToTargetEdgeVehicle.getPoint().subtract(armyCenter).normalize();

        double dot = maxDamageArmyCenterVector.dotProduct(nearestToTargetVehicleVector);
        double cross = maxDamageArmyCenterVector.cross(nearestToTargetVehicleVector);
        return Math.atan2(cross, dot);
    }

    public SmartVehicle getFarestVehicleFromPoint(Point2D point) {

        Iterator<SmartVehicle> edgeVehiclesIterator = getForm().getEdgesVehicles().values().iterator();

        SmartVehicle farestToTargetEdgeVehicle = edgeVehiclesIterator.next();
        double maxDistance = point.distance(farestToTargetEdgeVehicle.getPoint());

        while (edgeVehiclesIterator.hasNext()) {
            SmartVehicle edgeVehicle = edgeVehiclesIterator.next();
            double distance = point.distance(edgeVehicle.getPoint());

            if (distance > maxDistance) {
                farestToTargetEdgeVehicle = edgeVehicle;
            }
        }

        return farestToTargetEdgeVehicle;
    }

    public void addArmy (ArmyAllyOrdering army) {
        if (!vehiclesContainedArmies.contains(army)) {
            vehiclesContainedArmies.add(army);
        }
    }

    public Set<ArmyAllyOrdering> getVehiclesArmies() {
        return vehiclesContainedArmies;
    }
    public boolean isHeat (Army army) {
        Collection<SmartVehicle> edgesVehicles = getForm().getEdgesVehicles().values();

        for (SmartVehicle vehicle : army.getForm().getEdgesVehicles().values()) {
            for (SmartVehicle vehicle1 : edgesVehicles) {
                if (vehicle.getPoint().distance(vehicle1.getPoint()) <= 10 ) {
                    return true;
                }
            }
        }

        return false;
    }

}
