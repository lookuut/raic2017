import com.sun.tools.doclets.internal.toolkit.util.ClassUseMapper;

public class CommandSiegeFacility extends Command {

    private SmartFacility facility;

    public CommandSiegeFacility() {

    }

    public Command prepare(ArmyAllyOrdering army) throws Exception {

        army.getForm().recalc(army.getVehicles());

        facility = MyStrategy.commanderFacility.getFacilityToSiege(army);
        if (facility == null) {//no facility to siege
            return null;
        }
        facility.addGoingToFacilityArmy(army);
        Point2D targetVec = facility.getFacilityCentre().subtract(army.getForm().getAvgPoint());

        if (targetVec.magnitude() < CustomParams.onFacilityEps) {
            setState(CommandStates.Complete);
            return null;
        }

        TargetPoint point = new TargetPoint();
        point.maxDamageValue = 100;
        point.vector = targetVec;

        return army.pathFinder(new CommandMove(targetVec), point);
    }

    public boolean check (ArmyAllyOrdering army) {
        setState(CommandStates.Complete);
        return true;
    }

    public void run(ArmyAllyOrdering army) throws Exception {
        setState(CommandStates.Complete);
    }
    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }

}
