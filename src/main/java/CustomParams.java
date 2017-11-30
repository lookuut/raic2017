public class CustomParams {
    public static int allyUnitPPFactor = 10000;
    public static int pathFinderSectorCount = 24;
    public static int pathSegmentLenght = 60;
    public static int safetyDistance = 160;

    public static int needToScaleFactor = 10;
    public static float armyScaleFactor = 0.1F;
    public static int armyScaleMaxTime = 10;
    public static float maxVehicleDistInArmy = 60;
    public static int maxLinearPPRange = 3;
    public static int noAssignGroupId = 0;

    public static float coorsCeil(double value) {
        return (float)(Math.ceil(value * 100) / 100.00);
    }
    public static double compareDobuleEps = 1.0;

    //armies config
    public static int arrvArmyId = 1;
    public static int fighterArmyId = 2;
    public static int helicopterArmyId = 3;
    public static int tankArmyId = 4;
    public static int ifvArmyId = 5;
    public static int allArmyId = 6;

    public static int equalTypeAttackFactor = 5;
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

}
