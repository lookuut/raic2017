
import model.VehicleType;

import java.util.Arrays;
import java.util.HashMap;


public class ArmyDamageField {

    private MyStrategy strategy;

    public static int pFieldCellSize = 2;

    public static int pFieldDeep = 4;
    public static int ARRV_REPAIR_FACTOR = 4;

    public static int AERIAL_DAMAGE_FIELD = 0;
    public static int GROUND_DAMAGE_FIELD = 1;
    public static int AERIAL_DEFENCE_FIELD = 2;
    public static int GROUND_DEFENCE_FIELD = 3;

    protected long[][][] enemyArmyField;

    protected int pFieldWidth;
    protected int pFieldHeight;

    public ArmyDamageField(MyStrategy strategy) {
        this.strategy = strategy;

        //@TODO use more complex method to cast from double to int
        pFieldWidth = (int)(Math.ceil(strategy.getGame().getWorldWidth() / ArmyDamageField.pFieldCellSize));
        pFieldHeight = (int)(Math.ceil(strategy.getGame().getWorldHeight() / ArmyDamageField.pFieldCellSize));

        this.enemyArmyField = new long[pFieldWidth][pFieldHeight][ArmyDamageField.pFieldDeep];
    }

    protected void updatePField(double startX, double startY, double cellSize, int damage, int type) throws Exception {

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);

        startX = Math.min(pFieldWidth, startX);
        startY = Math.min(pFieldHeight, startY);

        for (double startYIndex = startY; startYIndex < cellSize; startYIndex += ArmyDamageField.pFieldCellSize) {
            int pFieldY = (int)Math.floor(startYIndex / pFieldCellSize);
            for (double startXIndex = startX; startXIndex < startX + cellSize; startXIndex += ArmyDamageField.pFieldCellSize) {
                int pFieldX = (int)Math.floor(startXIndex / pFieldCellSize);
                if (enemyArmyField[pFieldY][pFieldX][type] + damage < 0) {
                    throw new Exception("something goes wrong with damage field calculcation");
                }
                enemyArmyField[pFieldY][pFieldX][type] += damage;
            }
        }
    }

    protected void updateVehiclePField(SmartVehicle vehicle, HashMap <Long, SmartVehicle> previousVehiclesStates, double x, double y) {
        try {
            int aerialAttackRange = (int)Math.floor(vehicle.getAerialAttackRange() / ArmyDamageField.pFieldCellSize);
            int aerialDefence = vehicle.getAerialDefence();
            int durability = vehicle.getDurability();

            double l = aerialAttackRange / Math.sqrt(2);
            this.updatePField(x - l,y - l, 2 * l, vehicle.getAerialDamage(), AERIAL_DAMAGE_FIELD);
            this.enemyArmyField[(int)Math.ceil( y / ArmyDamageField.pFieldCellSize)][(int)Math.ceil( x / ArmyDamageField.pFieldCellSize)][AERIAL_DEFENCE_FIELD] += aerialDefence + durability;

            int groundAttackRange = (int)Math.floor(vehicle.getGroundAttackRange() / ArmyDamageField.pFieldCellSize);
            int groundDefence = vehicle.getGroundDefence();

            l = groundAttackRange / Math.sqrt(2);
            this.updatePField(x - l,y - l, 2 * l, vehicle.getGroundDamage(), GROUND_DAMAGE_FIELD);
            this.enemyArmyField[(int)Math.ceil( y / ArmyDamageField.pFieldCellSize)][(int)Math.ceil( x / ArmyDamageField.pFieldCellSize)][GROUND_DEFENCE_FIELD] += groundDefence + durability;

            SmartVehicle prevVehicleState = previousVehiclesStates.get(vehicle.getId());

            //ARRV add durability to nearest vehicles
            if (vehicle.getType() == VehicleType.ARRV) {
                double arrvRepairRange = this.strategy.getGame().getArrvRepairRange();
                double arrvRepairSpeed = this.strategy.getGame().getArrvRepairSpeed();
                double repairDurabilityOnInterval = this.strategy.getGame().getActionDetectionInterval() * arrvRepairSpeed * ARRV_REPAIR_FACTOR;

                l = arrvRepairRange / Math.sqrt(2);

                this.updatePField(x - l, y - l, 2 * l, (int)Math.floor(repairDurabilityOnInterval), AERIAL_DEFENCE_FIELD);
                this.updatePField(x - l, y - l, 2 * l, (int)Math.floor(repairDurabilityOnInterval), GROUND_DEFENCE_FIELD);

                //minus defence from arrv in previous position
                if (prevVehicleState != null) {
                    l = arrvRepairRange / Math.sqrt(2);
                    this.updatePField(prevVehicleState.getX() - l, prevVehicleState.getY() - l, 2 * l, -(int)Math.floor(repairDurabilityOnInterval), AERIAL_DEFENCE_FIELD);
                    this.updatePField(prevVehicleState.getX() - l, prevVehicleState.getY() - l, 2 * l, -(int)Math.floor(repairDurabilityOnInterval), GROUND_DEFENCE_FIELD);
                }
            }
            //minus potentional from previous position of vehicle

            if (prevVehicleState != null) {
                this.updatePField(prevVehicleState.getX() - l,prevVehicleState.getY() - l, 2 * l, -prevVehicleState.getAerialDamage(), AERIAL_DAMAGE_FIELD);
                this.updatePField(prevVehicleState.getX() - l,prevVehicleState.getY() - l, 2 * l, -prevVehicleState.getGroundDamage(), GROUND_DAMAGE_FIELD);
                int prevY = (int)Math.ceil( prevVehicleState.getY() / ArmyDamageField.pFieldCellSize);
                int prevX = (int)Math.ceil( prevVehicleState.getX() / ArmyDamageField.pFieldCellSize);
                this.enemyArmyField[prevY][prevX][AERIAL_DEFENCE_FIELD] -= (aerialDefence + prevVehicleState.getDurability());
                this.enemyArmyField[prevY][prevX][GROUND_DEFENCE_FIELD] -= (groundDefence + prevVehicleState.getDurability());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void defineArmyForm () {
        //remember about filter died vehicles
        HashMap <Long, SmartVehicle> previousVehiclesStates = this.strategy.getPreviousVehiclesStates();

        Arrays.stream(
                this.strategy.getWorld().
                        getNewVehicles()).
                filter(vehicle -> vehicle.getDurability() > 0 && vehicle.getPlayerId()== MyStrategy.getEnemyPlayerId()).
                forEach(vehicle -> {
                    SmartVehicle smartVehicle = previousVehiclesStates.get(vehicle.getId());
                    if (smartVehicle != null) {
                        this.updateVehiclePField(smartVehicle, previousVehiclesStates, vehicle.getX(), vehicle.getY());
                    }
        });

        Arrays.stream(this.strategy.getWorld().getVehicleUpdates())
                .filter(vehicle -> previousVehiclesStates.get(vehicle.getId()).getPlayerId() == MyStrategy.getEnemyPlayerId())
                .forEach(vehicleUpdate -> {
                    SmartVehicle smartVehicle = previousVehiclesStates.get(vehicleUpdate.getId());
                    this.updateVehiclePField(smartVehicle, previousVehiclesStates, vehicleUpdate.getX(), vehicleUpdate.getY());
                });
    }

    public long[][][] getEnemyArmyField () {
        return enemyArmyField;
    }

    public void print() {
        System.out.println(Arrays.deepToString(enemyArmyField).replaceAll("],", "]," + System.getProperty("line.separator")));
    }
}
