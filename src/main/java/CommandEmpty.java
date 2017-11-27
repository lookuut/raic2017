public class CommandEmpty extends Command {
    public CommandEmpty() {
        super();
    }

    public boolean check (AllyArmy army) {
        setState(CommandStates.Complete);
        return true;
    }

    @Override
    public void runned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }

}
