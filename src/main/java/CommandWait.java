
public class CommandWait extends Command {

    private Integer waitTick;
    private Integer runTick;

    public CommandWait(Integer waitTick) {
        this.waitTick = waitTick;
    }

    public boolean check (ArmyAllyOrdering army) {
        if (MyStrategy.world.getTickIndex() - runTick >= waitTick) {
            setState(CommandStates.Complete);
            return true;
        }

        return false;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {}

    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            runTick = MyStrategy.world.getTickIndex();
            setState(CommandStates.Run);
        }
    }
    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
