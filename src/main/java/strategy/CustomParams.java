package strategy;

public class CustomParams {
    //HEAL
    public static int endHealAVGDurability = 99;
    //path search
    public static float allyUnitPPFactor = 1000000;
    public static int pathFinderSectorCount = 24;
    public static int pathSegmentLenght = 80;
    public static int minPathFactor = 30;

    //search safety point sector count

    public static int healSafetyDistance = 120;
    public static int safetyDistance = 200;
    public static int doCompactDistance = 100;

    public static float armyScaleFactor = 0.1F;

    public static int maxLinearPPRange = 8;
    public static int noAssignGroupId = 0;


    public static double onFacilityEps = 2.0;
    public static double onHealerEps = 10.0;

    public static double nearestEnemyEps = 0.01;

    //armies config
    public static double enemyDamageFactor = 1.5;
    public static double compactAngle = Math.PI / 4;
    public static int minVehiclesCountInArmy = 20;

    public static int healTimeout = 30;
    //commands config
    public static int runImmediatelyTick = -1;

    public static int tileCellSize = 16;

    public static int enemyArmiesDefineInterval = 10;
    //
    public static int max_player_index = 2;

    public static float minEnemyPercentageToNuclearAttack = 0.1f;
    //Battle field config
    public static int fieldMaxWidth = 1024;
    public static int fieldMinHeight = 1024;

    public static int calculationPathInterval = 2;

    public static int trackMinTickInhistory = 60;

    //nuclear attack
    public static int nuclearAttackDefenceScaleFactor = 10;
    public static int nuclearAttackRatingRecalcTickInterval = 10;

    public static int borderPointsCount = 36;
    public static double percentOfHeatedVehicles = 0.75;

    //compact
    public static int armyCompactTimeout = 200;
    public static double damageDelta = 1.0;

}
