
import model.VehicleType;

import java.util.*;
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

        //CONDITION: nuclear attack cond
        BTreeNode isCanNuclearAttack = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> Commander.getInstance().isCanNuclearAttack(armyLocal)) ,
                army
        );

        //CONDITION: nuclear attack cond
        BTreeNode canNuclearAttackInDefence = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> Commander.getInstance().isCanNuclearAttack(armyLocal)) ,
                army
        );

        //CONDITION: goto heal ?
        BTreeNode isNeedToHeal = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.timeToGoHeal() && armyLocal.isAerial() && armyByType.containsKey(VehicleType.ARRV) && armyByType.get(VehicleType.ARRV).size() > 0),
                army
        );;
        //CONDITION: is need compact?
        BTreeNode isNeedToCompact = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isNeedToCompact()),
                army
        );

        BTreeNode isNeedToBeforeAttackCompact = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isNeedToCompact()),
                army
        );
        //CONDITION: is have enemy around
        BTreeNode isHaveEnemyCond = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isHaveTargetArmyAround(CustomParams.safetyDistance)),
                army
        );

        //CONDITION: is have enemy around
        BTreeNode isEnemyNear = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isHaveEnemyAround(CustomParams.doCompactDistance)),
                army
        );


        //CONDITION: if army terrain and have facilities to siege
        BTreeNode isHaveFacility = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> !armyLocal.isAerial() && MyStrategy.isHaveFacilitiesToSiege()),
                army
        );

        //CONDITION: if have enemy in all map
        BTreeNode isHaveEnemyInAllMap = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isHaveEnemy()),
                army
        );

        //CONDITION: can 
        BTreeNode isHaveEnemyWeakness = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isSafetyAround()),
                army
        );

        //CONDITION: can
        BTreeNode isSafetyAround = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isSafetyAround()),
                army
        );

        //CONDITION: can
        BTreeNode isNeedToTurnArmy = new BTreeNodeCondition(
                (Predicate<ArmyAlly>)((armyLocal) -> armyLocal.isNeedToTurnArmyToEnemy()),
                army
        );

        //actions
        BTreeNode actionHeal = new BTreeAction(() -> new CommandHeal(this));
        BTreeNode actionNuclearAttack = new BTreeAction(() -> new CommandNuclearAttack());
        BTreeNode actionCompact = new BTreeAction(() -> new CommandCompact());
        BTreeNode actionAttack = new BTreeAction(() -> new CommandAttack());
        BTreeNode actionSiegeFacility = new BTreeAction(() -> new CommandSiegeFacility());
        BTreeNode actionCommandDefence  = new BTreeAction(() -> new CommandDefence());
        BTreeNode actionCommandRotate  = new BTreeAction(() -> new CommandRotate(army));

        isNeedToHeal.setTrueNode(actionHeal);
        isNeedToHeal.setFalseNode(isHaveEnemyCond);

            isHaveEnemyCond.setTrueNode(isCanNuclearAttack);
                isCanNuclearAttack.setTrueNode(actionNuclearAttack);
                isCanNuclearAttack.setFalseNode(isHaveEnemyWeakness);
                    isHaveEnemyWeakness.setTrueNode(isEnemyNear);
                        isEnemyNear.setTrueNode(isNeedToTurnArmy);
                            isNeedToTurnArmy.setTrueNode(actionCommandRotate);
                            isNeedToTurnArmy.setFalseNode(actionAttack);
                        isEnemyNear.setFalseNode(isNeedToBeforeAttackCompact);
                            isNeedToBeforeAttackCompact.setTrueNode(actionCompact);
                            isNeedToBeforeAttackCompact.setFalseNode(actionAttack);
                    isHaveEnemyWeakness.setFalseNode(actionCommandDefence);
            isHaveEnemyCond.setFalseNode(isSafetyAround);
                isSafetyAround.setTrueNode(isNeedToCompact);

                    isNeedToCompact.setTrueNode(actionCompact);
                    isNeedToCompact.setFalseNode(isHaveFacility);

                        isHaveFacility.setTrueNode(actionSiegeFacility);
                        isHaveFacility.setFalseNode(isHaveEnemyInAllMap);

                            isHaveEnemyInAllMap.setTrueNode(actionAttack);
                            isHaveEnemyInAllMap.setFalseNode(actionCommandDefence);
                isSafetyAround.setFalseNode(canNuclearAttackInDefence);
                    canNuclearAttackInDefence.setTrueNode(actionNuclearAttack);
                    canNuclearAttackInDefence.setFalseNode(actionCommandDefence);
        bTree.addRoot(isNeedToHeal);
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
        return armyList.entrySet().stream().filter(entry -> armyKeys.contains(entry.getKey()) && entry.getValue().isAlive()).map(entry -> entry.getValue()).collect(Collectors.toList());
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
