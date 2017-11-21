import model.VehicleType;

import java.util.*;
import java.util.stream.Collectors;

public class AllyArmy extends Army {

    protected Queue<Command> commandQueue;
    protected Map<Long, SmartVehicle> vehicles;
    protected Integer groupId;
    protected BattleField armyBattleField;
    protected BattleField globalBattleField;

    public AllyArmy() {
        super();
        vehicles = new HashMap<>();
        commandQueue = new LinkedList<>();
        //armyBattleField = new BattleField();
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

    public SmartVehicle getNearestVehicle(double x, double y) {
        Map.Entry<Long, SmartVehicle> item = vehicles.entrySet().stream().min(
                (entry1, entry2) ->  Double.compare(
                        Math.pow((entry1.getValue().getX() - x) , 2) + Math.pow((entry1.getValue().getY()) - y , 2),
                        Math.pow(entry2.getValue().getX() - x , 2) + Math.pow(entry2.getValue().getY() - y , 2)
                )
        ).get();

        return item.getValue();
    }

    public void moveTo(double x, double y) {
        List<VehicleType> armyTypes = getVehiclesType();

        double armyX = getAvgX();
        double armyY = getAvgY();


    }

    /**
     * @TODO rewrite with local cache
     * @desc
     * @return
     */
    public List<VehicleType> getVehiclesType () {
        return vehicles.entrySet().stream().map((entry) -> entry.getValue().getType()).collect(Collectors.toList());
    }
}
