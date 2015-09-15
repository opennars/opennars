package nars.truth;

/**
 * Truth value used to store values when in the 'analytic' state
 */
public class AnalyticTruth extends DefaultTruth {

//
//    /** returns null if the confidence is zero */
//    public static AnalyticTruth get(final float f, final float c, final Truth t) {
//        return AnalyticTruth.get(f, c, t, (Memory)null);
//    }
//
//    /** returns null if the confidence is zero or below the memory's confidence threshold */
//    public static AnalyticTruth get(float freq, float conf, Truth copyEpsilonFrom, Memory m) {
//        return new AnalyticTruth(freq, conf, copyEpsilonFrom);
//    }
//
//    protected AnalyticTruth(float freq, float conf, Truth copyEpsilonFrom) {
//        this(freq, conf, copyEpsilonFrom.getEpsilon());
//    }
//
//    protected AnalyticTruth(float freq, float conf, float epsilon) {
//        super(freq, conf, epsilon);
//    }

    protected AnalyticTruth(Truth cloneFrom) {
        this(cloneFrom.getFrequency(), cloneFrom.getConfidence());
        //this(cloneFrom.getFrequency(), cloneFrom.getConfidence(), cloneFrom.getEpsilon());
    }

    public AnalyticTruth(float f, float c) {
        super();
        set(f, c);
    }


    @Override
    public boolean isAnalytic() {
        return true;
    }



}
