import model.*;

import java.awt.*;
import java.io.IOException;
import java.util.Locale;

public final class Runner {
    private final RemoteProcessClient remoteProcessClient;
    private final String token;

    public static void main(String[] args) throws IOException {
        new Runner(args.length == 3 ? args : new String[] {"127.0.0.1", "31001", "0000000000000000"}).run();
    }

    private Runner(String[] args) throws IOException {
        remoteProcessClient = new RemoteProcessClient(args[0], Integer.parseInt(args[1]));
        token = args[2];
    }

    @SuppressWarnings("WeakerAccess")
    public void run() throws IOException {
        try {
            // REMOVE CODE
            //RewindClient rc = new RewindClient();
            // REMOVE CODE
            remoteProcessClient.writeTokenMessage(token);
            remoteProcessClient.writeProtocolVersionMessage();
            remoteProcessClient.readTeamSizeMessage();
            Game game = remoteProcessClient.readGameContextMessage();

            Strategy strategy = new MyStrategy();

            PlayerContext playerContext;

            while ((playerContext = remoteProcessClient.readPlayerContextMessage()) != null) {
                Player player = playerContext.getPlayer();
                if (player == null) {
                    break;
                }

                Move move = new Move();
                strategy.move(player, playerContext.getWorld(), game, move);
                // REMOVE CODE
                //rewindViewer(rc);
                // REMOVE CODE
                remoteProcessClient.writeMoveMessage(move);
            }

            // REMOVE CODE
            //rc.close();
            // REMOVE CODE
        } finally {
            remoteProcessClient.close();
        }
    }



    public void rewindViewer(RewindClient rc) {

        int worldSize = 1024;
        for (SmartVehicle vehicle : MyStrategy.getVehicles().values()) {
            //Color rndColor = new Color((int) (255 * 255 * 255 * Math.random()));

            rc.livingUnit(
                    vehicle.getPoint().getIntX(),
                    vehicle.getPoint().getIntY(),
                    vehicle.getRadius(),
                    vehicle.getDurability(),
                    vehicle.getMaxDurability(),
                    vehicle.isAlly() ? RewindClient.Side.OUR : RewindClient.Side.ENEMY,
                    Math.PI ,
                    RewindClient.vehicleType(vehicle.getType()),
                    vehicle.getRemainingAttackCooldownTicks(),
                    vehicle.getAttackCooldownTicks(),
                    vehicle.getSelected());
        }
        PPField damageField = MyStrategy.enemyField.getDamageField(VehicleType.FIGHTER);
        float maxValue = damageField.getMaxValue();
        float minValue = damageField.getMinValue();
        for (int j = 0; j < MyStrategy.enemyField.getHeight(); j++) {
            for (int i = 0; i <  MyStrategy.enemyField.getWidth(); i++) {
                float factor = damageField.getFactor(i, j);

                Point2D point = damageField.getWorldPoint(new Point2D(i,j));
                Color color = new Color(0f,0f,0f);

                if (factor > 0) {
                    factor = 100 * factor/maxValue > 1 ? 1 : 100 * factor/maxValue;
                    color = new Color(factor, 0f,0f);
                } else if (factor < 0) {
                    factor = 100 * factor/minValue > 1 ? 1 : 100 * factor/minValue;
                    color = new Color(factor,  factor,factor);
                }

                rc.rect(point.getX(), point.getY(), point.getX() + damageField.cellSize(), point.getY() + damageField.cellSize(), color, 1);
            }
        }

        rc.endFrame();
    }
}
