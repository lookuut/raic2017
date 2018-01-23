
import model.VehicleType;

import java.util.*;
import java.util.stream.Collectors;

class Commander {

    private ArmyDivisions divisions;

    protected BehaviourTree<ArmyAlly> behaviourTree;
    protected Queue<BTreeAction> activeActions;

    private Map<VehicleType, List<SmartVehicle>> noArmyVehicles;
    private Map<VehicleType, Square> noArmySquaereMap;
    private CommandNuclearDefence nuclearAttackDefence;
    private static Deque<CommanderTask> tasksQueue;

    private static TerrainPPField terrainPPField;
    private static WeatherPPField weatherPPField;
    private SmartVehicle scoutVehicle;

    private Commander() {
        divisions = new ArmyDivisions();
        activeActions = new LinkedList<>();
        behaviourTree = new BehaviourTree<>();
        noArmyVehicles = new HashMap<>();
        noArmySquaereMap = new HashMap<>();
        nuclearAttackDefence = new CommandNuclearDefence();
        tasksQueue = new LinkedList<>();

        int ppFieldX = MyStrategy.battleField.getWidth();
        int ppFieldY = MyStrategy.battleField.getHeight();

        terrainPPField = new TerrainPPField(ppFieldX, ppFieldY);
        terrainPPField.addTerrainMap(MyStrategy.world.getTerrainByCellXY());

        weatherPPField = new WeatherPPField(ppFieldX, ppFieldY);
        weatherPPField.addWeatherMap(MyStrategy.getWeatherMap());
    }

    private void checkAttackNuclear() throws Exception {

        if (nuclearAttackDefence != null) {
            nuclearAttackDefence.check(null);
        }

        if (MyStrategy.isNuclearAttack() || nuclearAttackDefence.isRun()) {
            nuclearAttackDefence.run(null);
        }
    }

    public void clustering () {
        if (MyStrategy.world.getTickIndex() % 20 == 0) {
            MyStrategy.battleField.defineArmies();
            List<Army> allyArmies = MyStrategy.battleField.getAllyArmies();

            for (Army army : allyArmies) {
                SmartVehicle vehicle = army.getVehicles().values().iterator().next();
                if (vehicle.getArmy() != null) {
                    if (vehicle.getArmy().getVehicleCount() < army.getVehicleCount() && army.getVehiclesArmies().size() > 1) {
                        Iterator<ArmyAllyOrdering> iterator = army.getVehiclesArmies().iterator();
                        ArmyAllyOrdering firstArmy = iterator.next();
                        ArmyAllyOrdering secondArmy = iterator.next();

                        if (firstArmy.isAerial() == secondArmy.isAerial() && firstArmy.isHeat(secondArmy)) {
                            army.getForm().update(army.getVehicles());
                             System.out.println("Merge armies");
                             Square square = new Square(army.getForm().getMinPoint(), army.getForm().getMaxPoint());
                             new CommandCreateArmy(square, null);
                        }

                    } else if (vehicle.getArmy().getVehicleCount() > army.getVehicleCount()) {
                        System.out.println("Separate army");
                    }
                }
            }
        }
    }

    public void logic () throws Exception {
        constructArmies();
        checkAttackNuclear();
        nuclearAttack();
        //clustering();

        MyStrategy.commanderFacility.orderCreateVehicle();

        while (tasksQueue.size() > 0) {
            tasksQueue.getFirst().run();
            if (!tasksQueue.getFirst().check()) {
                break;
            }
            CommanderTask task = tasksQueue.pollFirst();
            divisions.addCondifuredArmy(task.getArmy());
        }

        //run divisions logic
        for (ArmyAllyOrdering army : divisions.getArmyList()) {
            if (army.isAlive() && !army.locked()) {
                army.run();
            }
        }
    }

