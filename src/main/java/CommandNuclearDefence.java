import model.ActionType;

import java.util.function.Consumer;

public class CommandNuclearDefence extends  Command {
    protected double posX;
    protected double posY;

    public CommandNuclearDefence(AllyArmy army, double posX, double posY) {
        super(army);
        this.posX = posX;
        this.posY = posY;

        this.setState(CommandStates.New);
        this.army = army;

        Consumer<Command> funcMove = (command) -> {
            double localX = this.posX - command.getArmy().getAvgX();
            double localY = this.posY - command.getArmy().getAvgY();

            MyStrategy.move.setAction(ActionType.SCALE);
            MyStrategy.move.setX(localX);
            MyStrategy.move.setY(localY);
        };

        queue.add(selectArmy);
        queue.add(funcMove);
    }

}
