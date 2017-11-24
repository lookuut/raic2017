
import model.Vehicle;
import model.VehicleType;
import model.VehicleUpdate;

public class SmartVehicle  {

    protected double radius;
    protected double x;
    protected double y;

    protected long id;
    protected long playerId;
    protected int durability;
    protected int maxDurability;
    protected double maxSpeed;
    protected double visionRange;
    protected double squaredVisionRange;
    protected double groundAttackRange;
    protected double squaredGroundAttackRange;
    protected double aerialAttackRange;
    protected double squaredAerialAttackRange;
    protected int groundDamage;
    protected int aerialDamage;
    protected int groundDefence;
    protected int aerialDefence;
    protected int attackCooldownTicks;
    protected int remainingAttackCooldownTicks;
    protected VehicleType type;
    protected boolean aerial;
    protected boolean selected;
    protected int[] groups;
    protected MyStrategy strategy;

    protected BattleFieldCell battleFieldCell;

    // null if vehicle is not in any army;
    protected Army army;

    public SmartVehicle (Vehicle vehicle, MyStrategy strategy) {

        this.id = vehicle.getId();
        this.playerId = vehicle.getPlayerId();
        this.durability = vehicle.getDurability();
        this.maxDurability = vehicle.getMaxDurability();
        this.maxSpeed = vehicle.getMaxSpeed();
        this.visionRange = vehicle.getVisionRange();
        this.squaredVisionRange = vehicle.getSquaredVisionRange();
        this.groundAttackRange = vehicle.getGroundAttackRange();
        this.squaredGroundAttackRange = vehicle.getSquaredGroundAttackRange();
        this.aerialAttackRange = vehicle.getAerialAttackRange();
        this.squaredAerialAttackRange = vehicle.getSquaredAerialAttackRange();
        this.groundDamage = vehicle.getGroundDamage();
        this.groundDefence = vehicle.getGroundDefence();
        this.attackCooldownTicks = vehicle.getAttackCooldownTicks();
        this.remainingAttackCooldownTicks = vehicle.getRemainingAttackCooldownTicks();
        this.type = vehicle.getType();
        this.aerial = vehicle.isAerial();
        this.selected = vehicle.isSelected();
        this.groups = vehicle.getGroups();
        this.aerialDamage = vehicle.getAerialDamage();
        this.aerialDefence = vehicle.getAerialDefence();

        this.x = vehicle.getX();
        this.y = vehicle.getY();
        this.radius = vehicle.getRadius();
        this.army = null;
        this.battleFieldCell = null;
        this.strategy = strategy;
    }


    public void vehicleUpdate(VehicleUpdate vehicleUpdate) {

        this.x = vehicleUpdate.getX();
        this.y = vehicleUpdate.getY();

        this.durability = vehicleUpdate.getDurability();
        this.remainingAttackCooldownTicks = vehicleUpdate.getRemainingAttackCooldownTicks();
        this.groups = vehicleUpdate.getGroups();
        this.selected = vehicleUpdate.isSelected();
    }

    public void vehicleUpdate(Vehicle vehicle) {
        this.x = vehicle.getX();
        this.y = vehicle.getY();

        this.durability = vehicle.getDurability();
        this.remainingAttackCooldownTicks = vehicle.getRemainingAttackCooldownTicks();
        this.groups = vehicle.getGroups();
        this.selected = vehicle.isSelected();
    }


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getGroundDamage () {
        return groundDamage;
    }

    public int getAerialDamage () {
        return aerialDamage;
    }

    public long getPlayerId () {
        return playerId;
    }

    public double getAerialAttackRange () {
        return aerialAttackRange;
    }

    public double getGroundAttackRange () {
        return groundAttackRange;
    }

    public int getAerialDefence () {
        return aerialDefence;
    }

    public int getGroundDefence () {
        return groundDefence;
    }

    public VehicleType getType() {
        return this.type;
    }

    public int getDurability () {
        return durability;
    }

    public long getId() {
        return id;
    }

    public BattleFieldCell getBattleFieldCell () {
        return battleFieldCell;
    }

    public void setBattleFieldCell(BattleFieldCell battleFieldCell) {
        this.battleFieldCell = battleFieldCell;
    }

    /**
     * @TODO be carefull here cause double compare missed
     * @return
     */
    public boolean isVehicleMoved() {
        return strategy.getVehiclePrevState(getId()) == null || (int)getX() != (int)strategy.getVehiclePrevState(getId()).getX() || (int)getY() != (int)strategy.getVehiclePrevState(getId()).getY();
    }

    public boolean getSelected () {
        return this.selected;
    }

    public double getVisionRange () {
        return visionRange;
    }

    public double distanceToPoint(double x, double y) {
        return Math.sqrt(Math.pow(x - this.getX(), 2) + Math.pow(y - this.getY(), 2));
    }

    public boolean isVisiblePoint (double x, double y) {
        return distanceToPoint(x,y) < this.visionRange;
    }

    public boolean isAlly() {
        return this.getPlayerId() == MyStrategy.player.getId();
    }

    public boolean isTerrain() {
        return !(getType() == VehicleType.FIGHTER || getType() == VehicleType.HELICOPTER);
    }

    public boolean isAerialAttacker () {
        return (getType() == VehicleType.FIGHTER || getType() == VehicleType.HELICOPTER || getType() == VehicleType.IFV);
    }

    public boolean isTerrainAttacker () {
        return (getType() == VehicleType.TANK || getType() == VehicleType.HELICOPTER || getType() == VehicleType.IFV);
    }

