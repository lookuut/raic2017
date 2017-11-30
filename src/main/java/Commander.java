import model.VehicleType;

import java.util.*;

import java.util.function.Predicate;
import java.util.stream.Collectors;

class Commander {
    protected Map<Integer, ArmyAllyOrdering> divisions;
    protected MyStrategy strategy;
    protected BehaviourTree<ArmyAlly> behaviourTree;
    protected Queue<BTreeAction> activeActions;

    /**
     * @desc all armies must be init in constructor
     * @param strategy
     */
    public Commander(MyStrategy strategy) {
        this.strategy = strategy;
        divisions = new HashMap();
        activeActions = new LinkedList<>();
        behaviourTree = new BehaviourTree<>();

        ArmyAllyOrderingArrv arrvArmy = new ArmyAllyOrderingArrv(CustomParams.arrvArmyId);
        ArmyAllyOrderingFighter fighterArmy = new ArmyAllyOrderingFighter(CustomParams.fighterArmyId);
        ArmyAllyOrdering helicopterArmy = new ArmyAllyOrdering(CustomParams.helicopterArmyId);
        ArmyAllyOrdering tankArmy = new ArmyAllyOrdering(CustomParams.tankArmyId);
        ArmyAllyOrdering ifvArmy = new ArmyAllyOrdering(CustomParams.ifvArmyId);
        ArmyAllyOrdering allArmy = new ArmyAllyOrdering(CustomParams.allArmyId);

        divisions.put(CustomParams.arrvArmyId, arrvArmy);
        divisions.put(CustomParams.fighterArmyId, fighterArmy);
        divisions.put(CustomParams.helicopterArmyId, helicopterArmy);
        divisions.put(CustomParams.tankArmyId, tankArmy);
        divisions.put(CustomParams.ifvArmyId, ifvArmy);
        //divisions.put(CustomParams.allArmyId, allArmy);

        arrvArmy.addCommand(new CommandCreateArmy(VehicleType.ARRV));
        fighterArmy.addCommand(new CommandCreateArmy(VehicleType.FIGHTER));
        helicopterArmy.addCommand(new CommandCreateArmy(VehicleType.HELICOPTER));
        tankArmy.addCommand(new CommandCreateArmy(VehicleType.TANK));
        ifvArmy.addCommand(new CommandCreateArmy(VehicleType.IFV));
        allArmy.addCommand(new CommandCreateArmy(null));

        //fighter behaviour tree
        BehaviourTree fighterBehaviourTree = new BehaviourTree<>();

        BTreeNodeCondition<ArmyAlly> nuckAttackCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((army) -> MyStrategy.canNuclearAttack()),
                fighterArmy);


        BTreeNodeCondition<ArmyAlly> defenceCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((army) -> army.percentOfDeathVehicles() > 0.75),
                fighterArmy);

        nuckAttackCond.addChildNode(new BTreeAction(() -> new CommandNuclearAttack()));
        nuckAttackCond.addChildNode(defenceCond);
        defenceCond.addChildNode(new BTreeAction(() -> new CommandDefence()));
        defenceCond.addChildNode(new BTreeAction(() -> new CommandAttack()));

        fighterBehaviourTree.addRoot(nuckAttackCond);

        fighterArmy.setBehaviourTree(fighterBehaviourTree);

        this.setEmptyBehaviourTree(helicopterArmy);
        this.setEmptyBehaviourTree(tankArmy);
        this.setEmptyBehaviourTree(ifvArmy);
        this.setDefenceBehaviourTree(arrvArmy);
        this.setAllArmyBehaviourTree(allArmy);
    }

    public void setAllArmyBehaviourTree(ArmyAllyOrdering army) {
        BehaviourTree<ArmyAlly> bTree = new BehaviourTree<>();
        BTreeNode root = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> MyStrategy.isNuclearAttack()),
                army
        );

        root.addChildNode(new BTreeAction(() -> new CommandNuclearDefence()));
        root.addChildNode(new BTreeAction(() -> new CommandEmpty()));

        bTree.addRoot(root);
        army.setBehaviourTree(bTree);
    }

    public void setDefenceBehaviourTree(ArmyAllyOrdering army) {
        BehaviourTree<ArmyAlly> bTree = new BehaviourTree<>();
        BTreeNode root = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> true),
                army
        );
        root.addChildNode(new BTreeAction(() -> new CommandDefence()));
        root.addChildNode(new BTreeAction(() -> new CommandEmpty()));
        bTree.addRoot(root);
        army.setBehaviourTree(bTree);
    }

    public void setEmptyBehaviourTree(ArmyAllyOrdering army) {
        BehaviourTree<ArmyAlly> bTree = new BehaviourTree<>();
        BTreeNode root = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> true),
                army
        );
        root.addChildNode(new BTreeAction(() -> new CommandAttack()));
        root.addChildNode(new BTreeAction(() -> new CommandEmpty()));
        bTree.addRoot(root);
        army.setBehaviourTree(bTree);
    }


    public void initStaticPPField () throws Exception {

        int ppFieldX = (int)(strategy.getWorld().getWidth() / strategy.getGame().getTerrainWeatherMapColumnCount());
        int ppFieldY = (int)(strategy.getWorld().getHeight() / strategy.getGame().getTerrainWeatherMapRowCount());
        TerrainPPField terrainPPField = new TerrainPPField(ppFieldX, ppFieldY);
        terrainPPField.addTerrainMap(strategy.getWorld().getTerrainByCellXY());

        WeatherPPField weatherPPField = new WeatherPPField(ppFieldX, ppFieldY);
        weatherPPField.addWeatherMap(MyStrategy.getWeatherMap());

        for (Map.Entry<Integer, ArmyAllyOrdering> entry : divisions.entrySet()) {
            entry.getValue().init(terrainPPField, weatherPPField);
        }
    }

    public void logic (BattleField battleField) throws Exception {
        //run divisions logic
        for (Map.Entry<Integer, ArmyAllyOrdering> entry : divisions.entrySet()) {
            if (entry.getValue().isArmyAlive()) {
                entry.getValue().run(battleField);
                entry.getValue().check();
            }
        }
    }


    public List<ArmyAllyOrdering> getArmyRunningCommands() {
        return divisions.values().stream().filter(army -> army.isRun()).collect(Collectors.toList());
    }

    public void check () {
        for (Map.Entry<Integer, ArmyAllyOrdering> entry : divisions.entrySet()) {
            entry.getValue().check();
        }
    }

    public Map<Integer, ArmyAllyOrdering> getDivisions() {
        return divisions;
    }
}