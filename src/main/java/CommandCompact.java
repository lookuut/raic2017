
public class CommandCompact extends Command {

    private CommandRotate rotate;
    private CommandScale scale;

    public boolean check (ArmyAllyOrdering army) {
        rotate.check(army);
        scale.check(army);
        if (rotate.isFinished() && scale.isFinished()) {
            complete();
            return true;
        }

        if (rotate.isFinished() && scale.isNew()) {
            setParentCommand(scale);
        }
        return false;
    }

    @Override
    public void prepare(ArmyAllyOrdering army) throws Exception {
        if (!army.isNeedToCompact()) {
            complete();
            return;
        }
        army.getForm().recalc(army.getVehicles());
        Point2D centre = army.getForm().getAvgPoint();
        Point2D maxVec = army.getForm().getMaxDistanceVec(centre);

        double arcLenght = maxVec.magnitude() * CustomParams.compactAngle;
        SmartVehicle vehicle = army.getForm().getEdgesVehicles().values().stream().findFirst().get();
        Integer tickRotate = Math.min((int)Math.ceil(arcLenght / vehicle.getMaxSpeed()), 60);
        Integer tickScale = Math.min((int)Math.ceil(maxVec.magnitude() / vehicle.getMaxSpeed()), 60);

        rotate = new CommandRotate((Math.random() > 0.5 ? -1 : 1 ) * CustomParams.compactAngle, centre, tickRotate);
        scale = new CommandScale(tickScale);
        setParentCommand(rotate);
    }

    @Override
    public void pinned(){

    }

    @Override
    public void processing(SmartVehicle vehicle) {

    }
}
