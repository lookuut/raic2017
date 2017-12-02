
public class CommandHeal extends Command {
    private Army arrv;
    private CommandMove move;

    public CommandHeal(Army arrv) {
        super();
        this.arrv = arrv;
    }

    public boolean check (ArmyAllyOrdering army) {
        boolean check = move.check(army);
        if (check) {
            setState(CommandStates.Complete);
        }

        return check;
    }


    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            Point2D moveVector = arrv.getForm().getAvgPoint().subtract(army.getForm().getAvgPoint());
            move = new CommandMove(moveVector);
            move.run(army);
            super.run(army);
        }
    }

    @Override
    public void pinned(){
        move.pinned();
    }

    @Override
    public void processing(SmartVehicle vehicle) {
        move.processing(vehicle);
    }
}
