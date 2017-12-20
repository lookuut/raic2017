import model.Vehicle;
import model.VehicleType;

import java.util.*;

public class Army {

    private Map<Long, SmartVehicle> vehicles;
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
    }


    public Map<Long, SmartVehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle (SmartVehicle vehicle) {
        setLastModificateTick(MyStrategy.world.getTickIndex());
        form.addPoint(vehicle.getPoint());
        this.putVehicle(vehicle);
        Integer count = 1;
        if (vehicleTypes.containsKey(vehicle.getType())) {
            count += vehicleTypes.get(vehicle.getType());
        }

        if (!vehiclesByType.containsKey(vehicle.getType())) {
            vehiclesByType.put(vehicle.getType(), new ArrayList<>());
        }

        vehiclesByType.get(vehicle.getType()).add(vehicle);
        vehicleTypes.put(vehicle.getType(), count);
        getForm().updateEdgesVehicles(vehicle);
        maxVisionRange = Math.max(maxVisionRange, vehicle.getMinVisionRange());
    }

    public void putVehicle(SmartVehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
        getForm().addPoint(vehicle.getPoint());
        getForm().updateEdgesVehicles(vehicle);
        maxVisionRange = Math.max(maxVisionRange, vehicle.getMinVisionRange());
    }

    public void removeVehicle(SmartVehicle vehicle) {
        vehicles.remove(vehicle);
        vehicleTypes.put(vehicle.getType(), vehicleTypes.get(vehicle.getType()) - 1);
        getForm().removeVehicle(vehicles, vehicle);
    }



    public boolean containVehicle(Long vehicleId) {
        return vehicles.containsKey(vehicleId);
    }

    public Set<VehicleType> getVehiclesType () {
        return vehicleTypes.keySet();
    }

    public Long getVehicleCount() {
        return vehicles.entrySet().stream().filter(entry -> entry.getValue().getDurability() > 0).count();
    }

    public ArmyForm getForm() {
        return form;
    }

    public boolean isArmyAlive () {
        return getVehicleCount() > 0;
    }

    public SmartVehicle getNearestVehicle(Point2D point) {
        Point2D[] points = new Point2D[1];
        points[0] = point;
        SmartVehicle[] vehicles = getNearestVehicle(points);
        return vehicles[0];
    }

    public SmartVehicle getGunnerVehicle(Point2D target) {
        double minDistance = Double.MAX_VALUE;
        double afterMinDistance = Double.MAX_VALUE;
        SmartVehicle afterMinDistanceVehicle = getVehicles().values().stream().findFirst().get();

        for (SmartVehicle vehicle : getVehicles().values()) {
            if (vehicle.getDurability() >= CustomParams.gunnerMinDurability) {
                double distance = target.subtract(vehicle.getPoint()).magnitude();
                if (distance < minDistance) {
                    minDistance = distance;
                } else {
                    if (distance < afterMinDistance) {
                        afterMinDistanceVehicle = vehicle;
                        afterMinDistance = distance;
                    }
                }
            }
        }
        return afterMinDistanceVehicle;
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

    public Map<VehicleType, List<SmartVehicle>> getVehiclesByType () {
        return this.vehiclesByType;
    }
}
