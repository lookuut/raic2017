import model.TerrainType;
import model.WeatherType;

/**
 * @TODO programming mirror map save logic
 */
public class PPField {
    protected float field[][];

    public PPField(int x, int y) {
        field = new float[y][x];
    }

    public void addTerrainMap (TerrainType[][] map) {
        try {
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[i].length; j++) {
                    field[i][j] += terrainToPP(map[i][j]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addWeatherMap(WeatherType[][] map) {
        try {
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[i].length; j++) {
                    field[i][j] += weatherToPP(map[i][j]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float terrainToPP (TerrainType type) throws Exception {
        switch (type) {
            case PLAIN:
                return (float)(3.0 / (MyStrategy.game.getPlainTerrainSpeedFactor() + MyStrategy.game.getPlainTerrainStealthFactor() + MyStrategy.game.getPlainTerrainVisionFactor()));
            case SWAMP:
                return (float)(3.0 / (MyStrategy.game.getSwampTerrainSpeedFactor() + MyStrategy.game.getSwampTerrainStealthFactor() + MyStrategy.game.getSwampTerrainVisionFactor()));
            case FOREST:
                return (float)(3.0 / (MyStrategy.game.getForestTerrainSpeedFactor() + MyStrategy.game.getForestTerrainStealthFactor() + MyStrategy.game.getForestTerrainVisionFactor()));
        }
        throw new Exception("Boolchit");
    }


    public static float weatherToPP (WeatherType type) throws Exception{
        switch (type) {
            case CLEAR:
                return (float)(3.0 / (MyStrategy.game.getClearWeatherSpeedFactor() + MyStrategy.game.getClearWeatherStealthFactor() + MyStrategy.game.getClearWeatherVisionFactor()));
            case CLOUD:
                return (float)(3.0 / (MyStrategy.game.getCloudWeatherSpeedFactor() + MyStrategy.game.getCloudWeatherStealthFactor() + MyStrategy.game.getCloudWeatherVisionFactor()));
            case RAIN:
                return (float)(3.0 / (MyStrategy.game.getRainWeatherSpeedFactor() + MyStrategy.game.getRainWeatherStealthFactor() + MyStrategy.game.getRainWeatherVisionFactor()));
        }

        throw new Exception("Boolchit");
    }

}
