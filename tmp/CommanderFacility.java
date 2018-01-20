
import model.Facility;
import model.FacilityType;
import model.VehicleType;

import java.util.*;
import java.util.function.Function;

public class CommanderFacility {

    private Map<Long, SmartFacility> facilities;
    private Map<FacilityType, Set<SmartFacility>> facilitiesByTypes;
    private Map<ArmyAllyOrdering, SmartFacility> armiesGotoFacility;
    private Deque<Command> createVehicleCommandQueue;
    public CommanderFacility () {
        facilities = new HashMap<>();
        facilitiesByTypes = new HashMap<>();
        armiesGotoFacility = new HashMap<>();
        createVehicleCommandQueue = new LinkedList<>();
    }

    public void orderCreateVehicle() {
        try {

            Map<VehicleType, Integer> enemyTypeVehiclesCount = MyStrategy.getEnemyTypeVehiclesCount();
            Map<VehicleType, Integer> allyTypeVehiclesCount = MyStrategy.getAllyTypeVehiclesCount();

            int maxDelta = 0;
            VehicleType maxDeltaVehicleType = VehicleType.FIGHTER;
            for (VehicleType type : VehicleType.values()) {
                int delta = enemyTypeVehiclesCount.get(type) - allyTypeVehiclesCount.get(type);
                if (delta > 0 && delta > maxDelta) {
                    maxDelta = delta;
                    maxDeltaVehicleType = type;
                }
            }

            if (allyTypeVehiclesCount.get(maxDeltaVehicleType) / (double)enemyTypeVehiclesCount.get(maxDeltaVehicleType) >= 1.2) {
                return;
            }

            for (SmartFacility facility : facilities.values()) {
                if (facility.getOwnerPlayerId() == MyStrategy.player.getId() &&
                        facility.getProductionProgress() == 0 &&
                        facility.getType() == FacilityType.VEHICLE_FACTORY &&
                        createVehicleCommandQueue.size() < 2) {
                    createVehicleCommandQueue.add(new CommandCreateVehicle(facility.getId(), SmartVehicle.getVictimType(maxDeltaVehicleType)));
                }
            }

            if (createVehicleCommandQueue.size() > 0 && createVehicleCommandQueue.getFirst().isNew()) {
                createVehicleCommandQueue.getFirst().run(null);
            }

            if (createVehicleCommandQueue.size() > 0 && createVehicleCommandQueue.getFirst().isFinished()) {
                createVehicleCommandQueue.poll();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFacility(Facility facility) {
        SmartFacility smartFacility;
        if (!facilities.containsKey(facility.getId())) {
            smartFacility = new SmartFacility(facility);
            facilities.put(facility.getId(), smartFacility);
        } else {
            smartFacility = facilities.get(facility.getId());
            smartFacility.update(facility);
        }

        Set<SmartFacility> setFacilities;
        if (!facilitiesByTypes.containsKey(facility.getType())) {
            setFacilities = new HashSet<>();
            facilitiesByTypes.put(facility.getType(), setFacilities);
        } else {
            setFacilities = facilitiesByTypes.get(smartFacility.getType());
        }
        setFacilities.add(smartFacility);

        if (facility.getOwnerPlayerId() == MyStrategy.player.getId()) {
            for (ArmyAllyOrdering army : smartFacility.getGoingToFacilityArmies()) {
                armiesGotoFacility.remove(army);
            }
            smartFacility.clearGoingToFacilityArmies();
        }
    }

    public SmartFacility getFacilityToSiege(ArmyAllyOrdering army) {

        if (armiesGotoFacility.containsKey(army)) {
            return armiesGotoFacility.get(army);
        }

        Function<Collection<SmartFacility>, SmartFacility> getMinDistanceFacility = (facilities) -> {
            double minDistance = Double.MAX_VALUE;
            SmartFacility minDistFacility = null;
            for (SmartFacility facility : facilities) {
                if (facility.getGoingToFacilityArmies().size() == 0 && facility.getOwnerPlayerId() != MyStrategy.player.getId()) {
                    Point2D facilityCentre = facility.getFacilityCentre();
                    double distance = facilityCentre.distance(army.getForm().getAvgPoint());
                    if (distance < minDistance) {
                        minDistance = distance;
                        minDistFacility = facility;
                    }
                }
            }
            return minDistFacility;
        };

        SmartFacility minDistFacility = getMinDistanceFacility.apply(facilities.values());

        if (minDistFacility != null) {
            minDistFacility.addGoingToFacilityArmy(army);
            armiesGotoFacility.put(army, minDistFacility);
        }

        return minDistFacility;
    }


}
