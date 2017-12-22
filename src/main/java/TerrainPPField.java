import model.TerrainType;

public class TerrainPPField extends PPField {

    public TerrainPPField(int x, int y) {
        super(x, y);
    }


    public void addTerrainMap (TerrainType[][] map){
        int propose = (getWidth() / map.length);

        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                if (i == 0 || j == 0 || i == getHeight() - 1 || j == getWidth() - 1) {
                    addFactor(j, i , CustomParams.allyUnitPPFactor);
                }
                int localY = i / propose;
                int localX = j / propose;
                addFactor(j, i , terrainToPP(map[localY][localX]));
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
