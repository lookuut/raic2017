import model.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public final class MyStrategy implements Strategy {

    public static Player player;
    public static World world;
    public static Game game;
    public static Move move;

    public static BattleField battleField;

    protected HashMap<Long, SmartVehicle> previousVehiclesStates;
    protected HashMap<Long, SmartVehicle> vehicles;

    protected Long myPlayerId;
    protected ArmyDamageField armyDamageField;

    public static Integer max_player_index = 2;
    protected Commander commander;

    public MyStrategy() {
        this.previousVehiclesStates = new HashMap();
        this.vehicles = new HashMap<>();
        this.commander = new Commander(this);
    }

    public double x;
    public double y;

    protected void init(Player me, World world, Game game, Move move){
        this.player = me;
        this.world = world;
        this.game = game;
        this.move = move;
        this.myPlayerId = this.player.getId();

        if (this.battleField == null) {
            this.battleField = new BattleField();
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
            this.battleField.formArmies();
            this.commander.logic(battleField);

            this.commander.check();
            this.commander.run();
            //this.armyDamageField.defineArmyForm();
            this.updatePreviousVehiclesStates(world);

            CommandQueue.getInstance().run(world.getTickIndex());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (world.getTickIndex() % 200 == 0) {
                System.out.println("Out of tick " + world.getTickIndex() + " my player id " + this.myPlayerId);
            }
        }

    }

    public void armyFieldAnalisys(World world) {
        List<Command> runningCommands = commander.getRunningCommands();
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
                CommandQueue.getInstance().prevCommandRunResult(smartVehicle);

                for (Command command : runningCommands) {
                    command.result(smartVehicle);
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

                    for (Command command : runningCommands) {
                        command.result(smartVehicle);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
        });
    }

    public Long getMyPlayerId() {
        return myPlayerId;
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
}