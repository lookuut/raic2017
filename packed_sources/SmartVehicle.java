
import model.*;

import java.util.Map;

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

    protected BattleFieldCell battleFieldCell;
    // null if vehicle is not in any army;
    protected ArmyAllyOrdering army;

    protected Point2D leftBottomAngle;
    protected Point2D rightTopAngle;

    public SmartVehicle (Vehicle vehicle) {

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
        this.point = new Point2D(vehicle.getX(), vehicle.getY());
        this.leftBottomAngle = new Point2D(vehicle.getX() - vehicle.getRadius(),  vehicle.getY() - vehicle.getRadius());
        this.rightTopAngle = new Point2D(vehicle.getX() + vehicle.getRadius(),  vehicle.getY() + vehicle.getRadius());
    }


    public void vehicleUpdate(VehicleUpdate vehicleUpdate) {

        this.x = vehicleUpdate.getX();
        this.y = vehicleUpdate.getY();

        this.durability = vehicleUpdate.getDurability();
        this.remainingAttackCooldownTicks = vehicleUpdate.getRemainingAttackCooldownTicks();
        this.groups = vehicleUpdate.getGroups();
        this.selected = vehicleUpdate.isSelected();
        this.point.setX(vehicleUpdate.getX());
        this.point.setY(vehicleUpdate.getY());
    }

    public void vehicleUpdate(Vehicle vehicle) {
        this.x = vehicle.getX();
        this.y = vehicle.getY();

        this.durability = vehicle.getDurability();
        this.remainingAttackCooldownTicks = vehicle.getRemainingAttackCooldownTicks();
        this.groups = vehicle.getGroups();
        this.selected = vehicle.isSelected();
        this.point.setX(vehicle.getX());
        this.point.setY(vehicle.getY());
    }

    public boolean isMoving() {
        SmartVehicle prevVehicleState = MyStrategy.getVehiclePrevState(getId());
        return prevVehicleState != null && prevVehicleState.getPoint().distance(getPoint()) > 0;
    }

    public int getMaxDurability () {
        return maxDurability;
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

    public void setPoint(Point2D point) {
        this.point = point;
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

    public int getAttackCooldownTicks() {
        return attackCooldownTicks;
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
        return MyStrategy.getVehiclePrevState(getId()) == null || getX() != MyStrategy.getVehiclePrevState(getId()).getX() || getY() != MyStrategy.getVehiclePrevState(getId()).getY();
    }

    public boolean isDurabilityChanched () {
        return MyStrategy.getVehiclePrevState(getId()) == null || MyStrategy.getVehiclePrevState(getId()).getDurability() != getDurability();
    }

    public boolean isAttackCooldownChanched () {
        return MyStrategy.getVehiclePrevState(getId()) == null || MyStrategy.getVehiclePrevState(getId()).getRemainingAttackCooldownTicks() != getRemainingAttackCooldownTicks();
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
        return distanceToPoint(x,y) < getActualVisionRange();
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

    public Point2D getMovingVector() {
        Point2D previousStatePoint = MyStrategy.getVehiclePrevState(getId()).getPoint();
        return getPoint().subtract(previousStatePoint);
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

    public float getDefencePPFactor(boolean attackerIsAerial, boolean attackAerial) {

        if (getType() == VehicleType.ARRV && !attackerIsAerial && !attackAerial) {//@TODO boolshit remove it
            return 300;
        }
        //ae to ae
        if (attackerIsAerial && attackAerial && isTerrain()) {
            return 0;
        }

        if (attackerIsAerial && attackAerial && isAerial()) {
            return getAerialDefence();
        }

        //ae to ter
        if (attackerIsAerial && !attackAerial && isAerial()) {
            return 0;
        }

        if (attackerIsAerial && !attackAerial && isTerrain()) {
            return getAerialDefence();
        }

        //ter to ae
        if (!attackerIsAerial && attackAerial && isTerrain()) {
            return 0;
        }

        if (!attackerIsAerial && attackAerial && isAerial()) {
            return getGroundDefence();
        }

        //ter to ter
        if (!attackerIsAerial && !attackAerial && isAerial()) {
            return 0;
        }

        if (!attackerIsAerial && !attackAerial && isTerrain()) {
            return getGroundDefence();
        }

        return 0;
    }

    public float getDamagePPFactor (boolean isAerial) {
        if (isAerial) {
            return getAerialDamage();
        }

        return getGroundDamage();
    }

    public boolean isAerial() {
        return aerial;
    }

    public int getRemainingAttackCooldownTicks () {
        return this.remainingAttackCooldownTicks;
    }

    public void addArmy(ArmyAllyOrdering army) {
        this.army = army;
    }

    public boolean isHaveArmy(ArmyAllyOrdering army) {
        return this.army == army;
    }

    public ArmyAllyOrdering getArmy() {
        return army;
    }

    /**
     *
     * @desc calculate when vehicle come to target point with considering terrain or weather map
     * @param direction
     * @return
     */

    public Integer getVehiclePointAtTick(Point2D direction) throws Exception {
        Point2D startPoint = getPoint().clone();
        Point2D targetPoint = getPoint().add(direction);

        targetPoint.setX(Math.max(targetPoint.getX(),0 ));
        targetPoint.setY(Math.max(targetPoint.getY(),0 ));

        targetPoint.setX(Math.min(targetPoint.getX(), MyStrategy.world.getWidth() - 1));
        targetPoint.setY(Math.min(targetPoint.getY(), MyStrategy.world.getHeight() - 1));

        startPoint.setX(Math.max(startPoint.getX(), 0));
        startPoint.setY(Math.max(startPoint.getY(), 0));

        startPoint.setX(Math.min(startPoint.getX(), MyStrategy.world.getWidth() - 1));
        startPoint.setY(Math.min(startPoint.getY(), MyStrategy.world.getHeight() - 1));

        int propose = (int) MyStrategy.world.getWidth() / MyStrategy.game.getTerrainWeatherMapColumnCount();

        Point2D voxelStartPoint = startPoint.multiply(1/(double)propose);

        Point2D voxelEndPoint = targetPoint.multiply(1/(double)propose);

        Integer stepX = propose * (direction.getX() < 0  ? -1 : 1);
        Integer stepY = propose * (direction.getY() < 0  ? -1 : 1);

        Integer tStartX = (stepX >= 0 ?(int)Math.floor(voxelStartPoint.getX()) : (int)Math.ceil(voxelStartPoint.getX())) * propose;
        Integer tStartY = (stepY >= 0 ?(int)Math.floor(voxelStartPoint.getY()) : (int)Math.ceil(voxelStartPoint.getY())) * propose;

        Point2D previousPoint = startPoint;

        double maxWidth = MyStrategy.world.getWidth();
        double maxHeight = MyStrategy.world.getHeight();
        double tickSum = 0;
        double speed;
        while (
                ((
                        stepX > 0 && Math.floor((tStartX + stepX) / propose) <= Math.floor(voxelEndPoint.getX())
                        ||
                        stepY > 0 && Math.floor((tStartY + stepY) / propose) <= Math.floor(voxelEndPoint.getY())
                )
                ||
                (
                        stepX < 0 && Math.floor((tStartX + stepX) / propose) > Math.floor(voxelEndPoint.getX())
                        ||
                        stepY < 0 && Math.floor((tStartY + stepY) / propose) > Math.floor(voxelEndPoint.getY())
                ))
                    &&
                (tStartX + stepX >= 0 && tStartY + stepY >= 0
                &&
                tStartX + stepX < maxWidth && tStartY + stepY < maxHeight)
                ) {

            Point2D horIntersectPoint = Point2D.lineIntersect(startPoint, targetPoint, new Point2D(0, tStartY + stepY) , new Point2D(maxWidth, tStartY + stepY ));
            Point2D verIntersectPoint = Point2D.lineIntersect(startPoint, targetPoint, new Point2D(tStartX + stepX, 0) , new Point2D(tStartX + stepX, maxHeight));
            Point2D intersectPoint = targetPoint;

            if (horIntersectPoint == null && verIntersectPoint == null) {
                throw new Exception("Cant intersect lines found");
            }
            //@TODO workaround
            if (tStartX / propose >= MyStrategy.game.getTerrainWeatherMapColumnCount() ||
                    tStartY / propose >= MyStrategy.game.getTerrainWeatherMapRowCount() ||
                    tStartX / propose < 0 ||
                    tStartY / propose < 0) {
                break;
            }
            speed = getMaxSpeed() * getEnviromentSpeedFactor(tStartX / propose, tStartY / propose);
            if (horIntersectPoint != null && verIntersectPoint != null && horIntersectPoint.magnitude() == verIntersectPoint.magnitude()) {
                tStartX += stepX;
                tStartY += stepY;
                intersectPoint = verIntersectPoint;
            } else if (horIntersectPoint == null || (verIntersectPoint != null && verIntersectPoint.subtract(startPoint).magnitude() < horIntersectPoint.subtract(startPoint).magnitude())) {
                tStartX += stepX;
                intersectPoint = verIntersectPoint;
            } else if (verIntersectPoint == null || (horIntersectPoint != null && verIntersectPoint.subtract(startPoint).magnitude() >= horIntersectPoint.subtract(startPoint).magnitude())) {
                tStartY += stepY;
                intersectPoint = horIntersectPoint;
            }

            tickSum += previousPoint.subtract(intersectPoint).magnitude() / speed;
            previousPoint = intersectPoint;
        }

        int x = tStartX / propose;
        int y = tStartY / propose;

        if (x >= 0 && x < MyStrategy.game.getTerrainWeatherMapColumnCount() && y >= 0 && y < MyStrategy.game.getTerrainWeatherMapRowCount()) {
            speed = getMaxSpeed() * getEnviromentSpeedFactor(tStartX / propose, tStartY / propose);;
            tickSum += previousPoint.subtract(targetPoint).magnitude() / speed;
        }

        return (int)Math.ceil(tickSum);
    }

    /**
     * @desc calculate dest point in tick to  direction
     * @return
     */
    public Point2D getVehiclePointAtTick(Point2D direction, Integer tick)  throws  Exception {
        Point2D startPoint = getPoint();
        Point2D targetPoint = direction.multiply(MyStrategy.world.getWidth()).add(getPoint());
        int propose = (int) MyStrategy.world.getWidth() / MyStrategy.game.getTerrainWeatherMapColumnCount();

        Point2D voxelStartPoint = getPoint().multiply(1/(double)propose);

        Point2D voxelEndPoint = targetPoint.multiply(1/(double)propose);


        Integer stepX = propose * (direction.getX() < 0  ? -1 : 1);
        Integer stepY = propose * (direction.getY() < 0  ? -1 : 1);

        Integer tStartX = (stepX >= 0 ?(int)Math.floor(voxelStartPoint.getX()) : (int)Math.ceil(voxelStartPoint.getX())) * propose;
        Integer tStartY = (stepY >= 0 ?(int)Math.floor(voxelStartPoint.getY()) : (int)Math.ceil(voxelStartPoint.getY())) * propose;

        Point2D previousPoint = startPoint;

        double maxWidth = MyStrategy.world.getWidth();
        double maxHeight = MyStrategy.world.getHeight();
        double tickSum = 0;

        while (
                ((
                        stepX > 0 && Math.floor((tStartX + stepX) / propose) < Math.floor(voxelEndPoint.getX())
                                ||
                                stepY > 0 && Math.floor((tStartY + stepY) / propose) < Math.floor(voxelEndPoint.getY())
                )
                ||
                (
                        stepX < 0 && Math.floor((tStartX + stepX) / propose) > Math.floor(voxelEndPoint.getX())
                                ||
                                stepY < 0 && Math.floor((tStartY + stepY) / propose) > Math.floor(voxelEndPoint.getY())
                ))
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
            int x = stepX >= 0 ? tStartX / propose : (tStartX + stepX) / propose;
            int y = stepY >= 0 ? tStartY / propose : (tStartY + stepY) / propose;
            double cellSpeed = getMaxSpeed() * getEnviromentSpeedFactor(x, y);
            if (horIntersectPoint == null || (verIntersectPoint != null && verIntersectPoint.subtract(startPoint).magnitude() < horIntersectPoint.subtract(startPoint).magnitude())) {
                tStartX += stepX;
                intersectPoint = verIntersectPoint;
            } else if (verIntersectPoint == null || (horIntersectPoint != null && horIntersectPoint.subtract(startPoint).magnitude() <= verIntersectPoint.subtract(startPoint).magnitude())) {
                tStartY += stepY;
                intersectPoint = horIntersectPoint;
            }

            double lastSegmentLenght = previousPoint.subtract(intersectPoint).magnitude();
            double tickInCell = lastSegmentLenght / (cellSpeed);
            if (tickSum + tickInCell >= tick) {
                return direction.normalize().multiply(cellSpeed * (tick - tickSum)).add( previousPoint );
            }
            tickSum += tickInCell;
            previousPoint = intersectPoint;
        }

        return new Point2D(tStartX, tStartY);
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

    public double getActualVisionRange() {
        double factor = 1.0;
        int x = (int)Math.floor(getX() / MyStrategy.getWeatherTerrainWidthPropose());
        int y = (int)Math.floor(getY() / MyStrategy.getWeatherTerrainHeightPropose());

        if (isAerial()) {
            WeatherType[][] weather = MyStrategy.getWeatherMap();

            if (weather[x][y] == WeatherType.CLOUD ) {
                factor = MyStrategy.game.getCloudWeatherVisionFactor();
            } else if (weather[x][y] == WeatherType.RAIN) {
                factor = MyStrategy.game.getRainWeatherVisionFactor() ;
            }
        } else {
            TerrainType[][] terrain = MyStrategy.getTerrainMap();
            if (terrain[x][y] == TerrainType.FOREST ) {
                factor = MyStrategy.game.getForestTerrainVisionFactor();
            } else if (terrain[x][y] == TerrainType.SWAMP) {
                factor = MyStrategy.game.getSwampTerrainVisionFactor();
            }
            return getVisionRange() * factor;
        }

        return getVisionRange() * factor;
    }

    public double getMinVisionRange() {
        if (isTerrain()) {
            return MyStrategy.game.getForestTerrainVisionFactor() * getVisionRange();
        }
        return MyStrategy.game.getRainWeatherVisionFactor() * getVisionRange();
    }

    public double getAttackRange(boolean enemyIsAerial) {
        if (enemyIsAerial) {
            return getAerialAttackRange();
        }
        return getGroundAttackRange();
    }

    public double getRadius() {
        return radius;
    }

    public Point2D getRightTopAngle() {
        return rightTopAngle;
    }

    public Point2D getLeftBottomAngle() {
        return leftBottomAngle;
    }

    public static boolean isTargetVehicleType(VehicleType allyType, VehicleType enemyType) {

        if (allyType == VehicleType.FIGHTER && (enemyType == VehicleType.FIGHTER || enemyType == VehicleType.HELICOPTER))  {
            return true;
        }

        if (allyType == VehicleType.HELICOPTER && (enemyType == VehicleType.FIGHTER || enemyType == VehicleType.ARRV || enemyType == VehicleType.TANK || enemyType == VehicleType.HELICOPTER || enemyType == VehicleType.IFV))  {
            return true;
        }

        if (allyType == VehicleType.IFV && (enemyType == VehicleType.HELICOPTER || enemyType == VehicleType.FIGHTER || enemyType == VehicleType.IFV || enemyType == VehicleType.ARRV || enemyType == VehicleType.TANK))  {
            return true;
        }

        if (allyType == VehicleType.TANK && (enemyType == VehicleType.IFV || enemyType == VehicleType.ARRV || enemyType == VehicleType.TANK || enemyType == VehicleType.HELICOPTER))  {
            return true;
        }

        return false;
    }

    public static boolean isVictimType (VehicleType allyType, VehicleType enemyType) {
        if (allyType == VehicleType.FIGHTER && (enemyType == VehicleType.HELICOPTER))  {
            return true;
        }

        if (allyType == VehicleType.HELICOPTER && (enemyType == VehicleType.ARRV ))  {
            return true;
        }

        if (allyType == VehicleType.IFV && (enemyType == VehicleType.HELICOPTER || enemyType == VehicleType.FIGHTER || enemyType == VehicleType.ARRV))  {
            return true;
        }

        if (allyType == VehicleType.TANK && (enemyType == VehicleType.IFV || enemyType == VehicleType.ARRV ))  {
            return true;
        }
        return false;
    }

    public static Point2D getAvgPoint(Map<Long, SmartVehicle> vehicles) {
        int count = 0;
        Point2D avgPoint = new Point2D(0,0);
        for (SmartVehicle vehicle : vehicles.values()) {
            if (vehicle.getDurability() > 0) {
                avgPoint.setX(avgPoint.getX() + vehicle.getPoint().getX());
                avgPoint.setY(avgPoint.getY() + vehicle.getPoint().getY());
                count++;
            }
        }
        avgPoint.setX(avgPoint.getX() / count);
        avgPoint.setY(avgPoint.getY() / count);
        return avgPoint;
    }

    public double getVehicleOnTickSpeed() {
        return MyStrategy.getVehiclePrevState(getId()).getPoint().subtract(getPoint()).magnitude();
    }

    public static int fighterGroundDefence = 70;
    public static int fighterAerialDefence = 70;

    public static int helicopterGroundDefence = 40;
    public static int helicopterAerialDefence = 40;

    public static int tankGroundDefence = 80;
    public static int tankAerialDefence = 60;

    public static int ifvGroundDefence = 60;
    public static int ifvAerialDefence = 80;

    public static int arrvGroundDefence = 20;
    public static int arrvAerialDefence = 50;


    public static int fighterGroundDamage = 0;
    public static int fighterAerialDamage = 100;

    public static int helicopterGroundDamage = 100;
    public static int helicopterAerialDamage = 80;

    public static int tankGroundDamage = 100;
    public static int tankAerialDamage = 60;

    public static int ifvGroundDamage = 90;
    public static int ifvAerialDamage = 80;

    public static int arrvGroundDamage = 0;
    public static int arrvAerialDamage = 0;

    public static int getEnemyDamage(VehicleType allyType, VehicleType enemyType) {

        int damage = 0;
        int defence = 0;
        if (SmartVehicle.isTerrain(allyType)) {
            switch (enemyType) {
                case FIGHTER:
                    damage = fighterGroundDamage;
                    break;
                case ARRV:
                    damage = arrvGroundDamage;
                    break;
                case TANK:
                    damage = tankGroundDamage;
                    break;
                case IFV:
                    damage = ifvGroundDamage;
                    break;
                case HELICOPTER:
                    damage = helicopterGroundDamage;
                    break;
            }
        } else {
            switch (enemyType) {
                case FIGHTER:
                    damage = fighterAerialDamage;
                    break;
                case ARRV:
                    damage = arrvAerialDamage;
                    break;
                case TANK:
                    damage = tankAerialDamage;
                    break;
                case IFV:
                    damage = ifvAerialDamage;
                    break;
                case HELICOPTER:
                    damage = helicopterAerialDamage;
                    break;
            }
        }

        if (SmartVehicle.isTerrain(enemyType)) {
            switch (allyType) {
                case FIGHTER:
                    defence = fighterGroundDefence;
                    break;
                case ARRV:
                    defence = arrvGroundDefence;
                    break;
                case TANK:
                    defence = tankGroundDefence;
                    break;
                case IFV:
                    defence = ifvGroundDefence;
                    break;
                case HELICOPTER:
                    defence = helicopterGroundDefence;
                    break;
            }
        } else {
            switch (allyType) {
                case FIGHTER:
                    defence = fighterAerialDefence;
                    break;
                case ARRV:
                    defence = arrvAerialDefence;
                    break;
                case TANK:
                    defence = tankAerialDefence;
                    break;
                case IFV:
                    defence = ifvAerialDefence;
                    break;
                case HELICOPTER:
                    defence = helicopterAerialDefence;
                    break;
            }
        }
        return Math.max(damage - defence, 0);
    }
}
