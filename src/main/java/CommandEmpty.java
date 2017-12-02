
public class CommandEmpty extends Command {

    public CommandEmpty() {
    }

    public boolean check (ArmyAllyOrdering army) {
        setState(CommandStates.Complete);
        return true;
    }


    public void run(ArmyAllyOrdering army) throws Exception {
        if (isNew()) {
            super.run(army);
        }
    }
    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }

}
