
import model.Vehicle;
import model.World;

import java.util.Collection;
import java.util.HashMap;

public class ArmyField {

    protected HashMap<Long, SmartVehicle> vehicles;

    public static void analysisField (World world, Collection<ArmyField> armyFields) {

    }


    public boolean contains (Long vehicleId) {
        return vehicles.containsKey(vehicleId);
    }

    public void recalcVehicleState (Vehicle vehicle) {
        // do something
        return;
    }
}
