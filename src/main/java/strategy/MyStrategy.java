package strategy;

import model.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


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

    private static Map<Long, SmartVehicle> smartVehicles;

    private static Map<Long, Map<Long, SmartVehicle>> playerVehicles;
    private static Map<Long, Map<VehicleType, Integer>> playerVehiclesByType;


    public static Profiler profiler;

    public static Commander commander;
    private long strategyTimeSum;

    public MyStrategy() {
        this.previousVehiclesStates = new HashMap();
        this.smartVehicles = new HashMap<>();

        playerVehicles = new HashMap<>();
        playerVehicles.put(1l, new HashMap<>());
        playerVehicles.put(2l, new HashMap<>());

        playerVehiclesByType = new HashMap<>();
        playerVehiclesByType.put(1l, new HashMap<>());
        playerVehiclesByType.put(2l, new HashMap<>());

        this.commanderFacility = new CommanderFacility();

        for(Map<VehicleType, Integer> typeCounter : playerVehiclesByType.values()) {
            for (VehicleType type : VehicleType.values()) {
                typeCounter.put(type, 0);
            }
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

    public static SmartVehicle getSmartVehicle(Vehicle vehicle) {
        SmartVehicle smartVehicle = smartVehicles.get(vehicle.getId());

        if (smartVehicle == null) {
            smartVehicle = new SmartVehicle(vehicle);
            smartVehicles.put(smartVehicle.getId(), smartVehicle);
        } else {
            smartVehicle.vehicleUpdate(vehicle);
        }

        return smartVehicle;
    }

    public static SmartVehicle getSmartVehicle(VehicleUpdate vehicleUpdate) {
        SmartVehicle smartVehicle = smartVehicles.get(vehicleUpdate.getId());
        smartVehicle.vehicleUpdate(vehicleUpdate);
        return smartVehicle;
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

        Vehicle[] vehicleArray = world.getNewVehicles();

        List<Vehicle> allyVehicleList= Arrays.stream(vehicleArray).filter(vehicle -> vehicle.getPlayerId() == MyStrategy.player.getId()).collect(Collectors.toList());
        List<Vehicle> enemyVehicleList = Arrays.stream(vehicleArray).filter(vehicle -> vehicle.getPlayerId() == MyStrategy.getEnemyPlayerId()).collect(Collectors.toList());

        Arrays.stream(vehicleArray).
            forEach(vehicle -> {
                //update vehicles hashmap
                SmartVehicle smartVehicle = MyStrategy.getSmartVehicle(vehicle);

                playerVehicles.get(smartVehicle.getPlayerId()).put(smartVehicle.getId(), smartVehicle);
                playerVehiclesByType.get(
                        smartVehicle.getPlayerId()
                ).put(smartVehicle.getType(),
                        playerVehiclesByType.get(smartVehicle.getPlayerId()).get(smartVehicle.getType()) + 1);

                battleField.addVehicle(smartVehicle);

                if (smartVehicle.isAlly()) {
                    commander.addNoArmyVehicle(smartVehicle);
                }

                commander.result(smartVehicle);
                updateVehiclesInArmies.accept(smartVehicle);

                List<Vehicle> vehicles;
                if (smartVehicle.isAlly()) {
                    vehicles = allyVehicleList;
                } else {
                    vehicles = enemyVehicleList;
                }

                for (Vehicle _vehicle : vehicles) {
                    SmartVehicle sVehicle = MyStrategy.getSmartVehicle(_vehicle);
                    smartVehicle.updateNearVehicle(sVehicle);
                }
            });

        VehicleUpdate[] vehicleUpdatesArray = world.getVehicleUpdates();

        Arrays.stream(vehicleUpdatesArray).
            forEach(vehicleUpdate -> {
                SmartVehicle smartVehicle = MyStrategy.getSmartVehicle(vehicleUpdate);

                playerVehicles.get(smartVehicle.getPlayerId()).put(smartVehicle.getId(), smartVehicle);

                commander.result(smartVehicle);

                battleField.addVehicle(smartVehicle);
                updateVehiclesInArmies.accept(smartVehicle);

                if (smartVehicle.getDurability() == 0) {
                    for (Long vehicleId : smartVehicle.getNearVehicles()) {
                        MyStrategy.getVehicles().get(vehicleId).removeNearVehicle(smartVehicle.getId());
                    }
                } else if (smartVehicle.canNearVehicleUpdate()){
                    for (SmartVehicle vUpdate : playerVehicles.get(smartVehicle.getPlayerId()).values()) {
                        smartVehicle.updateNearVehicle(vUpdate);
                    }
                }

                if (smartVehicle.getDurability() == 0) {
                    smartVehicles.remove(smartVehicle.getId());

                    playerVehicles.get(smartVehicle.getPlayerId()).remove(smartVehicle.getId());
                    playerVehiclesByType.get(smartVehicle.getPlayerId()).put(
                            smartVehicle.getType(),
                            playerVehiclesByType.get(smartVehicle.getPlayerId()).get(smartVehicle.getType()) - 1
                    );
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
        return smartVehicles;
    }

    public static Map<Long, SmartVehicle> getEnemyVehicles() {
        return playerVehicles.get(MyStrategy.getEnemyPlayerId());
    }

    public static Map<Long, SmartVehicle> getAllyVehicles() {
        return playerVehicles.get(MyStrategy.player.getId());
    }

    public static Map<VehicleType, Integer> getEnemyTypeVehiclesCount() {
        return playerVehiclesByType.get(MyStrategy.getEnemyPlayerId());
    }

    public static Map<VehicleType, Integer> getAllyTypeVehiclesCount() {
        return playerVehiclesByType.get(MyStrategy.player.getId());
    }
}