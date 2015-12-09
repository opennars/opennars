package nars.util.meter;

import com.google.common.collect.Lists;
import nars.NAR;
import nars.concept.Concept;
import nars.term.Term;
import nars.truth.DefaultTruth;
import nars.util.event.CycleReaction;

import java.util.List;

/**
 * Calculates the maximum confidence of a concept's beliefs for a specifc frequency
 * TODO support freq ranges
 */
public class MaxBeliefConfidence extends CycleReaction implements Signals {

    public final Term term;
    private final NAR nar;
    private final float freq;
    public long bestAt = -1;
    float conf = -1;
    private float best;

    public MaxBeliefConfidence(NAR nar, String term, float freq) {
        super(nar);
        this.nar = nar;
        this.term = nar.term(term);
        this.freq = freq;
    }

    @Override
    public void onCycle() {
        Concept c = nar.concept(term);
        if (c == null) conf = -1;
        else {
            float lastConf = conf;
            conf = c.getBeliefs().getConfidenceMax(
                    freq - DefaultTruth.DEFAULT_TRUTH_EPSILON / 2.0f,
                    freq + DefaultTruth.DEFAULT_TRUTH_EPSILON / 2.0f
            );
            if (lastConf < conf) {
                bestAt = nar.time();
                best = conf;
            }

            if (!Float.isNaN(conf))
                conf = -1;
        }
    }

    @Override
    public String toString() {
        String s = term + " best=" + best + " @ " + bestAt;
        if (best != conf)
            s += " current=" + conf;

        return s;
    }

    @Override
    public List<Signal> getSignals() {
        return Lists.newArrayList(new Signal(term + "_confMax"));
    }

    @Override
    public Object[] sample(Object key) {
        return new Object[]{conf};
    }
}
