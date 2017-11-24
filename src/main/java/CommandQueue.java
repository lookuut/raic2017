
import java.util.LinkedList;
import java.util.Queue;

public class CommandQueue {

    protected Integer currentTick = 0;
    protected Queue<CommandWrapper> queue;
    protected CommandWrapper prevCommand;

    private static CommandQueue instance = null;

    private CommandQueue () {
        queue = new LinkedList<>();
    }

    public static CommandQueue getInstance() {
        if (instance == null) {
            instance = new CommandQueue();
        }

        return instance;
    }

    public void addCommand(CommandWrapper cw) {
        this.queue.add(cw);
    }

    public void run(Integer tick) {
        if (tick == this.currentTick) {
            return;
        }
        this.prevCommand = null;

        if (this.queue.size() > 0 && MyStrategy.player.getRemainingActionCooldownTicks() == 0) {
            CommandWrapper cw = this.queue.peek();
            if (MyStrategy.world.getTickIndex() - cw.command.getRunTickIndex() >= cw.tickIndex) {
                cw.consumer.accept(cw.command);
                cw.command.setState(CommandStates.Run);
                this.prevCommand = cw;
                this.queue.poll();
                cw.command.runned();
            }
        }

        this.currentTick = tick;
    }

    public int size() {
        return this.queue.size();
    }
}
