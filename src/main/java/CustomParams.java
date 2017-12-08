public class CustomParams {
    //path search
    public static float allyUnitPPFactor = 10000;
    public static int pathFinderSectorCount = 24;
    public static int pathSegmentLenght = 60;
    public static float minPathFactor = 5000;
    public static float borderFactor = 1000;
    //search safety point sector count
    public static int searchSafetyZoneSectorCount = 12;
    public static int safetyDistance = 160;
    public static int dangerRadious = 80;
    public static int enemyVisionRange = 30;

    public static float armyScaleFactor = 0.1F;
    public static int armyAfterCreateTimeWait = 30;

    public static int maxLinearPPRange = 3;
    public static int noAssignGroupId = 0;

    public static double positionAtMapEps = 0.5;
    public static double onFacilityEps = 2.0;
    public static double onHealerEps = 2.0;

    public static double nearestEnemyEps = 0.1;

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

    //Battle field config
    public static int fieldMaxWidth = 1024;
    public static int fieldMinHeight = 1024;

    public static int calculationPathInterval = 4;

    public static int trackMinTickInhistory = 60;

    //nuclear attack
    public static int nuclearAttackRatingRecalcTickInterval = 1;
    public static int nuclearAttackRatingItemCount = 3;
    public static int gunnerMinDurability = 100;

    public static int borderPointsCount = 12;
    public static double minVehicleDurabilityToHeal = 70;
    public static double minVehicleToHealCount = 20;
    //facilities
    public static int maxGotoSiegeArmyCount = 2;

}
