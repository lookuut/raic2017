
import model.ActionType;
import java.util.*;


/**
 * @desc singleton class
 */

public class CommandQueue {

    private Integer tick = -1;

    private HashMap<Integer, Queue<CommandWrapper>> groupedQueueMap;
    private SortedSet<GroupPriority> prioritySortedSet;
    private Integer selectedGroup;

    private static CommandQueue instance = null;

    private CommandQueue () {
        groupedQueueMap = new HashMap<>();
        prioritySortedSet = new TreeSet<>();
        selectedGroup = -1;
    }

    public static CommandQueue getInstance() {
        if (instance == null) {
            instance = new CommandQueue();
        }

        return instance;
    }

    private static Integer priority = 0;
    public static Integer getPriority() {
        return priority++;
    }
    /**
     * @desc change priority set, last added go to priority high
     * @param group army group id
     */
    public void addPriority(Integer group) {
        prioritySortedSet.add(new GroupPriority(group, getPriority()));
    }

    public void addCommand(CommandWrapper cw) {

        if (!groupedQueueMap.containsKey(cw.group)) {
            groupedQueueMap.put(cw.group,new LinkedList<>());
        }

        groupedQueueMap.get(cw.group).add(cw);
    }

    public void run(Integer tick) {
        if (tick == this.tick) {
            return;
        }

        if (MyStrategy.player.getRemainingActionCooldownTicks() == 0) {
            Queue<CommandWrapper> queue = null;

            if (groupedQueueMap.containsKey(selectedGroup) && groupedQueueMap.get(selectedGroup).size() > 0) {//if have selected group command run it
                queue = groupedQueueMap.get(selectedGroup);
            } else {//else search commands by priority

                for (GroupPriority priority : prioritySortedSet) {
                    //if group have commands run it
                    if (groupedQueueMap.containsKey(priority.getGroup()) && groupedQueueMap.get(priority.getGroup()).size() > 0) {
                        queue = groupedQueueMap.get(priority.getGroup());
                        break;
                    }
                }
            }

            if (queue != null) {
                CommandWrapper cw = queue.peek();

                if (selectGroup(cw.group)) {
                    if (MyStrategy.world.getTickIndex() - cw.command.getRunTickIndex() >= cw.tickIndex) {
                        cw.consumer.accept(cw.command);
                        cw.command.setState(CommandStates.Run);
                        queue.poll();
                        cw.command.pinned();
                        System.out.println("Running group " + cw.group);
                    } else {
                        System.out.println("======================>");
                    }
                }
            }
        }

        this.tick = tick;
    }

    public boolean selectGroup(Integer group) {
        if (selectedGroup == group) {//group already selected or no need to be select group command id
            return true;
        }

        selectedGroup = group;

        if (group == CustomParams.noAssignGroupId) {
            return true;
        }

        MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
        MyStrategy.move.setGroup(group);
        System.out.println("Select group " + group);
        return false;
    }

}
