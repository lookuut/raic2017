import model.WeatherType;

public class WeatherPPField extends  PPField{
    public WeatherPPField(int x, int y) {
        super(x, y);
    }


    public static float weatherToPP (WeatherType type) {
        switch (type) {
            case CLEAR:
                return (float)(3.0 / (MyStrategy.game.getClearWeatherSpeedFactor() + MyStrategy.game.getClearWeatherStealthFactor() + MyStrategy.game.getClearWeatherVisionFactor()));
            case CLOUD:
                return (float)(3.0 / (MyStrategy.game.getCloudWeatherSpeedFactor() + MyStrategy.game.getCloudWeatherStealthFactor() + MyStrategy.game.getCloudWeatherVisionFactor()));
            case RAIN:
                return (float)(3.0 / (MyStrategy.game.getRainWeatherSpeedFactor() + MyStrategy.game.getRainWeatherStealthFactor() + MyStrategy.game.getRainWeatherVisionFactor()));
        }
        return 0f;
    }

    public void addWeatherMap(WeatherType[][] map) {
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                addFactor(j, i, weatherToPP(map[i][j]));
            }
        }

        addFactor(0, 0 , CustomParams.allyUnitPPFactor);
        addFactor(getWidth() - 1, getHeight() - 1 , CustomParams.allyUnitPPFactor);
        addFactor(0, getHeight() - 1 , CustomParams.allyUnitPPFactor);
        addFactor(getWidth() - 1, 0 , CustomParams.allyUnitPPFactor);

        addFactor(0, 1 , CustomParams.allyUnitPPFactor);
        addFactor(1, 0 , CustomParams.allyUnitPPFactor);

        addFactor(getWidth() - 2, getHeight() -1 , CustomParams.allyUnitPPFactor);
        addFactor(getWidth() - 1, getHeight() - 2 , CustomParams.allyUnitPPFactor);

        addFactor(1, getHeight() - 1 , CustomParams.allyUnitPPFactor);
        addFactor(0, getHeight() - 2 , CustomParams.allyUnitPPFactor);

        addFactor(getWidth() - 1, 1 , CustomParams.allyUnitPPFactor);
        addFactor(getWidth() - 2, 0 , CustomParams.allyUnitPPFactor);
    }
}

