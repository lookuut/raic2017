import model.WeatherType;

public class WeatherPPField extends  PPField{
    public WeatherPPField(int x, int y) {
        super(x, y);
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

    public void addWeatherMap(WeatherType[][] map) throws Exception {
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                addFactor(j, i, weatherToPP(map[i][j]));
            }
        }
    }
}
