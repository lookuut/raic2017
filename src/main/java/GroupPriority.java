public class GroupPriority implements  Comparable<GroupPriority>{
    private Integer armyId;
    private Integer priority;


    public GroupPriority (Integer armyId, Integer priority) {
        this.armyId = armyId;
        this.priority = priority;
    }

    public Integer getArmyId() {
        return armyId;
    }

    public Integer getPriority() {
        return priority;
    }

    public int hashCode() {
        return armyId.hashCode();
    }

    @Override
    public int compareTo(GroupPriority groupPriority) {
        return priority == groupPriority.getPriority() ? armyId - groupPriority.armyId : (priority - groupPriority.getPriority());
    }
}
