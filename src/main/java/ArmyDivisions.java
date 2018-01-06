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


    public Integer addArmy(Square square, Set<VehicleType> types) throws Exception {

        for (VehicleType type : types) {
            if (!armyByType.containsKey(type)) {
                armyByType.put(type , new HashSet<>());
            }
        }

        Integer armyId = armyLastId.incrementAndGet();

        ArmyAllyOrdering army = new ArmyAllyOrdering(armyId, MyStrategy.battleField, Commander.getTerrainPPField(), Commander.getWeatherPPField());
        setEmptyBehaviourTree(army);

        for (VehicleType type : types) {
            armyByType.get(type).add(armyId);
        }

        Commander.addTask(new CommanderTask(army, new CommandCreateArmy(square, (types.size() == 1 ? types.iterator().next() : null))));

        return armyId;
    }

    public void setEmptyBehaviourTree(ArmyAllyOrdering army) {
        BehaviourTree<ArmyAlly> bTree = new BehaviourTree<>();
        //CONDITION: if army have aerial and have ARRV armies and need to heal, then go to heal
        BTreeNode isGotoHealCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.timeToGoHeal() && armyLocal.isAerial() && armyByType.containsKey(VehicleType.ARRV) && armyByType.get(VehicleType.ARRV).size() > 0),
                army
        );
        //ACTION: HEAL Command
        isGotoHealCond.addChildNode(new BTreeAction(() -> new CommandHeal(this)));

        //CONDITION: is need compact?
        BTreeNode isNeedToCompact = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isNeedToCompact()),
                army
        );
        isGotoHealCond.addChildNode(isNeedToCompact);
        //ACTION: commact self
        isNeedToCompact.addChildNode(new BTreeAction(() -> new CommandCompact()));
        //CONDITION: is have enemy around
        BTreeNode isHaveEnemyCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> army.isHaveEnemyAround(CustomParams.safetyDistance)),
                army
        );
        isNeedToCompact.addChildNode(isHaveEnemyCond);
        //ACTION: if have enemy attack them
        isHaveEnemyCond.addChildNode(new BTreeAction(() -> new CommandAttack()));

        //CONDITION: if army terrain and have facilities to siege
        BTreeNode isHaveFacility = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> !army.isAerial() && MyStrategy.isHaveFacilities()),
                army
        );
        isHaveEnemyCond.addChildNode(isHaveFacility);
        //ACTION: go to siege facility
        isHaveFacility.addChildNode(new BTreeAction(() -> new CommandSiegeFacility()));
        //CONDITION: if have enemy in all map
        BTreeNode isHaveEnemyInAllMap = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> army.isHaveEnemy()),
                army
        );
        isHaveFacility.addChildNode(isHaveEnemyInAllMap);
        //ACTION: then attack enemy
        isHaveEnemyInAllMap.addChildNode(new BTreeAction(() -> new CommandAttack()));
        //ACTION: else defence
        isHaveEnemyInAllMap.addChildNode(new BTreeAction(() -> new CommandDefence()));

        bTree.addRoot(isGotoHealCond);
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

    public void clear() {
        armyList.clear();
        armyByType.clear();
    }
}
