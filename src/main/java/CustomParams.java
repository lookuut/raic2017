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


    public static float coorsCeil(double value) {
        return (float)(Math.ceil(value * 100) / 100.00);
    }
}
