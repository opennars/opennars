package nars.truth;

/** subclass used as an indicator that it was the result of projection */
@Deprecated final class ProjectedTruth extends DefaultTruth {

    //private final long target;

    public ProjectedTruth(float f, float c) {
        super(f, c);
        //this.target = target;
    }

//    public ProjectedTruth(Truth cloned, long occurrenceTime) {
//        this(cloned.getFrequency(), cloned.getConfidence(), occurrenceTime);
//    }

    //public long getTargetTime() { return target; }
}
