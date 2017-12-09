
import model.ActionType;
import java.util.*;


/**
 * @desc singleton class
 */

public class CommandQueue {

    private Integer tick = -1;

    private HashMap<Integer, Queue<CommandWrapper>> groupedQueueMap;
    private SortedSet<GroupPriority> prioritySortedSet;
    private Integer selectedArmyId;

    private static CommandQueue instance = null;

    private CommandQueue () {
        groupedQueueMap = new HashMap<>();
        prioritySortedSet = new TreeSet<>();
        selectedArmyId = -1;
    }

    public static CommandQueue getInstance() {
        if (instance == null) {
            instance = new CommandQueue();
        }

        return instance;
    }

    /**
     * @desc change priority set, last added go to priority high
     * @param armyId army armyId id
     */
    public void addPriority(Integer armyId, Integer priority) {
        prioritySortedSet.add(new GroupPriority(armyId, priority));
    }

    public void addCommand(CommandWrapper cw) {

        if (!groupedQueueMap.containsKey(cw.armyId)) {
            groupedQueueMap.put(cw.armyId, new LinkedList<>());
        }

        groupedQueueMap.get(cw.armyId).add(cw);
    }

    public void run(Integer tick) {
        if (tick == this.tick) {
            return;
        }

        if (MyStrategy.player.getRemainingActionCooldownTicks() == 0) {
            Queue<CommandWrapper> queue = null;

            if (groupedQueueMap.containsKey(selectedArmyId) && groupedQueueMap.get(selectedArmyId).size() > 0) {//if have selected armyId command run it
                queue = groupedQueueMap.get(selectedArmyId);
            } else {//else search commands by priority

                for (GroupPriority priority : prioritySortedSet) {
                    //if armyId have commands run it
                    if (groupedQueueMap.containsKey(priority.getArmyId()) && groupedQueueMap.get(priority.getArmyId()).size() > 0) {
                        queue = groupedQueueMap.get(priority.getArmyId());
                        break;
                    }
                }
            }

            if (queue != null) {
                CommandWrapper cw = queue.peek();

                if (selectGroup(cw.armyId)) {
                    if (MyStrategy.world.getTickIndex() - cw.command.getRunTickIndex() >= cw.tickIndex) {
                        cw.consumer.accept(cw.command);
                        cw.command.setState(CommandStates.Run);
                        queue.poll();
                        cw.command.pinned();
                        System.out.println("Running armyId " + cw.armyId);
                    } else {
                        System.out.println("======================>");
                    }
                }
            }
        }

        this.tick = tick;
    }

    public boolean selectGroup(Integer armyId) {
        if (selectedArmyId == armyId) {//armyId already selected or no need to be select armyId command id
            return true;
        }

        selectedArmyId = armyId;

        if (armyId == CustomParams.noAssignGroupId) {
            return true;
        }

        MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
        MyStrategy.move.setGroup(armyId);
        System.out.println("Select armyId " + armyId);
        return false;
    }

}
