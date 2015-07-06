package nars.truth;

/** truth with additional occurence time used for projection & eternalization results */
public final class ProjectedTruth extends BasicTruth {

    private final long target;

    public ProjectedTruth(final float f, final float c, float epsilon, long target) {
        super(f, c, epsilon);
        this.target = target;
    }

    public ProjectedTruth(Truth cloned, long occurrenceTime) {
        this(cloned.getFrequency(), cloned.getConfidence(), cloned.getEpsilon(), occurrenceTime);
    }

    public long getTargetTime() { return target; }
}
