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
    public void prepare(ArmyAllyOrdering army) throws Exception {
    }

    private List<Long> defineAttackedSquareAngleVehicles(Collection<ArmyAllyOrdering> armies) {
        List<Long> result = new ArrayList<>();

        Long leftVehicleId = 0l;
        Long rightVehicleId = 0l;;

        Long topVehicleId = 0l;;
        Long bottomVehicleId = 0l;;

        double left = MyStrategy.world.getWidth();
        double right = 0;

        double top = MyStrategy.world.getHeight();
        double bottom = 0;

        for (ArmyAllyOrdering armyLocal : armies) {
            if (armyLocal.isAlive()) {
                for (SmartVehicle vehicle : armyLocal.getVehicles().values()) {
                    if (vehicle.getX() < left) {
                        left = vehicle.getX();
                        leftVehicleId = vehicle.getId();
                    }

                    if (vehicle.getX() > right) {
                        right = vehicle.getX();
                        rightVehicleId = vehicle.getId();
                    }

                    if (vehicle.getY() < top) {
                        top = vehicle.getY();
                        topVehicleId = vehicle.getId();
                    }

                    if (vehicle.getY() > bottom) {
                        bottom = vehicle.getY();
                        bottomVehicleId = vehicle.getId();
                    }
                }
            }
        }
        result.add(leftVehicleId);
        result.add(rightVehicleId);
        result.add(topVehicleId);
        result.add(bottomVehicleId);
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
                if (armyLocal.getForm().isPointInDistance(attackPoint, MyStrategy.game.getTacticalNuclearStrikeRadius()) && armyLocal.isAlive()) {
                    armyLocal.getForm().update(armyLocal.getVehicles());
                    armyLocal.lock();
                    attackedArmies.add(armyLocal);
                }
            }

            if (attackedArmies.size() > 0) {
                List<Long> attackedSquare = defineAttackedSquareAngleVehicles(attackedArmies);
                CommandWrapper cw = new CommandWrapper( this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId, CommandPriority.High);


                Consumer<Command> commandScale = (command) -> {
                    MyStrategy.move.setAction(ActionType.SCALE);
                    MyStrategy.move.setX(attackX);
                    MyStrategy.move.setY(attackY);
                    MyStrategy.move.setFactor(CustomParams.nuclearAttackDefenceScaleFactor);
                    scaleTick = MyStrategy.world.getTickIndex();
                };

                cw.addCommand(Command.selectSquare(attackedSquare));
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

            List<Long> attackedSquare = defineAttackedSquareAngleVehicles(attackedArmies);
            CommandWrapper cw = new CommandWrapper( this, CustomParams.runImmediatelyTick, CustomParams.noAssignGroupId, CommandPriority.High);


            Consumer<Command> commandScale = (command) -> {
                MyStrategy.move.setAction(ActionType.SCALE);
                MyStrategy.move.setX(attackX);
                MyStrategy.move.setY(attackY);
                MyStrategy.move.setFactor(CustomParams.armyScaleFactor);
                compactCompleteTick = MyStrategy.world.getTickIndex() + (MyStrategy.world.getTickIndex() - scaleTick);
            };

            cw.addCommand(Command.selectSquare(attackedSquare));
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
