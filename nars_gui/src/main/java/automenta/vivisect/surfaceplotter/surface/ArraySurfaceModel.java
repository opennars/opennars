package automenta.vivisect.surfaceplotter.surface;

public class ArraySurfaceModel extends AbstractSurfaceModel {

    SurfaceVertex[][] surfaceVertex;

    /**
     * Creates two surfaces using data from the array.
     *
     * @param xmin lower bound of x values
     * @param xmax upper bound of x values
     * @param ymin lower bound of y values
     * @param ymax upper bound of y values
     * @param size number of items in each dimensions (ie z1 = float[size][size]
     * )
     * @param z1 value matrix (null supported)
     * @param z2 secondary function value matrix (null supported)
     */
    public void setValues(float xmin, float xmax, float ymin, float ymax, int size, float[][] z1, float[][] z2) {
        if (!isDataAvailable())
            setDataAvailable(false); // clean space
        setXMin(xmin);
        setXMax(xmax);
        setYMin(ymin);
        setYMax(ymax);
        setCalcDivisions(size - 1);

        float stepx = (xMax - xMin) / calcDivisions;
        float stepy = (yMax - yMin) / calcDivisions;
        float xfactor = 20 / (xMax - xMin); // 20 aint magic: surface vertex requires a value in [-10 ; 10]
        float yfactor = 20 / (yMax - yMin);

        int total = (calcDivisions + 1) * (calcDivisions + 1); // compute total size

        if ((surfaceVertex == null) || (surfaceVertex[0].length < total)) {
            surfaceVertex = new SurfaceVertex[2][total];
        }

        for (int i = 0; i <= calcDivisions; i++) {
            for (int j = 0; j <= calcDivisions; j++) {
                int k = i * (calcDivisions + 1) + j;

                float xv = xMin + i * stepx;
                float yv = yMin + j * stepy;
                float v1 = z1 != null ? z1[i][j] : Float.NaN;
                if (Float.isInfinite(v1)) {
                    v1 = Float.NaN;
                }
                if (!Float.isNaN(v1)) {
                    if (Float.isNaN(z1Max) || (v1 > z1Max)) {
                        z1Max = v1;
                    } else if (Float.isNaN(z1Min) || (v1 < z1Min)) {
                        z1Min = v1;
                    }
                }

                if (surfaceVertex[0][k]==null)
                    surfaceVertex[0][k] = new SurfaceVertex();
                
                surfaceVertex[0][k].set((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v1);
                float v2 = z2 != null ? z2[i][j] : Float.NaN;
                if (Float.isInfinite(v2)) {
                    v2 = Float.NaN;
                }
                if (!Float.isNaN(v2)) {
                    if (Float.isNaN(z2Max) || (v2 > z2Max)) {
                        z2Max = v2;
                    } else if (Float.isNaN(z2Min) || (v2 < z2Min)) {
                        z2Min = v2;
                    }
                }

                if (surfaceVertex[1][k]==null)
                    surfaceVertex[1][k] = new SurfaceVertex();
                surfaceVertex[1][k].set((xv - xMin) * xfactor - 10, (yv - yMin) * yfactor - 10, v2);
            }
        }

        autoScale();
        setDataAvailable(true);

        fireStateChanged();

    }

    @Override
    public SurfaceVertex[][] getSurfaceVertex() {
        return surfaceVertex;
    }

}
