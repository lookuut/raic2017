public class CustomParams {
    //path search
    public static float allyUnitPPFactor = 1000000;
    public static int pathFinderSectorCount = 24;
    public static int pathSegmentLenght = 80;

    //search safety point sector count
    public static int searchSafetyZoneSectorCount = 12;
    public static int safetyDistance = 160;
    public static int dangerRadious = 160;

    public static float armyScaleFactor = 0.1F;
    public static int enemyArmiesMaxSize = 20;

    public static int maxLinearPPRange = 8;
    public static int noAssignGroupId = 0;


    public static double onFacilityEps = 2.0;
    public static double onHealerEps = 2.0;

    public static double nearestEnemyEps = 0.01;

    //armies config
    public static double compactAngle = Math.PI / 4;
    public static int minVehiclesCountInArmy = 25;
    public static int maxVehiclesCountInArmy = 25;
    public static double maxSizeVehicleInArmy = 1.0;//for scale

    //commands config
    public static int runImmediatelyTick = -1;

    public static int tileCellSize = 16;

    //
    public static int max_player_index = 2;

    public static float minEnemyPercentageToNuclearAttack = 0.1f;
    //Battle field config
    public static int fieldMaxWidth = 1024;
    public static int fieldMinHeight = 1024;

    public static int calculationPathInterval = 2;

    public static int trackMinTickInhistory = 60;

    //nuclear attack
    public static int nuclearAttackRatingRecalcTickInterval = 10;

    public static int borderPointsCount = 12;
    public static double percentOfHeatedVehicles = 0.75;
    //facilities
    public static int maxGotoSiegeArmyCount = 1;

    //compact
    public static int armyCompactTimeout = 200;
    public static double movingDelta = 0.0001;
}
