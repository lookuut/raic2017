
import model.ActionType;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * @desc singleton class
 */

public class CommandQueue {

    private Integer tick = -1;
    private Integer selectedArmyId;

    private Map<CommandPriority, Queue<CommandWrapper>> groupedQueueMap;
    private Map<CommandPriority, Integer> priorityCounter;
    private Integer commandCounter = 0;
    private static CommandQueue instance = null;

    private CommandQueue () {

        priorityCounter = new HashMap<>();
        groupedQueueMap = new HashMap<>();
        for (CommandPriority priority : CommandPriority.values()) {
            priorityCounter.put(priority, 0);
            groupedQueueMap.put(priority, new LinkedList<>());
        }

        selectedArmyId = -1;
    }

    public static CommandQueue getInstance() {
        if (instance == null) {
            instance = new CommandQueue();
        }

        return instance;
    }

    public void addCommand(CommandWrapper cw) {
        groupedQueueMap.get(cw.getPriority()).add(cw);
    }
    private CommandWrapper runningCommands;


    private Queue<CommandWrapper> pickUpNewQueue () {
        Queue<CommandWrapper> queue = null;

        if (priorityCounter.get(CommandPriority.Low) < 2 && groupedQueueMap.get(CommandPriority.Low).size() > 0) {
            queue = groupedQueueMap.get(CommandPriority.Low);
        } else if (priorityCounter.get(CommandPriority.Middle) < 4 && groupedQueueMap.get(CommandPriority.Middle).size() > 0) {
            queue = groupedQueueMap.get(CommandPriority.Middle);
        } else if (groupedQueueMap.get(CommandPriority.High).size() > 0) {
            queue = groupedQueueMap.get(CommandPriority.High);
        } else if (2 * (MyStrategy.game.getBaseActionCount() - getCommandCounter()) >= tick % MyStrategy.game.getActionDetectionInterval()) {
            for (CommandPriority priority : CommandPriority.values()) {
                if (groupedQueueMap.get(priority).size() > 0)  {
                    queue = groupedQueueMap.get(priority);
                    break;
                }
            }
        }

        return queue;
    }
    public void run(Integer tick) {
        if (tick == this.tick) {
            return;
        }

        if (MyStrategy.player.getRemainingActionCooldownTicks() == 0) {

            if (Commander.getInstance().isThereEnemyAround(CustomParams.safetyDistance) &&
                    MyStrategy.mayEnemyAttackNuclearSoon() &&
                    MyStrategy.game.getBaseActionCount() - getCommandCounter() <= 2) {//if nuclear attack is possibl need two action to defence

            }

            if (runningCommands == null || runningCommands.getQueue().size() == 0) {
                Queue<CommandWrapper> queue = pickUpNewQueue();
                if (queue != null) {
                    runningCommands = queue.poll();
                }
            }


            if (runningCommands != null && runningCommands.getQueue().size() > 0) {
                if (selectGroup(runningCommands.getArmyId())) {
                    if (MyStrategy.world.getTickIndex() - runningCommands.getCommand().getRunTickIndex() >= runningCommands.getTickIndex()) {
                        runningCommands.getQueue().poll().accept(runningCommands.getCommand());
                        runningCommands.getCommand().setState(CommandStates.Run);
                        runningCommands.getCommand().pinned();
                        incrementGroupCounter(runningCommands);
                        incrementCommandCounter();
                        //System.out.println("Running armyId " + runningCommands.getArmyId());
                    } else {
                        //System.out.println("======================>");
                    }
                }
            }
        }

        this.tick = tick;
        if (tick % MyStrategy.game.getActionDetectionInterval() == 0) {
            for (CommandPriority priority : CommandPriority.values()) {
                priorityCounter.put(priority, 0);
            }

            refreshCommandCounter();
        }
    }


    public void incrementGroupCounter(CommandWrapper cw) {
        priorityCounter.put(cw.getPriority(), priorityCounter.get(cw.getPriority()) + 1);
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
        incrementCommandCounter();
        System.out.println("Select armyId " + armyId);
        return false;
    }

    public void incrementCommandCounter() {
        commandCounter++;
    }
    public Integer getCommandCounter() {
        return commandCounter;
    }
    public void refreshCommandCounter() {
        commandCounter = 0;
    }

}
