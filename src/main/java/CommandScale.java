import model.ActionType;

import java.util.function.Consumer;

public class CommandScale extends Command {
    protected Integer scaleStartedIndex;
    public CommandScale(AllyArmy army, double x, double y, double scale) {
        super(army);

        Consumer<Command> commandScale = (command) -> {
            MyStrategy.move.setAction(ActionType.SCALE);
            MyStrategy.move.setX(x);
            MyStrategy.move.setY(y);
            MyStrategy.move.setFactor(scale);
        };

        queue.add(new CommandWrapper(selectArmy, this, -1));
        queue.add(new CommandWrapper(commandScale, this, -1));
    }

    public void run () {
        scaleStartedIndex = MyStrategy.world.getTickIndex();
        super.run();
    }

    public boolean check () {
        if (getState() == CommandStates.Run) {
            if (MyStrategy.world.getTickIndex() - scaleStartedIndex > MyStrategy.game.getTacticalNuclearStrikeDelay()) {
                setState(CommandStates.Complete);
                return true;
            }
        }

        return false;
    }
}
