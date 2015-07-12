package nars.truth;

/** truth with additional occurence time used for projection & eternalization results */
public final class ProjectedTruth extends DefaultTruth {

    private final long target;

    public ProjectedTruth(final float f, final float c, long target) {
        super(f, c);
        this.target = target;
    }

    public ProjectedTruth(Truth cloned, long occurrenceTime) {
        this(cloned.getFrequency(), cloned.getConfidence(), occurrenceTime);
    }

    public long getTargetTime() { return target; }
}
