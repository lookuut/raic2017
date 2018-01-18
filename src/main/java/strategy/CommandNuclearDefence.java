package strategy;

import model.ActionType;
import model.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class CommandNuclearDefence extends Command {
    private Integer compactCompleteTick;
    private Integer scaleTick;

    protected Integer attackTick;
    protected double attackX;
    protected double attackY;
    private List<ArmyAllyOrdering> attackedArmies;
    private boolean isNeedCompact;
    private boolean isNeedProtect;
    public CommandNuclearDefence() {
        attackTick = null;
        isNeedCompact = false;
        isNeedProtect = true;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    private List<Point2D> defineAttackedSquare(Collection<ArmyAllyOrdering> armies) {
        List<Point2D> result = new ArrayList<>();
        Point2D attackMaxPoint = new Point2D(0,0);
        Point2D attackMinPoint = new Point2D(MyStrategy.world.getWidth(),MyStrategy.world.getHeight());

        for (ArmyAllyOrdering armyLocal : armies) {
                armyLocal.getForm().update(armyLocal.getVehicles());

                if (attackMinPoint.getX() > armyLocal.getForm().getMinPoint().getX()) {
                    attackMinPoint.setX(armyLocal.getForm().getMinPoint().getX());
                }

                if (attackMinPoint.getY() > armyLocal.getForm().getMinPoint().getY()) {
                    attackMinPoint.setY(armyLocal.getForm().getMinPoint().getY());
                }

                if (attackMaxPoint.getX() < armyLocal.getForm().getMaxPoint().getX()) {
                    attackMaxPoint.setX(armyLocal.getForm().getMaxPoint().getX());
                }

                if (attackMaxPoint.getY() < armyLocal.getForm().getMaxPoint().getY()) {
                    attackMaxPoint.setY(armyLocal.getForm().getMaxPoint().getY());
                }
        }
        result.add(attackMinPoint);
        result.add(attackMaxPoint);
        return result;
    }

    public void run (ArmyAllyOrdering army) throws Exception {
        if (isNeedProtect) {
            compactCompleteTick = MyStrategy.world.getTickCount();
            attackedArmies = new ArrayList<>();
            Player player = MyStrategy.nuclearAttack();

            if (player == null) {
                return;
            }

            attackTick = player.getNextNuclearStrikeTickIndex();

            attackX = player.getNextNuclearStrikeX();
            attackY = player.getNextNuclearStrikeY();

            Point2D attackPoint = new Point2D(attackX, attackY);

            for (ArmyAllyOrdering armyLocal : Commander.getInstance().getDivisions().getArmies().values()) {
                if (armyLocal.getForm().isPointInDistance(attackPoint, MyStrategy.game.getTacticalNuclearStrikeRadius())) {
                    armyLocal.getForm().update(armyLocal.getVehicles());
                    armyLocal.lock();
                    attackedArmies.add(armyLocal);
                }
            }

            if (attackedArmies.size() > 0) {
                List<Point2D> attackedSquare = defineAttackedSquare(attackedArmies);
                CommandWrapper cw = new CommandWrapper( this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId, CommandPriority.High);


                Consumer<Command> commandScale = (command) -> {
                    MyStrategy.move.setAction(ActionType.SCALE);
                    MyStrategy.move.setX(attackX);
                    MyStrategy.move.setY(attackY);
                    MyStrategy.move.setFactor(CustomParams.nuclearAttackDefenceScaleFactor);
                    scaleTick = MyStrategy.world.getTickIndex();
                };

                cw.addCommand(Command.selectSquare(attackedSquare.get(0), attackedSquare.get(1)));
                cw.addCommand(commandScale);
                CommandQueue.getInstance().addCommand(cw);
            }
            isNeedCompact = true;
            isNeedProtect = false;
        } else if (isRun() && isNeedCompact &&
                (
                        MyStrategy.world.getTickIndex() >= attackTick
                                ||
                                MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeVehicleId() == -1
                )) {//nuclear attack complete, need scale

            List<Point2D> attackedSquare = defineAttackedSquare(attackedArmies);
            CommandWrapper cw = new CommandWrapper( this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId, CommandPriority.High);


            Consumer<Command> commandScale = (command) -> {
                MyStrategy.move.setAction(ActionType.SCALE);
                MyStrategy.move.setX(attackX);
                MyStrategy.move.setY(attackY);
                MyStrategy.move.setFactor(CustomParams.armyScaleFactor);
                compactCompleteTick = MyStrategy.world.getTickIndex() + (MyStrategy.world.getTickIndex() - scaleTick);
            };

            cw.addCommand(Command.selectSquare(attackedSquare.get(0), attackedSquare.get(1)));
            cw.addCommand(commandScale);
            CommandQueue.getInstance().addCommand(cw);


            isNeedCompact = false;
        }
        super.run(army);
    }

    public boolean check (ArmyAllyOrdering army) {
        if (getState() == CommandStates.Run && MyStrategy.world.getTickIndex() >= compactCompleteTick)
        {
            for (ArmyAllyOrdering armyAllyOrdering : attackedArmies) {
                armyAllyOrdering.unlock();
            }
            setState(CommandStates.Complete);
            isNeedProtect = true;
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
