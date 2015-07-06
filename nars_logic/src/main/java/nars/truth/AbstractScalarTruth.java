package nars.truth;

import org.apache.commons.math3.util.FastMath;

/**
 * Created by me on 7/4/15.
 */
public abstract class AbstractScalarTruth extends AbstractTruth<Float> implements Truth {

    public AbstractScalarTruth(final float freq, final float conf) {
        super(conf);
        setFrequency(freq);
    }

    public AbstractScalarTruth() {
        super();
    }


    /**
     * The frequency factor of the truth value
     */
    private float frequency;



    @Override
    public int hashCode() {
        return Truth.hash(this);
    }


    public BasicTruth clone() {
        if (isAnalytic())
            return new AnalyticTruth(this);
        else
            return new BasicTruth(this);
    }

    public float getFrequency() {
        return frequency;
    }

    public Truth setFrequency(float f) {

        final float e = getEpsilon();
        f = FastMath.round(f / e) * e;

        if (f > 1.0f) f = 1.0f;
        if (f < 0f) f = 0f;
        //if ((f > 1.0f) || (f < 0f)) throw new RuntimeException("Invalid frequency: " + f); //f = 0f;

        this.frequency = f;

        return this;
    }

    @Override
    protected boolean equalsValue(Truth t) {
        return (Truth.isEqual(getFrequency(), t.getFrequency(), getEpsilon()));
    }
}
