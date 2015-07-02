package nars.nal.truth;

/**
 * Truth value used to store values when in the 'analytic' state
 */
public class AnalyticTruth extends BasicTruth {


    public AnalyticTruth(float freq, float conf, float epsilon) {
        super(freq, conf, epsilon);
    }

    AnalyticTruth(Truth cloneFrom) {
        this(cloneFrom.getFrequency(), cloneFrom.getConfidence(), cloneFrom.getEpsilon());
    }

    @Override
    public boolean isAnalytic() {
        return true;
    }

}
