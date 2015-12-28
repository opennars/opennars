package nars.truth;

import nars.util.data.Util;

/**
 * Created by me on 7/4/15.
 */
public abstract class AbstractScalarTruth extends AbstractTruth<Float> implements Truth {


    /**
     * The frequency factor of the truth value
     */
    private float frequency;


    @Override
    public void setConfidence(float b) {
        float e = DefaultTruth.DEFAULT_TRUTH_EPSILON; //getEpsilon();
        confidence = Util.round(b, e);
    }


    @Override
    public final int hashCode() {
        return Truth.hash(this);
    }


    @Override
    public float getFrequency() {
        return frequency;
    }


    @Override
    public Truth setFrequency(float f) {
        float e = DefaultTruth.DEFAULT_TRUTH_EPSILON; //getEpsilon();
        frequency = Util.round(f, e);
        return this;
    }



    @Override
    public boolean equalsFrequency(Truth t) {
        return (Util.equal(getFrequency(), t.getFrequency(), DefaultTruth.DEFAULT_TRUTH_EPSILON));
    }
}
