import model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public final class MyStrategy implements Strategy {

    public static Player player;
    public static World world;
    public static Game game;
    public static Move move;

    private static WeatherType[][] weatherMap;
    private static TerrainType[][] terrainMap;

    public static BattleField battleField;
    public static EnemyField enemyField;

    protected HashMap<Long, SmartVehicle> previousVehiclesStates;
    protected HashMap<Long, SmartVehicle> vehicles;

    protected ArmyDamageField armyDamageField;
    public static  Commander commander;

    public MyStrategy() {
        this.previousVehiclesStates = new HashMap();
        this.vehicles = new HashMap<>();

        //set armies start priorities
        CommandQueue.getInstance().addPriority(CustomParams.fighterArmyId);
        CommandQueue.getInstance().addPriority(CustomParams.helicopterArmyId);
        CommandQueue.getInstance().addPriority(CustomParams.tankArmyId);
        CommandQueue.getInstance().addPriority(CustomParams.ifvArmyId);
        CommandQueue.getInstance().addPriority(CustomParams.arrvArmyId);
        CommandQueue.getInstance().addPriority(CustomParams.allArmyId);
        CommandQueue.getInstance().addPriority(CustomParams.noAssignGroupId);
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
            commander.initStaticPPField();

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

            this.armyFieldAnalisys(world);

            this.commander.logic(battleField);
            this.commander.check();
            this.updatePreviousVehiclesStates(world);
            CommandQueue.getInstance().run(world.getTickIndex());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Out of tick " + world.getTickIndex() + " commands count " + player.getRemainingActionCooldownTicks());
        }
    }

    public void armyFieldAnalisys(World world) {
        List<ArmyAllyOrdering> activeArmy = commander.getArmyRunningCommands();
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

                battleField.addVehicle(smartVehicle);

                for (ArmyAllyOrdering army : activeArmy) {
                    army.result(smartVehicle);
                }
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
                    battleField.addVehicle(smartVehicle);

                    for (ArmyAllyOrdering army : activeArmy) {
                        army.result(smartVehicle);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
    }

    public Player getPlayer() {
        return player;
    }

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

    public SmartVehicle getVehiclePrevState(Long vehicleId) {
        return previousVehiclesStates.get(vehicleId);
    }

    public static boolean canNuclearAttack() {
        return MyStrategy.player.getRemainingNuclearStrikeCooldownTicks() == 0;
    }

    public static boolean isNuclearAttack() {
        Player player = Arrays.stream(MyStrategy.world.getPlayers()).filter(player1 -> player1.getNextNuclearStrikeTickIndex() > 0).findFirst().orElse(null);
        return player != null;
    }

    public static Player nuclearAttack() {
        Player player = Arrays.stream(MyStrategy.world.getPlayers()).filter(player1 -> player1.getNextNuclearStrikeTickIndex() > 0).findFirst().orElse(null);
        return player;
    }

    public static Long getEnemyPlayerId() {
        return MyStrategy.world.getOpponentPlayer().getId();
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
}