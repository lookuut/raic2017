
import java.util.function.Consumer;

public class CommandWrapper {
    public Consumer<Command> consumer;
    public Command command;

    public CommandWrapper(Consumer<Command> consumer, Command command) {
        this.consumer = consumer;
        this.command = command;
    }
}