    public void constructArmies() {
        
        for (Map.Entry<VehicleType, List<SmartVehicle>> entry : noArmyVehicles.entrySet()) {
            try {
                if (entry.getValue().size() >= CustomParams.minVehiclesCountInArmy) {
                    Square vehicleTypeSquare = noArmySquaereMap.get(entry.getKey());
                    if (
                        entry.getKey() == VehicleType.FIGHTER ||
                        entry.getKey() == VehicleType.HELICOPTER ||
                        entry.getKey() == VehicleType.IFV ||
                        entry.getKey() == VehicleType.ARRV) {
                        divisions.addArmy(vehicleTypeSquare, new HashSet(Arrays.asList(entry.getKey())));
                    } else if (entry.getKey() == VehicleType.TANK) {

                        double centreX = (vehicleTypeSquare.getRightTopAngle().getX() - vehicleTypeSquare.getLeftBottomAngle().getX()) / 2;
                        double centreY = (vehicleTypeSquare.getRightTopAngle().getY() - vehicleTypeSquare.getLeftBottomAngle().getY()) / 2;
                        Point2D centrePoint = new Point2D(vehicleTypeSquare.getLeftBottomAngle().getX() + centreX, vehicleTypeSquare.getLeftBottomAngle().getY() + centreY);

                        Square armySquare = new Square(vehicleTypeSquare.getLeftBottomAngle(), centrePoint.add(new Point2D(0, centreY)));
                        divisions.addArmy(armySquare, new HashSet(Arrays.asList(entry.getKey())));

                        armySquare = new Square(new Point2D(vehicleTypeSquare.getLeftBottomAngle().getX() + centreX, vehicleTypeSquare.getLeftBottomAngle().getY()), vehicleTypeSquare.getRightTopAngle());
                        divisions.addArmy(armySquare, new HashSet(Arrays.asList(entry.getKey())));
                    }
                    noArmySquaereMap.remove(entry.getKey());
                    noArmyVehicles.get(entry.getKey()).clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void check () {

        Iterator<Map.Entry<Integer, ArmyAllyOrdering>> iter = divisions.getArmies().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, ArmyAllyOrdering> entry = iter.next();
            ArmyAllyOrdering army = entry.getValue();

            if (!army.isAlive()) {
                divisions.removeArmy(army);
                iter.remove();
            } else if (army.isRun() && !army.locked()) {
                army.check();
            }
        }
    }

    public ArmyDivisions getDivisions() {
        return divisions;
    }

    private ArmyAllyOrdering nuclearAttackArmy;
    private Point2D nuclearAttackTarget;

    public boolean isCanNuclearAttack (Army army) {
        return army == nuclearAttackArmy && MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() == 0;
    }

    public Point2D getNuclearAttackTarget() {
        return nuclearAttackTarget;
    }

    public boolean isHaveEnemyAround () {
        for (ArmyAllyOrdering army : divisions.getArmyList()) {
            if (army.isHaveEnemyAround(CustomParams.safetyDistance)) {
                return true;
            }
        }

        return false;
    }

    public void nuclearAttack () {
        try {

            if (MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() > 0) {
                return;
            }

            if (divisions.getArmies().size() == 0) {
                return;
            }

            if (!isHaveEnemyAround()) {
                return;
            }

            double minTime = Double.MAX_VALUE;
            ArmyAllyOrdering minTimeArmy = null;
            Point2D minTimeNuclearTarget = null;
            SortedSet<NuclearAttackPoint> nuclearAttackPointSortedSet = MyStrategy.enemyField.getNuclearAttackPointsRating();

            if (nuclearAttackPointSortedSet.size() == 0) {
                return;
            }

            for (NuclearAttackPoint point : nuclearAttackPointSortedSet) {
                for (ArmyAllyOrdering army : divisions.getArmyList()) {//choose non locked armies and alive
                    if (!army.locked() && !army.isDangerousAround()) {
                        double distance = army.getForm().getEdgesVehiclesCenter().subtract(point.getPoint()).magnitude();
                        if (minTime > distance / army.getSpeed()) {
                            minTime = distance / army.getSpeed();
                            minTimeArmy = army;
                            minTimeNuclearTarget = point.getPoint();
                        }
                    }
                }
            }

            if (minTimeArmy == null) {
                return;
            }

            nuclearAttackTarget = minTimeNuclearTarget;
            nuclearAttackArmy = minTimeArmy;


            if (nuclearAttackArmy.getRunningCommand() instanceof CommandAttack) {
                nuclearAttackArmy.getRunningCommand().setState(CommandStates.Complete);
            }

            if (nuclearAttackArmy.getForm().isPointInVisionRange(getNuclearAttackTarget()) &&
                    nuclearAttackArmy.getRunningCommand() instanceof CommandMove &&
                    !(nuclearAttackArmy.getRunningCommand() instanceof CommandStop)/* &&
                    !nuclearAttackArmy.getForm().isDamagedByNuclearAttack(getNuclearAttackTarget())*/) {

                nuclearAttackArmy.getRunningCommand().setState(CommandStates.Complete);
                nuclearAttackArmy.addCommand(new CommandStop());
            }

        } catch (Exception e)  {
            e.printStackTrace();
        }
    }

    public void nuclearAttackClear() {
        nuclearAttackArmy = null;
        nuclearAttackTarget = null;
    }

    public void addNoArmyVehicle(SmartVehicle vehicle){
        if (vehicle.isAlly()) {

            if (!noArmyVehicles.containsKey(vehicle.getType())){
                noArmyVehicles.put(vehicle.getType(), new ArrayList<>());
                noArmySquaereMap.put(vehicle.getType(), new Square(vehicle.getLeftBottomAngle(), vehicle.getRightTopAngle()));
            } else {
                if (!noArmySquaereMap.containsKey(vehicle.getType())) {
                    noArmySquaereMap.put(vehicle.getType(), new Square(vehicle.getLeftBottomAngle(), vehicle.getRightTopAngle()));
                } else {
                    Square square = noArmySquaereMap.get(vehicle.getType());
                    square.addPoint(vehicle.getLeftBottomAngle());
                    square.addPoint(vehicle.getRightTopAngle());
                }
            }

            List<SmartVehicle> vehicleList = noArmyVehicles.get(vehicle.getType());
            vehicleList.add(vehicle);
        }
    }


    public static void addTask(CommanderTask task) {
        tasksQueue.add(task);
    }

    public void result(SmartVehicle vehicle) {
        if (tasksQueue.size() > 0) {
            tasksQueue.getFirst().result(vehicle);
        }
    }

    public static TerrainPPField getTerrainPPField () {
        return terrainPPField;
    }
    public static WeatherPPField getWeatherPPField () {
        return weatherPPField;
    }

    public boolean isThereEnemyAround(double distance) {
        for (ArmyAllyOrdering army : divisions.getArmies().values()) {
            if (army.isHaveTargetArmyAround(CustomParams.safetyDistance)) {
                return true;
            }
        }

        return false;
    }

    private static Commander instance;
    public static Commander getInstance() {
        if (instance == null) {
            instance = new Commander();
        }

        return instance;
    }

}
