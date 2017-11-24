import model.VehicleType;

import java.util.*;

import java.util.function.Predicate;
import java.util.stream.Collectors;

class Commander {
    protected Map<Integer, AllyArmy> divisions;
    protected MyStrategy strategy;
    protected BehaviourTree<AllyArmy> behaviourTree;
    protected Queue<BTreeAction> activeActions;

    protected BattleField battleField;


    public static Integer fighterArmyId = 2;
    /**
     * @desc all armies must be init in constructor
     * @param strategy
     */
    public Commander(MyStrategy strategy) {
        this.strategy = strategy;
        divisions = new HashMap();
        activeActions = new LinkedList<>();
        behaviourTree = new BehaviourTree<>();

        AllyArmy arrvArmy = new ARRVArmy();
        AllyArmy fighterArmy = new FighterArmy();
        AllyArmy helicopterArmy = new AllyArmy();
        AllyArmy tankArmy = new AllyArmy();
        AllyArmy ifvArmy = new AllyArmy();
        AllyArmy allArmy = new AllyArmy();

        arrvArmy.setGroupId(1);
        fighterArmy.setGroupId(fighterArmyId);
        helicopterArmy.setGroupId(3);
        tankArmy.setGroupId(4);
        ifvArmy.setGroupId(5);
        allArmy.setGroupId(6);

        //divisions.put(1, arrvArmy);
        divisions.put(fighterArmyId, fighterArmy);
        divisions.put(3, helicopterArmy);
        divisions.put(4, tankArmy);
        divisions.put(5, ifvArmy);
        //divisions.put(6, allArmy);

        arrvArmy.addCommand(new CommandCreateArmy(VehicleType.ARRV));
        fighterArmy.addCommand(new CommandCreateArmy(VehicleType.FIGHTER));
        helicopterArmy.addCommand(new CommandCreateArmy(VehicleType.HELICOPTER));
        tankArmy.addCommand(new CommandCreateArmy(VehicleType.TANK));
        ifvArmy.addCommand(new CommandCreateArmy(VehicleType.IFV));
        allArmy.addCommand(new CommandCreateArmy(null));

        //setEmptyBehaviourTree(arrvArmy);

        //fighter behaviour tree
        BehaviourTree fighterBehaviourTree = new BehaviourTree<>();

        BTreeNodeCondition<AllyArmy> rootNode = new BTreeNodeCondition(
                (Predicate<AllyArmy>)((army) -> false),
                fighterArmy);


        BTreeNodeCondition<AllyArmy> nuclearAttackCond = new BTreeNodeCondition(
                (Predicate<AllyArmy>)((army) -> MyStrategy.canNuclearAttack()),
                fighterArmy);
        BTreeNode gotoHeal = new BTreeActionSequence(() -> new CommandAttack());
        rootNode.addChildNode(gotoHeal);
        rootNode.addChildNode(nuclearAttackCond);
        nuclearAttackCond.addChildNode(new BTreeAction(() -> new CommandAttack()));
        nuclearAttackCond.addChildNode(new BTreeAction(() -> new CommandAttack()));

        fighterBehaviourTree.addRoot(rootNode);

        fighterArmy.setBehaviourTree(fighterBehaviourTree);
        this.setEmptyBehaviourTree(helicopterArmy);
        this.setEmptyBehaviourTree(tankArmy);
        this.setEmptyBehaviourTree(ifvArmy);

        this.setAllArmyBehaviourTree(allArmy);


        /*
        BTreeNode isNuclearAtttack = new BTreeNodeCondition((Predicate<Commander>)((commander) -> MyStrategy.player.getNextNuclearStrikeTickIndex() > 0), this);


        isNuclearAtttack.addChildNode(new BTreeAction(() -> new CommandNuclearDefence(this)));
        isNuclearAtttack.addChildNode(new BTreeAction(() -> new CommandEmpty()));


        rootNode.addChildNode(new BTreeAction(() -> new CommandCreateArmy(VehicleType.FIGHTER)));
        rootNode.addChildNode(nuclearAttackCond);
        */



        //nuclearAttackCond.addChildNode(nucAttack);
        //nuclearAttackCond.addChildNode(isNuclearAtttack);



        /*
        fighterArmy.addCommand(new CommandCreateArmy(VehicleType.HELICOPTER));
        fighterArmy.addCommand(new CommandMove(100, 400));
        fighterArmy.addCommand(new CommandMove( 400, 400));
        */

        //divisions.put(2, helicopterArmy);

        //helicopterArmy.setBehaviourTree(fighterBehaviourTree);

    }

    public void setAllArmyBehaviourTree(AllyArmy army) {
        BehaviourTree<AllyArmy> bTree = new BehaviourTree<>();
        BTreeNode root = new BTreeNodeCondition(
                (Predicate<AllyArmy>)((armyLocal) -> MyStrategy.isNuclearAttack()),
                army
        );
        root.addChildNode(new BTreeAction(() -> new CommandNuclearDefence(army)));
        root.addChildNode(new BTreeAction(() -> new CommandEmpty()));

        bTree.addRoot(root);
        army.setBehaviourTree(bTree);
    }

    public void setEmptyBehaviourTree(AllyArmy army) {
        BehaviourTree<AllyArmy> bTree = new BehaviourTree<>();
        BTreeNode root = new BTreeNodeCondition(
                (Predicate<AllyArmy>)((armyLocal) -> true),
                army
        );
        root.addChildNode(new BTreeAction(() -> new CommandAttack()));
        root.addChildNode(new BTreeAction(() -> new CommandEmpty()));
        bTree.addRoot(root);
        army.setBehaviourTree(bTree);
    }


    public void initStaticPPField () {

        int ppFieldX = (int)(strategy.getWorld().getWidth() / strategy.getGame().getTerrainWeatherMapColumnCount());
        int ppFieldY = (int)(strategy.getWorld().getHeight() / strategy.getGame().getTerrainWeatherMapRowCount());
        PPField terrainPPField = new PPField(ppFieldX, ppFieldY);
        terrainPPField.addTerrainMap(strategy.getWorld().getTerrainByCellXY());

        PPField weatherPPField = new PPField(ppFieldX, ppFieldY);
        weatherPPField.addWeatherMap(strategy.getWorld().getWeatherByCellXY());

        for (Map.Entry<Integer, AllyArmy> entry : divisions.entrySet()) {
            entry.getValue().init(terrainPPField, weatherPPField);
        }
    }

    public void logic (BattleField battleField) throws Exception {
        //run divisions logic
        //@TODO boolshit
        if (MyStrategy.canNuclearAttack() && MyStrategy.world.getTickIndex() > 100 && !(divisions.get(fighterArmyId).isHaveNuclearAttackCommand())) {
            divisions.get(fighterArmyId).addCommandToHead(new CommandNuclearAttack());
        }

        for (Map.Entry<Integer, AllyArmy> entry : divisions.entrySet()) {
            entry.getValue().run(battleField);
            entry.getValue().check();
        }
    }


    public List<AllyArmy> getArmyRunningCommands() {
        return divisions.values().stream().filter(army -> army.isRun()).collect(Collectors.toList());
    }

    public void check () {
        for (Map.Entry<Integer, AllyArmy> entry : divisions.entrySet()) {
            entry.getValue().check();
        }
    }

    public Map<Integer, AllyArmy> getDivisions() {
        return divisions;
    }

    public static int selectGroupId = -1;
}