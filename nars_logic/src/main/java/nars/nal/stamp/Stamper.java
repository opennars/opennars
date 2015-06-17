//package nars.nal.stamp;
//
//import nars.Memory;
//import nars.budget.DirectBudget;
//import nars.io.JSONOutput;
//import nars.nal.Sentence;
//import nars.nal.Task;
//import nars.nal.nal7.Tense;
//import nars.nal.nal8.Operation;
//import nars.nal.term.Compound;
//
///**
// * applies Stamp information to a sentence. default IStamp implementation.
// */
//public class Stamper<C extends Compound> extends DirectBudget implements Stamp, StampEvidence, AbstractStamper {
//
//
//
//
////    /**
////     * used when the occurrence time cannot be estimated, means "unknown"
////     */
//    //public static final long UNKNOWN = Integer.MAX_VALUE;
//
//
//    protected Stamp a = null;
//
//    protected Stamp b = null;
//
//    @Deprecated public Stamper(final Memory memory, final Tense tense) {
//        this(memory, memory.time(), tense);
//    }
//
//    @Deprecated public Stamper(final Memory memory, long creationTime, final Tense tense) {
//        this(memory, creationTime, Stamp.getOccurrenceTime(creationTime, tense, memory.duration()));
//    }
//
//    public Stamper(Operation operation, Memory memory, Tense tense) {
//        this(operation.getTask().sentence, memory, tense);
//    }
//    public Stamper(Sentence s, Memory memory, Tense tense) {
//        this(s, s.getCreationTime(), Stamp.getOccurrenceTime(s.getCreationTime(), tense, memory.duration()));
//    }
//
//    public Stamper(final Memory memory, long creationTime, final long occurenceTime) {
//        this.setDuration(memory.duration());
//        this.setCreationTime(creationTime);
//        this.setOccurrenceTime(occurenceTime);
//    }
//
//    public Stamper(long[] evidentialBase, Stamp a, Stamp b, long creationTime, long occurrenceTime, int duration) {
//        this.setA(a);
//        this.setB(b);
//        this.setCreationTime(creationTime);
//        this.setOccurrenceTime(occurrenceTime);
//        this.setDuration(duration);
//        this.setEvidentialSet(evidentialSet);
//    }
//
//    public Stamper(Memory memory, long occurrence) {
//        this(memory, memory.time(), occurrence);
//    }
//
//    public Stamper(Stamp s, long occ) {
//        this(s, s.getOccurrenceTime(), occ);
//    }
//    public Stamper(Task task, long occ) {
//        this(task.sentence, occ);
//    }
//
//    public Stamper() {
//        super();
//    }
//
//    public Stamper clone() {
//        return new Stamper(getEvidentialSet(), getA(), getB(), getCreationTime(), getOccurrenceTime(), getDuration());
//    }
//
//    public Stamper cloneEternal() {
//        return new Stamper(getEvidentialSet(), getA(), getB(), getCreationTime(), Stamp.ETERNAL, getDuration());
//    }
//
//    public Stamper(long[] evidentialBase, long creationTime, long occurrenceTime, int duration) {
//        this(evidentialBase, null, null, creationTime, occurrenceTime, duration);
//    }
//
//    public Stamper(Stamp a, long creationTime, long occurrenceTime) {
//        this(a, null, creationTime, occurrenceTime);
//    }
//
//    public Stamper(Stamp a, Stamp b, long creationTime, long occurrenceTime) {
//        this.setA(a);
//        this.setB(b);
//        this.setCreationTime(creationTime);
//        this.setOccurrenceTime(occurrenceTime);
//    }
//
//
//    public Stamper<C> setEternal() {
//        return setOccurrenceTime(Stamp.ETERNAL);
//    }
//
//
//
//
//    @Override
//    public Stamp cloneWithNewCreationTime(long newCreationTime) {
//        throw new UnsupportedOperationException("Not impl");
//    }
//
//    @Override
//    public Stamp cloneWithNewOccurrenceTime(long newOcurrenceTime) {
//        throw new UnsupportedOperationException("Not impl");
//    }
//
//
//
//
//    /**
//     * optional first parent stamp
//     */
//    public Stamp getA() {
//        return a;
//    }
//
//    public Stamp setA(Stamp a) {
//        if (a == this)
//            throw new RuntimeException("Circular parent stamp");
//        this.a = a;
//        return this;
//    }
//
//    /**
//     * optional second parent stamp
//     */
//    public Stamp getB() {
//        return b;
//    }
//
//    public Stamp setB(Stamp b) {
//        if (b == this)
//            throw new RuntimeException("Circular parent stamp");
//        this.b = b; return this;
//    }
//
//
//
//    public boolean isDouble() {
//        return this.a!=null & this.b!=null;
//    }
//}
