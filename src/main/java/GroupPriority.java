public class GroupPriority implements  Comparable<GroupPriority>{
    private Integer group;
    private Integer priority;


    public GroupPriority (Integer group, Integer priority) {
        this.group = group;
        this.priority = priority;
    }

    public Integer getGroup() {
        return group;
    }

    public Integer getPriority() {
        return priority;
    }

    public int hashCode() {
        return group.hashCode();
    }

    @Override
    public int compareTo(GroupPriority groupPriority) {
        return (priority - groupPriority.getPriority());
    }
}
