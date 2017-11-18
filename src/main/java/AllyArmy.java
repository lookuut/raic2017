import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AllyArmy extends Army {

    protected Queue<Command> commandQueue;
    protected Map<Long, SmartVehicle> vehicles;
    protected Integer groupId;

    public AllyArmy() {
        super();
        vehicles = new HashMap<>();
        commandQueue = new LinkedList<>();
    }

    public void setGroupId (Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupId () {
        return groupId;
    }

    public Map<Long, SmartVehicle> getVehicles() {
        return vehicles;
    }

    public void addVehicle (SmartVehicle vehicle) {
        minMaxUpdate(vehicle);
        this.putVehicle(vehicle);
    }

    public void putVehicle(SmartVehicle vehicle) {
        vehicles.put(vehicle.getId(), vehicle);
    }

    protected void recalculationMaxMin () {
        if (vehicles.size() > 0) {
            this.maxX = vehicles.entrySet().iterator().next().getValue().getX();
            this.maxY = vehicles.entrySet().iterator().next().getValue().getY();
            this.minX = this.maxX;
            this.minY = this.maxY;

            for (Map.Entry<Long, SmartVehicle> entry : vehicles.entrySet()) {
                minMaxUpdate(entry.getValue());
            }
        }
    }

    public void addCommand(Command command) {
        commandQueue.add(command);
    }

    public void run () {
        if (commandQueue.size() == 0) {
            return;
        }
        Command command = commandQueue.peek();

        if (command.getState() == CommandStates.New) {
            command.run();
        }
    }

    public void check () {

        if (commandQueue.size() == 0) {
            return;
        }
        Command command = commandQueue.peek();

        if (command.getState() == CommandStates.Run) {
            recalculationMaxMin();

            if (command.check()) {
                command.setState(CommandStates.Complete);
                commandQueue.poll();
            }
        }
    }

    public boolean isRun() {
        return commandQueue.size() > 0 && commandQueue.peek().getState() == CommandStates.Run;
    }

    public void result(SmartVehicle vehicle) {
        if (commandQueue.size() > 0 && commandQueue.peek().getState() == CommandStates.Run) {
            commandQueue.peek().result(vehicle);
        }
    }

    public boolean containVehicle(Long vehicleId) {
        return vehicles.containsKey(vehicleId);
    }
}
