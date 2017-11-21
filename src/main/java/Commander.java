import model.TerrainType;
import model.VehicleType;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class Commander {
    protected Map<Integer, AllyArmy> divisions;
    protected MyStrategy strategy;
    protected BehaviourTree<Commander> behaviourTree;
    protected Queue<BTreeAction> activeActions;

    protected BattleField battleField;

    protected PPField staticTerrainField;
    protected PPField staticAirField;

    public Commander(MyStrategy strategy) {
        this.strategy = strategy;
        divisions = new HashMap();
        activeActions = new LinkedList<>();
        behaviourTree = new BehaviourTree<>();
        AllyArmy fighterArmy = new FighterArmy();

        BTreeNodeCondition<Commander> rootNode = new BTreeNodeCondition(
                (Predicate<Commander>)((commander) -> !commander.isHaveFighterArmy()),
                this);

        BTreeNodeCondition<Commander> nuclearAttackCond = new BTreeNodeCondition(
                (Predicate<Commander>)((commander) -> MyStrategy.canNuclearAttack()),
                this);

        BTreeNode isNuclearAtttack = new BTreeNodeCondition((Predicate<Commander>)((commander) -> MyStrategy.player.getNextNuclearStrikeTickIndex() > 0), this);

        isNuclearAtttack.addChildNode(new BTreeAction(() -> new CommandNuclearDefence(this)));
        isNuclearAtttack.addChildNode(new BTreeAction((Supplier<Command>)(() -> new CommandEmpty(fighterArmy))));

        fighterArmy.setGroupId(1);
        rootNode.addChildNode(new BTreeAction((Supplier<Command>)(() -> new CommandCreateArmy(fighterArmy, VehicleType.FIGHTER))));
        rootNode.addChildNode(nuclearAttackCond);

        BTreeNode nucAttack = new BTreeActionSequence((Supplier<Command>)(() -> new CommandMove(fighterArmy, 400, 100)));
        ((BTreeActionSequence)nucAttack).addCommand((Supplier<Command>)(() -> new CommandNuclearAttack(fighterArmy, 400, 100)));

        behaviourTree.addRoot(rootNode);
        nuclearAttackCond.addChildNode(nucAttack);
        nuclearAttackCond.addChildNode(isNuclearAtttack);

        divisions.put(1, fighterArmy);
    }

    public void initStaticPPField () {

        int ppFieldX = (int)(strategy.getWorld().getWidth() / strategy.getGame().getTerrainWeatherMapColumnCount());
        int ppFieldY = (int)(strategy.getWorld().getHeight() / strategy.getGame().getTerrainWeatherMapRowCount());
        PPField terrainPPField = new PPField(ppFieldX, ppFieldY);
        terrainPPField.addTerrainMap(strategy.getWorld().getTerrainByCellXY());

        PPField weatherPPField = new PPField(ppFieldX, ppFieldY);
        weatherPPField.addWeatherMap(strategy.getWorld().getWeatherByCellXY());
    }

    public void logic (BattleField battleField) {
        BTreeAction action = behaviourTree.getAction();
        if (action != null && !activeActions.contains(action)) {
            activeActions.add(action);
        }

        for (BTreeAction _action : activeActions) {
            Command command = _action.getCommand();
            if (command != null) {
                _action.getCommand().run();
            }
        }
    }

    public List<Command> getRunningCommands () {
        return activeActions.stream().filter(command -> command.isRun()).map(BTreeAction::getCommand).collect(Collectors.toList());
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

        for (BTreeAction action : activeActions) {
            if (!action.isComplete()) {
                Command command = action.getCommand();
                command.check();

                if (command.getState() == CommandStates.Complete || command.getState() == CommandStates.Failed) {
                    //activeActions.remove(command);
                }
            }
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