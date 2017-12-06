import java.util.*;

import model.VehicleType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArmyDivisions {


    private static final AtomicInteger armyLastId = new AtomicInteger(0);

    private Map<VehicleType, HashSet<Integer>> armyByType;
    private Map<Integer, ArmyAllyOrdering> armyList;

    public ArmyDivisions() {
        armyList = new HashMap<>();
        armyByType = new HashMap<>();
    }

    public Integer addArmy(Square square, VehicleType type) throws Exception {
        if (!armyByType.containsKey(type)) {
            armyByType.put(type , new HashSet<>());
        }

        Integer armyId = armyLastId.incrementAndGet();
        ArmyAllyOrdering army = new ArmyAllyOrdering(armyId);

        int ppFieldX = (int)(MyStrategy.world.getWidth() / MyStrategy.game.getTerrainWeatherMapColumnCount());
        int ppFieldY = (int)(MyStrategy.world.getHeight() / MyStrategy.game.getTerrainWeatherMapRowCount());
        TerrainPPField terrainPPField = new TerrainPPField(ppFieldX, ppFieldY);
        terrainPPField.addTerrainMap(MyStrategy.world.getTerrainByCellXY());

        WeatherPPField weatherPPField = new WeatherPPField(ppFieldX, ppFieldY);
        weatherPPField.addWeatherMap(MyStrategy.getWeatherMap());

        army.init(terrainPPField, weatherPPField);

        army.addCommand(new CommandCreateArmy(square, type));
        setEmptyBehaviourTree(army);
        armyList.put(armyId, army);
        armyByType.get(type).add(armyId);

        //@TODO configured options, brain it
        CommandQueue.getInstance().addPriority(armyId);
        return armyId;
    }

    public void setEmptyBehaviourTree(ArmyAllyOrdering army) {
        BehaviourTree<ArmyAlly> bTree = new BehaviourTree<>();

        BTreeNode isHaveEnemyCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.timeToGoHeal() && armyByType.get(VehicleType.ARRV).size() > 0),
                army
        );

        isHaveEnemyCond.addChildNode(new BTreeAction(() -> new CommandHeal(this)));

        BTreeNode isGotoHealCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> army.isHaveEnemy()),
                army
        );

        isHaveEnemyCond.addChildNode(isGotoHealCond);
        isGotoHealCond.addChildNode(new BTreeAction(() -> new CommandAttack()));

        BTreeNode isHaveFacility = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> !army.isAerial() && MyStrategy.isHaveFacilities()),
                army
        );
        isGotoHealCond.addChildNode(isHaveFacility);
        isHaveFacility.addChildNode(new BTreeAction(() -> new CommandSiegeFacility()));
        isHaveFacility.addChildNode(new BTreeAction(() -> new CommandDefence()));

        bTree.addRoot(isHaveEnemyCond);
        army.setBehaviourTree(bTree);
    }

    public Collection<ArmyAllyOrdering> getArmyList() {
        return armyList.values();
    }

    public Map<Integer, ArmyAllyOrdering> getArmies() {
        return armyList;
    }

    public Collection<ArmyAllyOrdering> getArmyList(VehicleType type) {
        Set armyKeys = armyByType.get(type);
        if (armyKeys == null || armyKeys.size() == 0) {
            return null;
        }
        return armyList.entrySet().stream().filter(entry -> armyKeys.contains(entry.getKey()) && entry.getValue().isArmyAlive()).map(entry -> entry.getValue()).collect(Collectors.toList());
    }

    public void removeArmy(ArmyAllyOrdering army) {
        for (VehicleType vehicleType : army.getVehiclesType()) {
            armyByType.get(vehicleType).remove(army.getGroupId());
        }
    }
}
