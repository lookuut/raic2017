package test;

public class WorldConfig {
    //world config
    public static double width = 1024.0;
    public static double height = 1024.0;

    public static int weatherColumnCount = 32;
    public static int terrainColumnCount = 32;

    public static int weatherRowCount = 32;
    public static int terrainRowCount = 32;

    public static int tickCount = 20000;

    // vehicle config
    public static long mePlayerId = 1;
    public static long enemyPlayerid = 2;
    public static double vehicleRadious = 2.0;
    public static int maxDurability = 100;
    public static double maxSpeed = 1.2;
    public static int maxVisionRange = 100;

    public static double squaredVisionRange = 10000;
    public static double groundAttackRange = 20;

    public static double squaredGroundAttackRange = 400;
    public static double aerialAttackRange = 20;
    public static double squaredAerialAttackRange = 400;
    public static int groundDamage = 100;
    public static int aerialDamage = 100;
    public static int groundDefence = 60;
    public static int aerialDefence = 60;
    public static int attackCooldownTicks = 60;
    public static int remainingAttackCooldownTicks = 0;
    public static boolean selected = false;
    public static int[] groups = new int[0];
}
