import model.VehicleType;

import java.util.*;

import java.util.function.Predicate;
import java.util.stream.Collectors;

class Commander {
    protected Map<Integer, ArmyAllyOrdering> divisions;
    protected MyStrategy strategy;
    protected BehaviourTree<ArmyAlly> behaviourTree;
    protected Queue<BTreeAction> activeActions;
    static private Integer navigateNuclearTick = -1;
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

        fighterArmy.setBehaviourTree(fighterBehaviourTree);

        this.setEmptyBehaviourTree(fighterArmy);
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
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.onDanger()),
                army
        );
        root.addChildNode(new BTreeAction(() -> new CommandDefence()));
        root.addChildNode(new BTreeAction(() -> new CommandEmpty()));//on idea go to siege base
        bTree.addRoot(root);
        army.setBehaviourTree(bTree);
    }

    public void setEmptyBehaviourTree(ArmyAllyOrdering army) {
        BehaviourTree<ArmyAlly> bTree = new BehaviourTree<>();
        BTreeNode root = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isArmyAlive() && armyLocal.isAerial() && armyLocal.getAvgDurability() < CustomParams.minAvgDurability),
                army
        );
        root.addChildNode(new BTreeAction(() -> new CommandHeal(divisions.get(CustomParams.arrvArmyId))));
        root.addChildNode(new BTreeAction(() -> new CommandAttack()));
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
                if (MyStrategy.canNuclearAttack()) {
                    nuclearAttack(entry.getValue());
                }

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

    public void nuclearAttack (ArmyAllyOrdering army) {
        try {
            for (NuclearAttackPoint attackPoint : MyStrategy.enemyField.getNuclearAttackPointsRating()) {

                if (army.getForm().isPointInDistance(attackPoint.getPoint(), MyStrategy.game.getTacticalNuclearStrikeRadius())) {//point in allow distance need cabum
                    SmartVehicle vehicle = army.getNearestVehicle(attackPoint.getPoint());
                    Point2D fromPoint = vehicle.getPoint();
                    Point2D targetVector = attackPoint.getPoint().subtract(fromPoint);
                    double visionRange = vehicle.getMinVisionRange();

                    if (army.getRunningCommand() instanceof  CommandMove) {
                        CommandMove command = ((CommandMove) army.getRunningCommand());
                        Point2D moveTargetVector = command.getTargetVector();
                        double angle = moveTargetVector.angle(targetVector);
                        if (angle >= 90 && angle < 270) {
                            fromPoint = vehicle.getVehiclePointAtTick(moveTargetVector.normalize(), Math.min(MyStrategy.game.getTacticalNuclearStrikeDelay(),command.getMaxRunnableTick()));
                        }
                    }

                    Point2D targetPoint = attackPoint.getPoint();
                    Point2D targetPointDirection = attackPoint.getPoint().subtract(fromPoint);

                    if (targetPointDirection.magnitude() > visionRange) {
                        targetPoint = targetPointDirection.multiply(visionRange / targetPointDirection.magnitude()).add(fromPoint);
                    }

                    navigateNuclearTick = MyStrategy.world.getTickIndex();
                    new CommandNuclearAttack(vehicle, targetPoint).run(army);
                    break;
                }
            }
        } catch (Exception e)  {
            e.printStackTrace();
        }
    }

    static public Integer getNavigateNuclearTick() {
        return navigateNuclearTick;
    }
}