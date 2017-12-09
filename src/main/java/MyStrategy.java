import model.*;

import java.util.*;
import java.util.function.Consumer;


public final class MyStrategy implements Strategy {

    private static List<Point2D> borderPointList;
    public static Player player;
    public static World world;
    public static Game game;
    public static Move move;

    public static CommanderFacility commanderFacility;

    private static WeatherType[][] weatherMap;
    private static TerrainType[][] terrainMap;

    public static BattleField battleField;
    public static EnemyField enemyField;

    static protected HashMap<Long, SmartVehicle> previousVehiclesStates;
    private static  HashMap<Long, SmartVehicle> vehicles;


    protected ArmyDamageField armyDamageField;
    public static  Commander commander;

    public MyStrategy() {
        this.previousVehiclesStates = new HashMap();
        this.vehicles = new HashMap<>();
        this.commanderFacility = new CommanderFacility();

        CommandQueue.getInstance().addPriority(CustomParams.noAssignGroupId, 0);
    }

    protected void init(Player me, World world, Game game, Move move) throws Exception {
        this.player = me;
        this.world = world;
        this.game = game;
        this.move = move;

        if (this.battleField == null) {
            commander = new Commander(this);
            battleField = new BattleField(CustomParams.tileCellSize);
            enemyField = new EnemyField(battleField);

            //what is this shit ?
            armyDamageField = new ArmyDamageField(this);
            System.out.println("Seed : " + MyStrategy.game.getRandomSeed());
        }

    }

    public void updatePreviousVehiclesStates (World world) throws Exception {

        for (Vehicle vehicle : world.getNewVehicles()) {
            this.previousVehiclesStates.put(vehicle.getId(), new SmartVehicle(vehicle, this));
        }

        for (VehicleUpdate vehicleUpdate : world.getVehicleUpdates()) {
            if (!this.previousVehiclesStates.containsKey(vehicleUpdate.getId())) {
                throw new Exception("This is bad situation, unknown vehicle ID");
            }

            SmartVehicle vehicle = this.previousVehiclesStates.get(vehicleUpdate.getId());
            vehicle.vehicleUpdate(vehicleUpdate);
        }
    }

    @Override
    public void move(Player me, World world, Game game, Move move) {
        try {
            this.init(me, world, game, move);
            this.updateWorld(world);
            this.commander.check();
            this.commander.logic();

            this.updatePreviousVehiclesStates(world);
            CommandQueue.getInstance().run(world.getTickIndex());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Out of tick " + world.getTickIndex() + " commands count " + player.getRemainingActionCooldownTicks());
        }
    }

    public void updateWorld(World world) {
        updateVehicles(world);
        updateFacilities(world);
    }

    private void updateFacilities(World world) {
        for (Facility facility : world.getFacilities()) {
            commanderFacility.updateFacility(facility);
        }
    }

