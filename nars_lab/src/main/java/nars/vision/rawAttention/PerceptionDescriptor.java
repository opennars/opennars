package nars.vision.rawAttention;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PerceptionDescriptor {
    public List<int[]> sensorOffsets = new ArrayList<>();

    public int[] visionCenterPosition;

    public boolean[][] pixelMap;

    // updated by recalculatePerceptionMap()
    public boolean[][] cachedPerceptionMap;

    public void createPixelMap(int width, int height) {
        pixelMap = new boolean[width][height];
    }

    // just for visualisation
    public void recalculatePerceptionMap() {
        cachedPerceptionMap = calculatePerceptionMap();
    }

    private boolean[][] calculatePerceptionMap() {
        int width = pixelMap.length;
        int height = pixelMap[0].length;

        boolean[][] perceptionMap = new boolean[width][height];

        for( final int[] currentSensorOffset : sensorOffsets ) {
            final int x = currentSensorOffset[0] + visionCenterPosition[0];
            final int y = currentSensorOffset[1] + visionCenterPosition[1];

            if( x >= 0 && x < width && y >= 0 && y < height ) {
                perceptionMap[x][y] = true;
            }
        }

        return perceptionMap;
    }

    public List<Boolean> sampleSensors() {
        List<Boolean> resultList = new ArrayList<>();

        for( final int[] currentSensorOffset : sensorOffsets ) {
            final int x = currentSensorOffset[0] + visionCenterPosition[0];
            final int y = currentSensorOffset[1] + visionCenterPosition[1];

            resultList.add(pixelMap[x][y]);
        }

        return resultList;
    }

    public void populateSensorOffsetsFor(int centerWidthDiv2, int centerHeightDiv2, int radiusLogarithmDistribution) {
        sensorOffsets.clear();

        // center box
        for( int y = -centerHeightDiv2; y < centerHeightDiv2; y++ ) {
            for( int x = -centerWidthDiv2; x < centerWidthDiv2; x++ ) {
                sensorOffsets.add(new int[]{x, y});
            }
        }

        // TODO< logarithm distributed periphial vision >
    }
}
