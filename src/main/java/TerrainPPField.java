import model.TerrainType;

public class TerrainPPField extends PPField {

    public TerrainPPField(int x, int y) {
        super(x, y);
    }


    public void addTerrainMap (TerrainType[][] map){
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                addFactor(j, i , terrainToPP(map[i][j]));
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


    public static float terrainToPP (TerrainType type){
        switch (type) {
            case PLAIN:
                return (float)(3.0 / (MyStrategy.game.getPlainTerrainSpeedFactor() + MyStrategy.game.getPlainTerrainStealthFactor() + MyStrategy.game.getPlainTerrainVisionFactor()));
            case SWAMP:
                return (float)(3.0 / (MyStrategy.game.getSwampTerrainSpeedFactor() + MyStrategy.game.getSwampTerrainStealthFactor() + MyStrategy.game.getSwampTerrainVisionFactor()));
            case FOREST:
                return (float)(3.0 / (MyStrategy.game.getForestTerrainSpeedFactor() + MyStrategy.game.getForestTerrainStealthFactor() + MyStrategy.game.getForestTerrainVisionFactor()));
        }
        return 0f;
    }
}
