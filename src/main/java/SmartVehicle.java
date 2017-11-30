
import geom.Point2D;
import model.*;

import java.util.HashSet;

public class SmartVehicle  {

    protected double radius;
    protected double x;
    protected double y;
    protected Point2D point;

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
    protected HashSet<ArmyAllyOrdering> armySet;

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
        this.point = new Point2D(vehicle.getX(), vehicle.getY());
        armySet = new HashSet();
    }


    public void vehicleUpdate(VehicleUpdate vehicleUpdate) {

        this.x = vehicleUpdate.getX();
        this.y = vehicleUpdate.getY();

        this.durability = vehicleUpdate.getDurability();
        this.remainingAttackCooldownTicks = vehicleUpdate.getRemainingAttackCooldownTicks();
        this.groups = vehicleUpdate.getGroups();
        this.selected = vehicleUpdate.isSelected();
        this.point = new Point2D(vehicleUpdate.getX(), vehicleUpdate.getY());
    }

    public void vehicleUpdate(Vehicle vehicle) {
        this.x = vehicle.getX();
        this.y = vehicle.getY();

        this.durability = vehicle.getDurability();
        this.remainingAttackCooldownTicks = vehicle.getRemainingAttackCooldownTicks();
        this.groups = vehicle.getGroups();
        this.selected = vehicle.isSelected();
        this.point = new Point2D(vehicle.getX(), vehicle.getY());
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

    public Point2D getTerrainPoint(Point2D point) {
        int x = (int)Math.floor(MyStrategy.game.getTerrainWeatherMapColumnCount() * (getX() / MyStrategy.world.getWidth()));
        int y = (int)Math.floor(MyStrategy.game.getTerrainWeatherMapRowCount() * (getY() / MyStrategy.world.getHeight()));
        return new Point2D(x, y);
    }

    public double getAttackRange(SmartVehicle enemyVehicle) {
        if (enemyVehicle.isAerial()) {
            return getAerialAttackRange();
        } else {
            return getGroundAttackRange();
        }
    }

    public Point2D getPoint() {
        return point;
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

    public int getTypeInt() {
        switch (getType()) {
            case HELICOPTER:
                return 1;
            case IFV:
                return 2;
            case TANK:
                return 3;
            case FIGHTER:
                return 4;
            case ARRV:
                return 5;
        }
        return 0;
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
        return strategy.getVehiclePrevState(getId()) == null || getX() != strategy.getVehiclePrevState(getId()).getX() || getY() != strategy.getVehiclePrevState(getId()).getY();
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
        return SmartVehicle.isTerrain(getType());
    }

    public boolean isAerialAttacker () {
        return (getType() == VehicleType.FIGHTER || getType() == VehicleType.HELICOPTER || getType() == VehicleType.IFV);
    }

    public static boolean isTerrain(VehicleType type) {
        return !(type== VehicleType.FIGHTER || type== VehicleType.HELICOPTER);
    }

    public boolean isTerrainAttacker () {
        return (getType() == VehicleType.TANK || getType() == VehicleType.HELICOPTER || getType() == VehicleType.IFV);
    }

    public double getMaxSpeed () {
        return maxSpeed;
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

    public int getTerrainPPFactor() {
        int factor = 0;

        if (isTerrain()) {
            factor += CustomParams.allyUnitPPFactor;
        } else if (!isAlly() && isTerrainAttacker()) {
            factor += getGroundDamage();
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
            allyGroundAttack = MyStrategy.game.getHelicopterGroundDamage();
            allyAerialAttack = MyStrategy.game.getHelicopterAerialDamage();
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

        if (type == getType()) {
            enemyAerialAttack += CustomParams.equalTypeAttackFactor;
            enemyGroundAttack += CustomParams.equalTypeAttackFactor;
        }
        if (type == VehicleType.ARRV) {
            enemyGroundDefence = 0;
            enemyAerialDefence = 0;
        }

        if ((getType() == VehicleType.TANK && type == VehicleType.FIGHTER)
                ||
                (getType() == VehicleType.FIGHTER && type == VehicleType.TANK)
                ||
                (getType() == VehicleType.ARRV && type == VehicleType.FIGHTER)
                ||
                (getType() == VehicleType.FIGHTER && type == VehicleType.ARRV)
                ||
                (getType() == VehicleType.ARRV && type == VehicleType.ARRV)
                ) {
            return 0;
        }

        if (isAerial() && isAerial) {
            return  enemyAerialAttack + enemyAerialDefence - allyAerialAttack - allyAerialDefence;
        } else if (isAerial() && !isAerial) {
            return  enemyGroundAttack + enemyGroundDefence - allyAerialAttack - allyAerialDefence;
        } else if (!isAerial() && isAerial) {
            return enemyAerialAttack + enemyAerialDefence - allyGroundAttack - allyGroundDefence;
        } else {
            return enemyGroundAttack + enemyGroundDefence - allyGroundAttack - allyGroundDefence;
        }
    }

    public boolean isAerial() {
        return aerial;
    }

    public int getRemainingAttackCooldownTicks () {
        return this.remainingAttackCooldownTicks;
    }

    public void addArmy(ArmyAllyOrdering army) {
        armySet.add(army);
    }

    public boolean isHaveArmy(ArmyAllyOrdering army) {
        return armySet.contains(army);
    }

    public HashSet<ArmyAllyOrdering> getArmySet () {
        return armySet;
    }

    /**
     *
     * @desc calculate when vehicle come to target point with considering terrain or weather map
     * @param direction
     * @return
     */

    public Integer getVehiclePointAtTick(Point2D direction) throws Exception {
        Point2D targetPoint = getPoint().add(direction);
        int propose = (int)MyStrategy.world.getWidth() / MyStrategy.game.getTerrainWeatherMapColumnCount();

        Point2D voxelStartPoint = getPoint().multiply(1/(double)propose);

        Point2D voxelEndPoint = targetPoint.multiply(1/(double)propose);

        Integer tStartX = (int)Math.floor(voxelStartPoint.getX()) * propose;
        Integer tStartY = (int)Math.floor(voxelStartPoint.getY()) * propose;

        Integer stepX = propose * (direction.getX() < 0  ? -1 : 1);
        Integer stepY = propose * (direction.getY() < 0  ? -1 : 1);

        Point2D previousPoint = getPoint();

        double maxWidth = MyStrategy.world.getWidth();
        double maxHeight = MyStrategy.world.getHeight();
        double tickSum = 0;
        while (
                (
                        Math.floor(tStartX / propose) < Math.floor(voxelEndPoint.getX())
                            ||
                        Math.floor(tStartY / propose) < Math.floor(voxelEndPoint.getY())
                )
                    &&
                        (tStartX + stepX >= 0 && tStartY + stepY >= 0
                                &&
                                tStartX + stepX < maxWidth && tStartY + stepY < maxHeight)
                ) {

            Point2D horIntersectPoint = Point2D.lineIntersect(getPoint(), targetPoint, new Point2D(0, tStartY + stepY) , new Point2D(maxWidth, tStartY + stepY ));
            Point2D verIntersectPoint = Point2D.lineIntersect(getPoint(), targetPoint, new Point2D(tStartX + stepX, 0) , new Point2D(tStartX + stepX, maxHeight));
            Point2D intersectPoint = targetPoint;

            if (horIntersectPoint == null && verIntersectPoint == null) {
                throw new Exception("Cant intersect lines found");
            }

            if (horIntersectPoint == null || (verIntersectPoint != null && verIntersectPoint.magnitude() < horIntersectPoint.magnitude())) {
                tStartX += stepX;
                intersectPoint = verIntersectPoint;
            } else if (verIntersectPoint == null || (horIntersectPoint != null && horIntersectPoint.magnitude() <= verIntersectPoint.magnitude())) {
                tStartY += stepY;
                intersectPoint = horIntersectPoint;
            }

            tickSum += previousPoint.subtract(intersectPoint).magnitude() / (getMaxSpeed() * getEnviromentSpeedFactor(tStartX / propose, tStartY / propose));
            previousPoint = intersectPoint;
        }

        tickSum += previousPoint.subtract(targetPoint).magnitude() / (getMaxSpeed() * getEnviromentSpeedFactor(tStartX / propose, tStartY / propose));

        return (int)Math.round(tickSum);
    }

    public double getEnviromentSpeedFactor(int x, int y) {

        double speedFactor;
        if (isTerrain()) {
            TerrainType[][] terrainMap = MyStrategy.getTerrainMap();

            speedFactor = MyStrategy.game.getPlainTerrainSpeedFactor();
            if (terrainMap[x][y] == TerrainType.FOREST) {
                speedFactor = MyStrategy.game.getForestTerrainSpeedFactor();
            } else if (terrainMap[x][y] == TerrainType.SWAMP) {
                speedFactor = MyStrategy.game.getSwampTerrainSpeedFactor();
            }

        } else {
            WeatherType[][] weatherMap = MyStrategy.getWeatherMap();

            speedFactor = MyStrategy.game.getClearWeatherSpeedFactor();
            if (weatherMap[x][y] == WeatherType.RAIN) {
                speedFactor = MyStrategy.game.getRainWeatherSpeedFactor();
            } else if (weatherMap[x][y] == WeatherType.CLOUD) {
                speedFactor = MyStrategy.game.getCloudWeatherSpeedFactor();
            }
        }

        return speedFactor;
    }

    public double getVisionRange(SmartVehicle vehicle) {
        double factor = 1.0;
        int x = (int)Math.floor(vehicle.getX() / MyStrategy.getWeatherTerrainWidthPropose());
        int y = (int)Math.floor(vehicle.getY() / MyStrategy.getWeatherTerrainHeightPropose());

        if (vehicle.isAerial()) {
            WeatherType[][] weather = MyStrategy.getWeatherMap();

            if (weather[y][x] == WeatherType.CLOUD ) {
                factor = MyStrategy.game.getCloudWeatherVisionFactor();
            } else if (weather[y][x] == WeatherType.RAIN) {
                factor = MyStrategy.game.getRainWeatherVisionFactor() ;
            }
        } else {
            TerrainType[][] terrain = MyStrategy.getTerrainMap();
            if (terrain[y][x] == TerrainType.FOREST ) {
                factor = MyStrategy.game.getForestTerrainVisionFactor();
            } else if (terrain[y][x] == TerrainType.SWAMP) {
                factor = MyStrategy.game.getSwampTerrainVisionFactor();
            }
            return vehicle.getVisionRange() * factor;
        }

        return vehicle.getVisionRange() * factor;
    }
}
