package nars.nal.meta;

import nars.truth.Truth;

/**
 * Created by me on 9/30/15.
 */
public interface TruthFunction {

    /**
     * @param T taskTruth
     * @param B beliefTruth (possibly null)
     * @return
     */
    Truth get(Truth T, Truth B);
}
