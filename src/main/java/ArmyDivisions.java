import java.util.*;

import model.VehicleType;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArmyDivisions {


    private static final AtomicInteger armyLastId = new AtomicInteger(1);
    private Map<VehicleType, HashSet<Integer>> armyByType;
    private Map<Integer, ArmyAllyOrdering> armyList;

    public ArmyDivisions() {
        armyList = new HashMap<>();
        armyByType = new HashMap<>();
    }

    public Integer addArmy(Square square, VehicleType type, TerrainPPField terrainPPField, WeatherPPField weatherPPField) throws Exception {
        if (!armyByType.containsKey(type)) {
            armyByType.put(type , new HashSet<>());
        }

        Integer armyId = armyLastId.incrementAndGet();

        ArmyAllyOrdering army = new ArmyAllyOrdering(armyId, MyStrategy.battleField, terrainPPField, weatherPPField);
        setEmptyBehaviourTree(army);

        armyByType.get(type).add(armyId);
        Commander.addTask(new CommanderTask(army, new CommandCreateArmy(square, type)));

        Integer priority = armyId + 5;
        //@TODO configured options, brain it
        if (type == VehicleType.FIGHTER) {
            priority = 1;
        }
        if (type == VehicleType.HELICOPTER) {
            priority = 2;
        }

        CommandQueue.getInstance().addPriority(armyId, priority);
        return armyId;
    }

    public void setEmptyBehaviourTree(ArmyAllyOrdering army) {
        BehaviourTree<ArmyAlly> bTree = new BehaviourTree<>();

        BTreeNode isHaveEnemyCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.timeToGoHeal() && false && armyByType.get(VehicleType.ARRV).size() > 0),
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

    public void addCondifuredArmy(ArmyAllyOrdering army) {
        armyList.put(army.getGroupId(), army);
    }
}
