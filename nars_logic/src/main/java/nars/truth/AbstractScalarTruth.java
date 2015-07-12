package nars.truth;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by me on 7/4/15.
 */
public abstract class AbstractScalarTruth extends AbstractTruth<Float> implements Truth {


    public AbstractScalarTruth() {
        super();
    }


    /**
     * The frequency factor of the truth value
     */
    private float frequency;


    public void setConfidence(float c) {
        //if ((c > 1.0f) || (c < 0f)) throw new RuntimeException("Invalid confidence: " + c);
        //final float maxConf = getConfidenceMax();

        /*final float e = getEpsilon();
        c = Math.round(c / e) * e;*/

        if (c > 1f) c = 1f;
        else if (c < 0) c = 0;

        this.confidence = c;

    }



    @Override
    public int hashCode() {
        return Truth.hash(this);
    }


//    public DefaultTruth clone() {
//        if (isAnalytic())
//            return new AnalyticTruth(this);
//        else
//            return new DefaultTruth(this);
//    }

    public float getFrequency() {
        return frequency;
    }

    public Truth setFrequency(float f) {

        /*final float e = getEpsilon();
        f = FastMath.round(f / e) * e;*/

        if (f > 1.0f) f = 1.0f;
        else if (f < 0f) f = 0f;
        //if ((f > 1.0f) || (f < 0f)) throw new RuntimeException("Invalid frequency: " + f); //f = 0f;

        this.frequency = f;

        return this;
    }

    @Override
    protected boolean equalsValue(Truth t) {
        return (Truth.isEqual(getFrequency(), t.getFrequency(), DefaultTruth.DEFAULT_TRUTH_EPSILON));
    }
}
