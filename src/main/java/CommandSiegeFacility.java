
public class CommandSiegeFacility extends Command {

    private SmartFacility facility;

    public CommandSiegeFacility() {

    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {

        army.getForm().recalc(army.getVehicles());

        facility = MyStrategy.commanderFacility.getFacilityToSiege(army);
        if (facility == null) {//no facility to siege
            complete();
            return;
        }
        
        Point2D targetVec = facility.getFacilityCentre().subtract(army.getForm().getAvgPoint());

        if (targetVec.magnitude() < CustomParams.onFacilityEps && facility.getOwnerPlayerId() == MyStrategy.player.getId()) {
            complete();
            return;
        }

        TargetPoint point = new TargetPoint();
        point.vector = targetVec;

        setParentCommand(new CommandMove(targetVec));
    }

    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }

}
