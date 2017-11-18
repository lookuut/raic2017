import model.VehicleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class Commander {
    protected Map<Integer, AllyArmy> divisions;
    protected MyStrategy strategy;
    protected BehaviourTree<Commander> behaviourTree;
    protected List<Command> runningCommandList;

    public Commander(MyStrategy strategy) {
        this.strategy = strategy;
        divisions = new HashMap();
        behaviourTree = new BehaviourTree<>();
        runningCommandList = new ArrayList<>();

        BTreeNodeCondition<Commander> rootNode = new BTreeNodeCondition(
                (Predicate<Commander>)((commander) -> commander.isHaveFighterArmy()),
                this);

        AllyArmy fighterArmy = new FighterArmy();

        rootNode.addChildNode(new BTreeAction(new CommandCreateArmy(fighterArmy, VehicleType.FIGHTER)));
        rootNode.addChildNode(new BTreeAction(new CommandMove(500,500, fighterArmy)));

        behaviourTree.addRoot(rootNode);
    }


    /**
     * @desc analysis divisions, create new divisions if need, generate command to
     */
    public void formDivisions () {
        /*
        if (divisions.size() == 0) {
            AllyArmy fighterArmy = new AllyArmy();
            fighterArmy.setGroupId(1);
            fighterArmy.addCommand(new CommandCreateArmy(fighterArmy, VehicleType.FIGHTER));
            fighterArmy.addCommand(new CommandMove(500,500, fighterArmy));
            fighterArmy.addCommand(new CommandMove(0,500, fighterArmy));
            divisions.put(1, fighterArmy);

            AllyArmy helicopterArmy = new AllyArmy();
            helicopterArmy.setGroupId(2);
            helicopterArmy.addCommand(new CommandCreateArmy(helicopterArmy, VehicleType.HELICOPTER));
            helicopterArmy.addCommand(new CommandMove(500,100, helicopterArmy));
            helicopterArmy.addCommand(new CommandMove(1000,500, helicopterArmy));
            divisions.put(2, helicopterArmy);
        }*/
    }
    public void logic () {
        BTreeAction action = behaviourTree.getAction();
        action.getCommand().run();
        runningCommandList.add(action.getCommand());
    }

    public List<AllyArmy> getRunningArmy() {
        return divisions.values().stream().filter(army -> army.isRun()).collect(Collectors.toList());
    }

    public void run() {
        for (Map.Entry<Integer, AllyArmy> entry : divisions.entrySet()) {
            entry.getValue().run();
        }
    }

    public void check () {
        for (Command command : runningCommandList) {
            command.check();
        }

        for (Map.Entry<Integer, AllyArmy> entry : divisions.entrySet()) {
            entry.getValue().check();
        }
    }

    public Map<Integer, AllyArmy> getDivisions() {
        return divisions;
    }

    public boolean isHaveFighterArmy() {
        for (Map.Entry<Integer, AllyArmy> entry : divisions.entrySet()) {
            if (entry.getValue() instanceof FighterArmy && entry.getValue().getVehicles().size() > 0) {
                return true;
            }
        }

        return false;
    }

}