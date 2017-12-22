import model.VehicleType;


import java.util.*;

class Commander {

    private ArmyDivisions divisions;

    protected BehaviourTree<ArmyAlly> behaviourTree;
    protected Queue<BTreeAction> activeActions;

    private static CommandNuclearAttack nuclearAttack;

    private Map<VehicleType, List<SmartVehicle>> noArmyVehicles;
    private Map<VehicleType, Square> noArmySquaereMap;
    private CommandNuclearDefence nuclearAttackDefence;
    private static Deque<CommanderTask> tasksQueue;

    private static TerrainPPField terrainPPField;
    private static WeatherPPField weatherPPField;

    private TerrainArmiesForm terrainArmiesForm;

    /**
     * @param strategy
     */
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

        terrainArmiesForm = new TerrainArmiesForm(divisions);
    }

    private void checkAttackNuclear() throws Exception {
        if (nuclearAttackDefence != null) {
            nuclearAttackDefence.check(null);
        }

        if (MyStrategy.isNuclearAttack() && !(nuclearAttackDefence.isRun() || nuclearAttackDefence.isHold())) {
            nuclearAttackDefence.run(null);
        }
    }

    public void logic () throws Exception {
        constructArmies();
        terrainArmiesForm.searchExpansionArmy();
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
            if (army.isArmyAlive()) {
                army.run();
            }
        }
    }

    public void constructArmies() {
        for (Map.Entry<VehicleType, List<SmartVehicle>> entry : noArmyVehicles.entrySet()) {
            try {
                if (entry.getValue().size() > CustomParams.minVehiclesCountInArmy) {
                    Square vehicleTypeSquare = noArmySquaereMap.get(entry.getKey());

                    if (entry.getKey() == VehicleType.HELICOPTER && false) {
                        double centreX = (vehicleTypeSquare.getRightTopAngle().getX() - vehicleTypeSquare.getLeftBottomAngle().getX()) / 2;
                        double centreY = (vehicleTypeSquare.getRightTopAngle().getY() - vehicleTypeSquare.getLeftBottomAngle().getY()) / 2;
                        Point2D centrePoint = new Point2D(vehicleTypeSquare.getLeftBottomAngle().getX() + centreX, vehicleTypeSquare.getLeftBottomAngle().getY() + centreY);

                        Square armySquare = new Square(vehicleTypeSquare.getLeftBottomAngle(), centrePoint.add(new Point2D(0, centreY)));
                        divisions.addArmy(armySquare, new HashSet(Arrays.asList(entry.getKey())));

                        armySquare = new Square(new Point2D(vehicleTypeSquare.getLeftBottomAngle().getX() + centreX, vehicleTypeSquare.getLeftBottomAngle().getY()), vehicleTypeSquare.getRightTopAngle());
                        divisions.addArmy(armySquare, new HashSet(Arrays.asList(entry.getKey())));
                    } else if (entry.getKey() == VehicleType.FIGHTER || entry.getKey() == VehicleType.HELICOPTER) {
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
            if (!army.isArmyAlive()) {//@TODO boolshit
                divisions.removeArmy(army);
                iter.remove();
            } else if (army.isRun()) {
                army.check();
            }
        }
    }

    public ArmyDivisions getDivisions() {
        return divisions;
    }

    public void nuclearAttack () {
        try {

            if (isDelayNuclearAttack() || MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() > 0 || divisions.getArmies().size() == 0) {
                return;
            }

            for (NuclearAttackPoint attackPoint : MyStrategy.enemyField.getNuclearAttackPointsRating()) {
                for (ArmyAllyOrdering army : divisions.getArmyList()) {
                    if (army.getForm().isPointInDistance(attackPoint.getPoint(), MyStrategy.game.getTacticalNuclearStrikeRadius())) {//point in allow distance need cabum
                        SmartVehicle vehicle = army.getGunnerVehicle(attackPoint.getPoint());
                        Point2D fromPoint = vehicle.getPoint();
                        Point2D targetVector = attackPoint.getPoint().subtract(fromPoint);

                        double visionRange = vehicle.getMinVisionRange();

                        if (army.getRunningCommand() instanceof  CommandMove) {
                            CommandMove command = ((CommandMove) army.getRunningCommand());
                            Point2D moveTargetVector = command.getTargetVector();
                            double angle = moveTargetVector.angle(targetVector);
                            if (angle >= 90 && angle < 270) {
                                fromPoint = vehicle.getVehiclePointAtTick(moveTargetVector.normalize(), Math.min(MyStrategy.game.getTacticalNuclearStrikeDelay(),command.getMaxRunnableTick()));
                            }
                        }

                        Point2D targetPoint = attackPoint.getPoint();
                        Point2D targetPointDirection = attackPoint.getPoint().subtract(fromPoint);

                        if (targetPointDirection.magnitude() > visionRange) {
                            targetPoint = targetPointDirection.multiply(visionRange / targetPointDirection.magnitude()).add(fromPoint);
                        }

                        nuclearAttack = new CommandNuclearAttack(vehicle, targetPoint);
                        nuclearAttack.run(army);
                        break;
                    }
                }
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
            terrainArmiesForm.addNewVehicle(vehicle);
            List<SmartVehicle> vehicleList = noArmyVehicles.get(vehicle.getType());
            vehicleList.add(vehicle);
        }
    }

    static public boolean isDelayNuclearAttack () {
        if (Commander.nuclearAttack == null || Commander.nuclearAttack.isFinished()) {
            return false;
        }

        return Commander.nuclearAttack.check(null);
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

    public void armyFormsResult(SmartVehicle vehicle) {
        terrainArmiesForm.updateVehicle(vehicle);
    }

    public boolean isThereEnemyAround(double distance) {
        for (ArmyAllyOrdering army : divisions.getArmies().values()) {
            if (army.isHaveEnemyAround(CustomParams.safetyDistance)) {
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