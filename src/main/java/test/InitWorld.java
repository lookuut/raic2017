package test;


import model.*;
import strategy.MyStrategy;
import strategy.Point2D;

import java.util.concurrent.atomic.AtomicInteger;

public class InitWorld {
    private World world;
    private InitPlayer initPlayer;
    private Game game;
    private Move move;
    private WeatherType[][] weatherTypes;
    private TerrainType[][] terrainTypes;
    private MyStrategy strategy;

    public InitWorld (Vehicle[] vehicles) {
        move = new Move();
        weatherTypes = new WeatherType[WorldConfig.weatherRowCount][WorldConfig.weatherColumnCount];
        terrainTypes = new TerrainType[WorldConfig.terrainRowCount][WorldConfig.terrainColumnCount];

        for (int x = 0; x < WorldConfig.weatherColumnCount; x++) {
            for (int y = 0; y < WorldConfig.weatherRowCount; y++) {
                weatherTypes[x][y] = WeatherType.CLEAR;
            }
        }

        for (int x = 0; x < WorldConfig.terrainColumnCount; x++) {
            for (int y = 0; y < WorldConfig.terrainRowCount; y++) {
                terrainTypes[x][y] = TerrainType.PLAIN;
            }
        }
        initPlayer = new InitPlayer();
        world = new World(0,
                WorldConfig.tickCount,
                WorldConfig.width,
                WorldConfig.height,
                initPlayer.getPlayers(),
                vehicles,
                new VehicleUpdate[0],
                terrainTypes,
                weatherTypes,
                new Facility[0]);
        game = initGame();
        strategy = new MyStrategy();

        strategy.move(initPlayer.getMe(), world, game, move);
    }

    public static AtomicInteger vehicleId = new AtomicInteger(0);

    public static Vehicle generateVehicle(Point2D point, VehicleType vehicleType, long playerId, int durability, boolean aerial) {
        return new Vehicle(
                vehicleId.incrementAndGet(),
                point.getX(),
                point.getY(),
                WorldConfig.vehicleRadious,
                playerId,
                durability,
                WorldConfig.maxDurability,
                WorldConfig.maxSpeed,
                WorldConfig.maxVisionRange,
                WorldConfig.squaredVisionRange,
                WorldConfig.groundAttackRange,
                WorldConfig.squaredGroundAttackRange,
                WorldConfig.aerialAttackRange,
                WorldConfig.squaredAerialAttackRange,
                WorldConfig.groundDamage,
                WorldConfig.aerialDamage,
                WorldConfig.groundDefence,
                WorldConfig.aerialDefence,
                WorldConfig.attackCooldownTicks,
                WorldConfig.remainingAttackCooldownTicks,
                vehicleType,
                aerial,
                WorldConfig.selected,
                WorldConfig.groups
                );
    }

    public Game initGame () {
        Game game = new Game(GameConfig.randomSeed, GameConfig.tickCount, GameConfig.worldWidth, GameConfig.worldHeight, GameConfig.fogOfWarEnabled,
        GameConfig.victoryScore, GameConfig.facilityCaptureScore, GameConfig.vehicleEliminationScore, GameConfig.actionDetectionInterval,
        GameConfig.baseActionCount, GameConfig.additionalActionCountPerControlCenter, GameConfig.maxUnitGroup,
        GameConfig.terrainWeatherMapColumnCount, GameConfig.terrainWeatherMapRowCount, GameConfig.plainTerrainVisionFactor,
        GameConfig.plainTerrainStealthFactor, GameConfig.plainTerrainSpeedFactor, GameConfig.swampTerrainVisionFactor,
        GameConfig.swampTerrainStealthFactor, GameConfig.swampTerrainSpeedFactor, GameConfig.forestTerrainVisionFactor,
        GameConfig.forestTerrainStealthFactor, GameConfig.forestTerrainSpeedFactor, GameConfig.clearWeatherVisionFactor,
        GameConfig.clearWeatherStealthFactor, GameConfig.clearWeatherSpeedFactor, GameConfig.cloudWeatherVisionFactor,
        GameConfig.cloudWeatherStealthFactor, GameConfig.cloudWeatherSpeedFactor, GameConfig.rainWeatherVisionFactor,
        GameConfig.rainWeatherStealthFactor, GameConfig.rainWeatherSpeedFactor, GameConfig.vehicleRadius, GameConfig.tankDurability,
        GameConfig.tankSpeed, GameConfig.tankVisionRange, GameConfig.tankGroundAttackRange, GameConfig.tankAerialAttackRange,
        GameConfig.tankGroundDamage, GameConfig.tankAerialDamage, GameConfig.tankGroundDefence, GameConfig.tankAerialDefence,
        GameConfig.tankAttackCooldownTicks, GameConfig.tankProductionCost, GameConfig.ifvDurability, GameConfig.ifvSpeed,
        GameConfig.ifvVisionRange, GameConfig.ifvGroundAttackRange, GameConfig.ifvAerialAttackRange, GameConfig.ifvGroundDamage,
        GameConfig.ifvAerialDamage, GameConfig.ifvGroundDefence, GameConfig.ifvAerialDefence, GameConfig.ifvAttackCooldownTicks,
        GameConfig.ifvProductionCost, GameConfig.arrvDurability, GameConfig.arrvSpeed, GameConfig.arrvVisionRange, GameConfig.arrvGroundDefence,
        GameConfig.arrvAerialDefence, GameConfig.arrvProductionCost, GameConfig.arrvRepairRange, GameConfig.arrvRepairSpeed,
        GameConfig.helicopterDurability, GameConfig.helicopterSpeed, GameConfig.helicopterVisionRange,
        GameConfig.helicopterGroundAttackRange, GameConfig.helicopterAerialAttackRange, GameConfig.helicopterGroundDamage,
        GameConfig.helicopterAerialDamage, GameConfig.helicopterGroundDefence, GameConfig.helicopterAerialDefence,
        GameConfig.helicopterAttackCooldownTicks, GameConfig.helicopterProductionCost, GameConfig.fighterDurability, GameConfig.fighterSpeed,
        GameConfig.fighterVisionRange, GameConfig.fighterGroundAttackRange, GameConfig.fighterAerialAttackRange,
        GameConfig.fighterGroundDamage, GameConfig.fighterAerialDamage, GameConfig.fighterGroundDefence, GameConfig.fighterAerialDefence,
        GameConfig.fighterAttackCooldownTicks, GameConfig.fighterProductionCost, GameConfig.maxFacilityCapturePoints,
        GameConfig.facilityCapturePointsPerVehiclePerTick, GameConfig.facilityWidth, GameConfig.facilityHeight,
        GameConfig.baseTacticalNuclearStrikeCooldown, GameConfig.tacticalNuclearStrikeCooldownDecreasePerControlCenter,
        GameConfig.maxTacticalNuclearStrikeDamage, GameConfig.tacticalNuclearStrikeRadius, GameConfig.tacticalNuclearStrikeDelay);

        return game;
    }
}
