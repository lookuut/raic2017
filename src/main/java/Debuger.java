public class Debuger {
    public static void debug(Integer tick) {

        if (MyStrategy.world.getTickIndex() == tick) {
            System.out.println("Debug case happend");
        }
    }
}
