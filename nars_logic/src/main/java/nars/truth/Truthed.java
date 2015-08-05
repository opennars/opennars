package nars.truth;

/** indicates an implementation has, or is associated with a specific TruthValue */
public interface Truthed {
    public Truth getTruth();

    default float getExpectation() {
        return getTruth().getExpectation();
    }
    default float getConfidence() {
        return getTruth().getConfidence();
    }
    default float getFrequency() {
        return getTruth().getFrequency();
    }
}
