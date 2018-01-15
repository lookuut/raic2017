package strategy;

import model.ActionType;
import model.Player;

public class CommandNuclearDefence extends Command {
    protected Integer attackTick;
    protected double attackX;
    protected double attackY;

    public CommandNuclearDefence() {
        attackTick = null;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void run (ArmyAllyOrdering army) throws Exception {
        if (isNew()) {

            Player player = MyStrategy.nuclearAttack();
            if (player == null) {//nuclear attack already was passed :( @TODO boolshit code
                return;
            }
            attackTick = player.getNextNuclearStrikeTickIndex();
            MyStrategy.move.setAction(ActionType.CLEAR_AND_SELECT);
            attackX = player.getNextNuclearStrikeX();
            attackY = player.getNextNuclearStrikeY();
            Point2D attackPoint = new Point2D(attackX, attackY);
            for (ArmyAllyOrdering armyLocal : Commander.getInstance().getDivisions().getArmies().values()) {
                if (armyLocal.getForm().isPointInDistance(attackPoint, MyStrategy.game.getTacticalNuclearStrikeRadius())) {
                    CommandScale scale = new CommandScale(MyStrategy.game.getTacticalNuclearStrikeDelay(), attackPoint, 10);
                    scale.setPriority(CommandPriority.High);
                    scale.run(armyLocal);
                    armyLocal.addCommand(scale);
                    CommandScale compact = new CommandScale(MyStrategy.game.getTacticalNuclearStrikeDelay(), attackPoint, 0.1);
                    compact.setPriority(CommandPriority.High);
                    armyLocal.addCommand(compact);
                    break;
                }
            }

            super.run(army);
        }
    }

    public boolean check (ArmyAllyOrdering army) {
        if (getState() == CommandStates.Run && MyStrategy.world.getTickIndex() >= this.attackTick + MyStrategy.game.getTacticalNuclearStrikeDelay()) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }


    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
