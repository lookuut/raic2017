
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

        if (this.size() > 0) {
            CommandWrapper cw = this.queue.poll();
            cw.consumer.accept(cw.command);
            cw.command.setState(CommandStates.Run);
            this.prevCommand = cw;
        }

        this.currentTick = tick;
    }

    public void prevCommandRunResult(SmartVehicle vehicle) {
        if (prevCommand != null) {
            prevCommand.command.result(vehicle);
        }
    }

    public int size() {
        return this.queue.size();
    }
}