    public int getAerialPPFactor() {
        int factor = 0;

        if (isAlly() && !isTerrain()) {
            factor += CustomParams.allyUnitPPFactor;
        }else if (!isAlly() && (isAerialAttacker())) {
            factor += getAerialDamage();
        }
        return factor;
    }


    public float getDamagePPFactor (VehicleType type, boolean isAerial) {

        int allyGroundAttack = 0;
        int allyAerialAttack = 0;

        int enemyGroundAttack = 0;
        int enemyAerialAttack = 0;

        int enemyGroundDefence = 0;
        int enemyAerialDefence = 0;

        int allyGroundDefence = 0;
        int allyAerialDefence = 0;

        if (type == VehicleType.TANK) {
            allyGroundAttack = MyStrategy.game.getTankGroundDamage();
            allyAerialAttack = MyStrategy.game.getTankAerialDamage();

            allyGroundDefence = MyStrategy.game.getTankGroundDefence();
            allyAerialDefence = MyStrategy.game.getTankAerialDefence();
        } else if (type == VehicleType.FIGHTER) {
            allyGroundAttack = MyStrategy.game.getFighterGroundDamage();
            allyAerialAttack = MyStrategy.game.getFighterAerialDamage();
            allyGroundDefence = MyStrategy.game.getFighterGroundDefence();
            allyAerialDefence = MyStrategy.game.getFighterAerialDefence();
        } else if (type == VehicleType.HELICOPTER) {
            allyGroundAttack += MyStrategy.game.getHelicopterGroundDamage();
            allyAerialAttack += MyStrategy.game.getHelicopterAerialDamage();
            allyGroundDefence = MyStrategy.game.getHelicopterGroundDefence();
            allyAerialDefence = MyStrategy.game.getHelicopterAerialDefence();
        } else if (type == VehicleType.IFV) {
            allyGroundAttack += MyStrategy.game.getIfvGroundDamage();
            allyAerialAttack += MyStrategy.game.getIfvAerialDamage();
            allyGroundDefence  = MyStrategy.game.getIfvGroundDefence();
            allyAerialDefence = MyStrategy.game.getIfvAerialDefence();
        } else if (type == VehicleType.ARRV) {
            allyGroundAttack = 0;
            allyAerialAttack = 0;
            allyGroundDefence = MyStrategy.game.getArrvGroundDefence();
            allyAerialDefence = MyStrategy.game.getArrvAerialDefence();
        }

        if (getType() == VehicleType.TANK) {
            enemyGroundAttack = MyStrategy.game.getTankGroundDamage();
            enemyAerialAttack = MyStrategy.game.getTankAerialDamage();

            enemyGroundDefence = MyStrategy.game.getTankGroundDefence();
            enemyAerialDefence = MyStrategy.game.getTankAerialDefence();
        } else if (getType() == VehicleType.HELICOPTER) {
            enemyGroundAttack = MyStrategy.game.getHelicopterGroundDamage();
            enemyAerialAttack = MyStrategy.game.getHelicopterAerialDamage();

            enemyGroundDefence = MyStrategy.game.getHelicopterGroundDefence();
            enemyAerialDefence = MyStrategy.game.getHelicopterAerialDefence();
        } else if (getType() == VehicleType.FIGHTER) {
            enemyGroundAttack = MyStrategy.game.getFighterGroundDamage();
            enemyAerialAttack = MyStrategy.game.getFighterAerialDamage();

            enemyGroundDefence = MyStrategy.game.getFighterGroundDefence();
            enemyAerialDefence = MyStrategy.game.getFighterAerialDefence();

        } else if (getType() == VehicleType.IFV) {
            enemyGroundAttack = MyStrategy.game.getIfvGroundDamage();
            enemyAerialAttack = MyStrategy.game.getIfvAerialDamage();

            enemyGroundDefence = MyStrategy.game.getIfvGroundDefence();
            enemyAerialDefence = MyStrategy.game.getIfvAerialDefence();
        } else if (getType() == VehicleType.ARRV) {
            enemyGroundAttack = 0;
            enemyAerialAttack = 0;

            enemyGroundDefence = MyStrategy.game.getArrvGroundDefence();
            enemyAerialDefence = MyStrategy.game.getArrvAerialDefence();
        }

        //@TODO kostiiiil
        if (type == VehicleType.FIGHTER && getType() == VehicleType.ARRV) {
            allyGroundAttack = 0;
            allyAerialAttack = 0;

            enemyGroundAttack = 0;
            enemyAerialAttack = 0;

            enemyGroundDefence = 0;
            enemyAerialDefence = 0;

            allyGroundDefence = 0;
            allyAerialDefence = 0;
        }

        if (type == getType()) {
            allyAerialDefence += 20;
            allyGroundDefence += 20;
        }

        if (isAerial() && isAerial) {
            return allyAerialAttack + allyAerialDefence - enemyAerialAttack - enemyAerialDefence;
        } else if (isAerial() && !isAerial) {
            return allyAerialAttack + allyAerialDefence - enemyGroundAttack - enemyGroundDefence;
        } else if (!isAerial() && isAerial) {
            return allyGroundAttack + allyGroundDefence - enemyAerialAttack - enemyAerialDefence;
        } else {
            return allyGroundAttack + allyGroundDefence - enemyGroundAttack - enemyGroundDefence;
        }
    }

    public int getTerrainPPFactor() {
        int factor = 0;

        if (isTerrain()) {
            factor += CustomParams.allyUnitPPFactor;
        }

        if (!isAlly() && isTerrainAttacker()) {
            factor += getAerialDamage();
        }

        return factor;
    }

    public boolean isAerial() {
        return aerial;
    }

    public int getRemainingAttackCooldownTicks () {
        return this.remainingAttackCooldownTicks;
    }
}
