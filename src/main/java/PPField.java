import model.TerrainType;
import model.WeatherType;
import java.util.*;

/**
 * @TODO programming mirror map save logic
 */
public class PPField {
    protected float field[][];

    protected int width;
    protected int height;

    public PPField(int width, int height) {
        field = new float[height][width];
        this.width = width;
        this.height = height;
    }

    public void addTerrainMap (TerrainType[][] map) {
        try {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    field[i][j] += terrainToPP(map[i][j]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addWeatherMap(WeatherType[][] map) {
        try {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
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


    public void addFactor (float factor, int x, int y) {
        this.field[y][x] += factor;
    }

    public float getFactor (int x, int y) {
        return field[y][x];
    }

    public void sumFields(List<PPField> fields) {
        for (PPField localField : fields) {
            this.sumField(localField);
        }
    }

    public void sumField(PPField localField) {

        int xFactor = (localField.getWidth() / getWidth());
        int yFactor = (localField.getHeight() / getHeight());
        for (int y = 0; y < this.height; y++) {
            int transformedY = yFactor * y;
            for (int x = 0; x < this.width; x++) {
                int transformedX = xFactor * x;
                addFactor(localField.getFactor(transformedX, transformedY) , x, y);
            }
        }
    }

    public void minusField(PPField localField) {

        int xFactor = (localField.getWidth() / getWidth());
        int yFactor = (localField.getHeight() / getHeight());
        for (int y = 0; y < this.height; y++) {
            int transformedY = yFactor * y;
            for (int x = 0; x < this.width; x++) {
                int transformedX = xFactor * x;
                addFactor(-localField.getFactor(transformedX, transformedY) , x, y);
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTransformedXCoordinat(double x) {
        return (int)(Math.floor(x * getWidth()) / MyStrategy.world.getWidth());
    }

    public int getTransformedYCoordinat(double y) {
        return (int)(Math.floor(y * getHeight()) / MyStrategy.world.getHeight());
    }

    public float getPathAvgFactor(LineSegment leftLineSegment, LineSegment rightLineSegment) {
        HashSet<Integer> visitedCells = new HashSet<>();
        HashSet<Integer> intersectCells = new HashSet<>();

        float factor = recursive(visitedCells, intersectCells, leftLineSegment.getStartX(), leftLineSegment.getStartY(), leftLineSegment);
        factor += recursive(visitedCells, intersectCells, rightLineSegment.getStartX(), rightLineSegment.getStartY(), leftLineSegment);

        return (factor / intersectCells.size());
    }

    public float recursive(HashSet<Integer> visitedCells, HashSet<Integer> intersectCells, int x, int y, LineSegment line) {
        float factor = 0;
        for (int j = -1; j <= 1 && j + y >= 0 && j + y < getHeight(); j++) {
            for (int i = -1; i <= 1 && x + i >= 0 && x + i < getWidth(); i++) {
                int number = x + i + (y + j) * width;

                if (!visitedCells.contains(number)) {
                    visitedCells.add(number);

                    if (line.isIntersectSquare(x + i, y + j, 1)) {
                        intersectCells.add(number);
                        factor += recursive(visitedCells, intersectCells, x + i, y + j, line) + getFactor(x + i, y + j);
                    }
                }
            }
        }
        return factor;
    }

    public void print() {
        System.out.println(Arrays.deepToString(field).replaceAll("],", "]," + System.getProperty("line.separator")));
    }

    public double[] getMaxValueCell() {
        int maxX = 0;
        int maxY = 0;
        float max = field[0][0];
        for (int j = 0; j < getHeight(); j++) {
            for (int i = 0; i < getWidth(); i++) {
                if (field[j][i] > max) {
                    max = field[j][i];
                    maxX = i;
                    maxY = j;
                }
            }
        }

        double[] result = {getWorldX(maxX), getWorldY(maxY)};
        return result;
    }

    public double getWorldX(int x) {
        return (Math.floor(x * MyStrategy.world.getWidth()) / (double)getWidth());
    }

    public double getWorldY(int y) {
        return (Math.floor(y * MyStrategy.world.getHeight()) / (double)getHeight());
    }


    public int[] getNearestSafetyPoint(double targetX, double targetY, LineSegment lineSegment) throws Exception {
        HashSet<Integer> visitedCells = new HashSet<>();
        int[] result = deepSearch(visitedCells, getTransformedXCoordinat(targetX), getTransformedYCoordinat(targetY), lineSegment);

        if (result == null) {
            throw new Exception("getNearestSafetyPoint  " + targetX + " " + targetY);
        }

        int[] coors = {result[0], result[1]};
        return coors;
    }

    public int[] deepSearch (HashSet<Integer> visitedCells, int x, int y, LineSegment line) {
        for (int j = -1; j <= 1 && j + y >= 0 && j + y < getHeight(); j++) {
            for (int i = -1; i <= 1 && x + i >= 0 && x + i < getWidth(); i++) {
                int number = x + i + (y + j) * getWidth();

                if (!visitedCells.contains(number)) {
                    visitedCells.add(number);

                    if (line.isIntersectSquare(x + i, y + j, 1)) {
                        if (getFactor(x + i, y + j) != 0) {
                            int [] result = {x + i, y + j};
                            return result;
                        } else {
                            return deepSearch(visitedCells, x + i, y + j, line);
                        }
                    }
                }
            }
        }

        return null;
    }
}
