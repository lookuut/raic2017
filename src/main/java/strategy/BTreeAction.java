package strategy;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class BTreeAction extends BTreeNode {

    protected Supplier<Command> commandConsumer;

    public BTreeAction(Supplier<Command> commandSupplier) {
        this.commandConsumer = commandSupplier;

    }

    /**
     * @TODO bad style, rewrite it
     * @return
     */
    public List<Command> getCommand() {
        return Arrays.asList(commandConsumer.get());
    }

}
