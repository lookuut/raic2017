package test;

import model.Player;

public class InitPlayer {
    private Player me;
    private Player enemy;

    public InitPlayer() {
        me = new Player(WorldConfig.mePlayerId, true, false, 0, 0,
        0, 0, 0,
        0, 0);


        enemy = new Player(WorldConfig.enemyPlayerid, false, false, 0, 0,
                0, 0, 0,
                0, 0);
    }

    public Player getEnemy() {
        return enemy;
    }

    public Player getMe() {
        return me;
    }

    public Player[] getPlayers () {
        Player[] players = new Player[2];
        players[0] = getMe();
        players[1] = getEnemy();
        return players;
    }
}
