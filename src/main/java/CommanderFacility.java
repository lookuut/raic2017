import model.Facility;
import model.FacilityType;
import model.VehicleType;

import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

public class CommanderFacility {

    private Map<Long, SmartFacility> facilities;
    private Map<FacilityType, Set<SmartFacility>> facilitiesByTypes;
    private Map<ArmyAllyOrdering, SmartFacility> armiesGotoFacility;

    public CommanderFacility () {
        facilities = new HashMap<>();
        facilitiesByTypes = new HashMap<>();
        armiesGotoFacility = new HashMap<>();
    }

    public void orderCreateVehicle() {
        try {
            for (SmartFacility facility : facilities.values()) {
                if (facility.getOwnerPlayerId() == MyStrategy.player.getId() &&
                        facility.getProductionProgress() == 0 &&
                        facility.getType() == FacilityType.VEHICLE_FACTORY) {
                    CommandCreateVehicle createVehicle = new CommandCreateVehicle(facility.getId(), VehicleType.HELICOPTER);
                    createVehicle.run(null);
                }
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

        Function<Set<SmartFacility>, SmartFacility> getMinDistanceFacility = (facilities) -> {
            double minDistance = Double.MAX_VALUE;
            SmartFacility minDistFacility = null;
            for (SmartFacility facility : facilities) {
                if (facility.getGoingToFacilityArmies().size() < CustomParams.maxGotoSiegeArmyCount &&
                        facility.getOwnerPlayerId() != MyStrategy.player.getId()) {
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
        SmartFacility minDistFacility = null;

        FacilityType priorityFacilityType = FacilityType.CONTROL_CENTER;
        if (armiesGotoFacility.values().stream().filter(facility -> facility.getType() == FacilityType.CONTROL_CENTER).count() * 2 >= armiesGotoFacility.size()) {
            priorityFacilityType = FacilityType.VEHICLE_FACTORY;
        }

        if (facilitiesByTypes.containsKey(priorityFacilityType) ) {
            minDistFacility = getMinDistanceFacility.apply(facilitiesByTypes.get(priorityFacilityType));
        }
        if (facilitiesByTypes.containsKey(priorityFacilityType) && minDistFacility == null) {
            minDistFacility = getMinDistanceFacility.apply(facilitiesByTypes.get(priorityFacilityType));
        }

        if (minDistFacility != null) {
            armiesGotoFacility.put(army, minDistFacility);
        }

        return minDistFacility;
    }


}