    private void updateVehicles(World world) {

        Collection<ArmyAllyOrdering> armies = commander.getDivisions().getArmyList();

        Consumer<SmartVehicle> updateVehiclesInArmies = (vehicle) -> {
            for (ArmyAllyOrdering army : armies) {
                if (army.isArmyAlive() && !vehicle.isAlly()) {
                    army.setEnemy(vehicle);
                }

                if (army.isArmyAlive()) {
                    army.result(vehicle);
                }
            }
        };
        Arrays.stream(
            world.getNewVehicles()).
            forEach(vehicle -> {
                //update vehicles hashmap
                SmartVehicle smartVehicle = vehicles.get(vehicle.getId());

                if (smartVehicle == null) {
                    smartVehicle = new SmartVehicle(vehicle, this);
                    vehicles.put(smartVehicle.getId(), smartVehicle);
                } else {
                    smartVehicle.vehicleUpdate(vehicle);
                }

                commander.addNoArmyVehicle(smartVehicle);
                commander.result(smartVehicle);

                battleField.addVehicle(smartVehicle);
                updateVehiclesInArmies.accept(smartVehicle);

            });

        Arrays.stream(
            world.getVehicleUpdates()).
            forEach(vehicleUpdate -> {
                try {
                    SmartVehicle smartVehicle = vehicles.get(vehicleUpdate.getId());

                    if (smartVehicle == null) {
                        throw new Exception("Something goes wrong with vehicles");
                    }
                    commander.result(smartVehicle);
                    smartVehicle.vehicleUpdate(vehicleUpdate);
                    battleField.addVehicle(smartVehicle);
                    updateVehiclesInArmies.accept(smartVehicle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
    }

    public Player getPlayer() { return player; }

    public World getWorld() {
        return world;
    }

    public Game getGame() {
        return game;
    }

    public Move getMove() {
        return move;
    }

    public HashMap<Long, SmartVehicle> getPreviousVehiclesStates() {
        return previousVehiclesStates;
    }

    public static SmartVehicle getVehiclePrevState(Long vehicleId) {
        return previousVehiclesStates.get(vehicleId);
    }

    public static boolean isNuclearAttack() {
        Player player = Arrays.stream(MyStrategy.world.getPlayers()).filter(player1 -> player1.getNextNuclearStrikeTickIndex() > 0 && player1.getId() != MyStrategy.player.getId()).findFirst().orElse(null);
        return player != null;
    }

    public static Player nuclearAttack() {
        Player player = Arrays.stream(MyStrategy.world.getPlayers()).filter(player1 -> player1.getNextNuclearStrikeTickIndex() > 0).findFirst().orElse(null);
        return player;
    }

    public static Long getEnemyPlayerId() {
        return MyStrategy.world.getOpponentPlayer().getId();
    }

    public static boolean isHaveFacilities() {
        return MyStrategy.world.getFacilities().length > 0;
    }
    /**
     * weather and terrain static data
     */
    private static Integer weatherTerrainWidthPropose;
    private static Integer weatherTerrainHeightPropose;

    public static int getWeatherTerrainWidthPropose() {
        if (weatherTerrainWidthPropose == null) {
            weatherTerrainWidthPropose = (int)MyStrategy.world.getWidth() / MyStrategy.game.getTerrainWeatherMapColumnCount();
        }
        return weatherTerrainWidthPropose;
    }

    public static int getWeatherTerrainHeightPropose() {
        if (weatherTerrainHeightPropose == null) {
            weatherTerrainHeightPropose = (int)MyStrategy.world.getHeight() / MyStrategy.game.getTerrainWeatherMapRowCount();
        }
        return weatherTerrainHeightPropose;
    }

    public static TerrainType[][] getTerrainMap() {
        if (terrainMap == null) {
            terrainMap = world.getTerrainByCellXY();
        }
        return terrainMap;
    }

    public static WeatherType[][] getWeatherMap() {
        if (weatherMap == null) {
            weatherMap = world.getWeatherByCellXY();
        }
        return weatherMap;
    }

    public static List<Point2D> getBorderPointList () {
        if (borderPointList == null) {
            borderPointList = new ArrayList<>();
            Point2D direction = new Point2D(MyStrategy.world.getWidth(),0);
            for (int i = 0; i  < CustomParams.borderPointsCount; i++) {
                double angle = i * 2 * Math.PI / CustomParams.borderPointsCount;
                Point2D turnedDirection = direction.turn(angle);
                Point2D borderPoint1 = new Point2D(0,0);
                Point2D borderPoint2 = new Point2D(0,0);
                if (turnedDirection.getX() >= 0 && Math.abs(turnedDirection.getX()) > Math.abs(turnedDirection.getY())) {
                    borderPoint1.setX(MyStrategy.world.getWidth());
                    borderPoint2 = new Point2D(MyStrategy.world.getWidth(), MyStrategy.world.getHeight());
                } else if (turnedDirection.getX() < 0 && Math.abs(turnedDirection.getX()) > Math.abs(turnedDirection.getY())) {
                    borderPoint2.setY(MyStrategy.world.getHeight());
                } else if (turnedDirection.getY() >=0 && Math.abs(turnedDirection.getX()) <= Math.abs(turnedDirection.getY()) ) {
                    borderPoint1.setY(MyStrategy.world.getHeight());
                    borderPoint2 = new Point2D(MyStrategy.world.getWidth(), MyStrategy.world.getHeight());
                } else {
                    borderPoint2.setX(MyStrategy.world.getWidth());
                }

                Point2D center = new Point2D(MyStrategy.world.getWidth() / 2, MyStrategy.world.getHeight() / 2);
                Point2D turnedVectorPoint = center.add(turnedDirection);

                borderPointList.add(Point2D.lineIntersect(center, turnedVectorPoint, borderPoint1 ,borderPoint2));
            }
        }

        return borderPointList;
    }

    public static Map<Long, SmartVehicle> getVehicles() {
        return vehicles;
    }
}