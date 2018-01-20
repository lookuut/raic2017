package strategy;

import model.*;

import java.util.*;
import java.util.function.Consumer;


public final class MyStrategy implements Strategy {

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

    private static Map<Long, SmartVehicle> vehicles;
    private static Map<Long, SmartVehicle> enemyVehicles;

    private static Map<VehicleType, Integer> allyVehiclesByType;
    private static Map<VehicleType, Integer> enemyVehiclesByType;

    public static Profiler profiler;

    public static Commander commander;
    private long strategyTimeSum;

    public MyStrategy() {
        this.previousVehiclesStates = new HashMap();
        this.vehicles = new HashMap<>();
        this.enemyVehicles = new HashMap<>();

        this.commanderFacility = new CommanderFacility();

        this.allyVehiclesByType = new HashMap<>();
        this.enemyVehiclesByType = new HashMap<>();

        for (VehicleType type : VehicleType.values()) {
            allyVehiclesByType.put(type, 0);
            enemyVehiclesByType.put(type, 0);
        }

        strategyTimeSum = 0;
        profiler = new Profiler();
    }

    protected void init(Player me, World world, Game game, Move move) throws Exception {
        this.player = me;
        this.world = world;
        this.game = game;
        this.move = move;

        if (this.battleField == null) {
            battleField = new BattleField(CustomParams.tileCellSize);
            commander = Commander.getInstance();
            enemyField = new EnemyField();

            System.out.println("Seed : " + MyStrategy.game.getRandomSeed());
        }

    }

    public void updatePreviousVehiclesStates (World world) throws Exception {

        for (Vehicle vehicle : world.getNewVehicles()) {
            this.previousVehiclesStates.put(vehicle.getId(), new SmartVehicle(vehicle));
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
        long startTime = System.currentTimeMillis();
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
            strategyTimeSum += System.currentTimeMillis() - startTime;
            String log = String.format("Tick %d, commands count %d, tick mills %d, seconds sum %d",
                    world.getTickIndex(),
                    me.getRemainingActionCooldownTicks(),
                    System.currentTimeMillis() - startTime,
                    strategyTimeSum/1000);
            System.out.println(log);
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
                if (army.isAlive() && !vehicle.isAlly()) {
                    army.setEnemy(vehicle);
                }

                if (army.isAlive()) {
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
                    smartVehicle = new SmartVehicle(vehicle);
                    vehicles.put(smartVehicle.getId(), smartVehicle);
                } else {
                    smartVehicle.vehicleUpdate(vehicle);
                }

                if (smartVehicle.getPlayerId() == MyStrategy.getEnemyPlayerId()) {
                    enemyVehicles.put(smartVehicle.getId() , smartVehicle);
                    enemyVehiclesByType.put(smartVehicle.getType(), enemyVehiclesByType.get(smartVehicle.getType()) + 1);
                } else {
                    allyVehiclesByType.put(smartVehicle.getType(), allyVehiclesByType.get(smartVehicle.getType()) + 1);
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

                    smartVehicle.vehicleUpdate(vehicleUpdate);

                    if (smartVehicle.getPlayerId() == MyStrategy.getEnemyPlayerId()) {
                        enemyVehicles.put(smartVehicle.getId() , smartVehicle);
                    }

                    commander.result(smartVehicle);

                    battleField.addVehicle(smartVehicle);
                    updateVehiclesInArmies.accept(smartVehicle);

                    if (smartVehicle.getDurability() == 0) {
                        vehicles.remove(smartVehicle.getId());
                        if (enemyVehicles.containsKey(smartVehicle.getId())) {
                            enemyVehicles.remove(smartVehicle.getId());
                        }

                        if (smartVehicle.getPlayerId() == MyStrategy.getEnemyPlayerId()) {
                            enemyVehicles.put(smartVehicle.getId() , smartVehicle);
                            enemyVehiclesByType.put(smartVehicle.getType(), enemyVehiclesByType.get(smartVehicle.getType()) - 1);
                        } else {
                            allyVehiclesByType.put(smartVehicle.getType(), allyVehiclesByType.get(smartVehicle.getType()) - 1);
                        }
                    }

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
        return (world.getOpponentPlayer().getNextNuclearStrikeTickIndex() - world.getTickIndex()) > 0;
    }

    public static boolean mayEnemyAttackNuclearSoon () {
        return MyStrategy.world.getOpponentPlayer().getRemainingNuclearStrikeCooldownTicks() > 0 && MyStrategy.world.getOpponentPlayer().getNextNuclearStrikeTickIndex() == 0;
    }

    public static Player nuclearAttack() {
        Player player = Arrays.stream(MyStrategy.world.getPlayers()).filter(player1 -> player1.getNextNuclearStrikeTickIndex() > 0).findFirst().orElse(null);
        return player;
    }

    public static Long getEnemyPlayerId() {
        return MyStrategy.world.getOpponentPlayer().getId();
    }

    public static boolean isHaveFacilitiesToSiege() {
        return Arrays.stream(MyStrategy.world.getFacilities()).filter(facility -> facility.getOwnerPlayerId() != MyStrategy.world.getMyPlayer().getId()).count() > 0;
    }
    /**
     * weather and terrain static data
     */
    private static Integer weatherTerrainWidthPropose;
    private static Integer weatherTerrainHeightPropose;

    public static int getWeatherTerrainWidthPropose() {
        if (weatherTerrainWidthPropose == null) {
            weatherTerrainWidthPropose = (int) MyStrategy.world.getWidth() / MyStrategy.game.getTerrainWeatherMapColumnCount();
        }
        return weatherTerrainWidthPropose;
    }

    public static int getWeatherTerrainHeightPropose() {
        if (weatherTerrainHeightPropose == null) {
            weatherTerrainHeightPropose = (int) MyStrategy.world.getHeight() / MyStrategy.game.getTerrainWeatherMapRowCount();
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

    public static Map<Long, SmartVehicle> getVehicles() {
        return vehicles;
    }

    public static Map<Long, SmartVehicle> getEnemyVehicles() {
        return enemyVehicles;
    }

    public static Map<VehicleType, Integer> getEnemyTypeVehiclesCount() {
        return enemyVehiclesByType;
    }

    public static Map<VehicleType, Integer> getAllyTypeVehiclesCount() {
        return allyVehiclesByType;
    }
}