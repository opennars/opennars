package nars.meta;

import nars.truth.Truth;

/**
 * Created by me on 9/30/15.
 */
public interface TruthOrDesireFunction {

    /**
     * @param T taskTruth
     * @param B beliefTruth (possibly null)
     * @return
     */
    abstract public Truth get(Truth T, Truth B);
}
