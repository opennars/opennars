package automenta.falcon;

/**
 * Created by me on 8/4/15.
 */
class LAYER {   /* A LAYER OF A NET:                     */
    public static final int BIAS = 1;

    int Units;                 /* - number of units in this layer       */
    double[] Output;          /* - output of ith unit                  */
    double[] Error;           /* - error term of ith unit              */
    double[][] Weight;        /* - connection weights to ith unit      */
    double[][] WeightSave;    /* - saved weights for stopped training  */
    double[][] dWeight;       /* - last weight deltas for momentum     */

    public LAYER(int units, int prev_units) {
        this.Units = units;
        this.Output = new double[this.Units + 1];
        this.Error = new double[this.Units + 1];
        this.Weight = new double[this.Units + 1][];
        this.WeightSave = new double[this.Units + 1][];
        this.dWeight = new double[this.Units + 1][];
        this.Output[0] = BIAS;  /* BIAS = 1, like w0 */

        if (prev_units != 0) {
            for (int i = 1; i <= this.Units; i++) {
                this.Weight[i] = new double[prev_units + 1];      /* Weight[0] for what */
                this.WeightSave[i] = new double[prev_units + 1];  /* WeightSave[0] for what */
                this.dWeight[i] = new double[prev_units + 1];     /* so dweight is a Unit[l-1]*Unit[l] array */
            }
        }
    }
}
