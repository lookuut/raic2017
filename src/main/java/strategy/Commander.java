package strategy;

import model.VehicleType;

import java.util.*;

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

    public void logic () throws Exception {
        constructArmies();
        checkAttackNuclear();
        nuclearAttack();

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
                        entry.getKey() == VehicleType.TANK ||
                        entry.getKey() == VehicleType.IFV ||
                        entry.getKey() == VehicleType.ARRV) {
                        divisions.addArmy(vehicleTypeSquare, new HashSet(Arrays.asList(entry.getKey())));
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

            for (NuclearAttackPoint point : nuclearAttackPointSortedSet) {
                for (ArmyAllyOrdering army : divisions.getArmyList()) {
                    double distance = army.getForm().getEdgesVehiclesCenter().subtract(point.getPoint()).magnitude();
                    if (minTime > distance / army.getSpeed()) {
                        minTime = distance / army.getSpeed();
                        minTimeArmy = army;
                        minTimeNuclearTarget = point.getPoint();
                    }
                }
            }

            nuclearAttackTarget = minTimeNuclearTarget;
            nuclearAttackArmy = minTimeArmy;


            if (nuclearAttackArmy.getRunningCommand() instanceof CommandAttack) {
                nuclearAttackArmy.getRunningCommand().setState(CommandStates.Complete);
            }

            if (nuclearAttackArmy.getForm().isPointInVisionRange(getNuclearAttackTarget()) &&
                    nuclearAttackArmy.getRunningCommand() instanceof CommandMove/* &&
                    !nuclearAttackArmy.getForm().isDamagedByNuclearAttack(getNuclearAttackTarget())*/) {

                nuclearAttackArmy.getRunningCommand().setState(CommandStates.Complete);
                nuclearAttackArmy.addCommand(new CommandStop());
            }

        } catch (Exception e)  {
            e.printStackTrace();
        }
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