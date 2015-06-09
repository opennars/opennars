package nars.nal.stamp;

import nars.Memory;
import nars.budget.DirectBudget;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.term.Compound;

/**
 * applies Stamp information to a sentence. default IStamp implementation.
 */
public class Stamper<C extends Compound> extends DirectBudget implements Stamp, StampEvidence, AbstractStamper {

    private long[] evidentialBase = null;

    private int duration;
    private long creationTime;

    private long occurrenceTime;

//    /**
//     * used when the occurrence time cannot be estimated, means "unknown"
//     */
    //public static final long UNKNOWN = Integer.MAX_VALUE;


    private Stamp a = null;

    private Stamp b = null;

    private long[] evidentialSetCached;

    @Deprecated public Stamper(final Memory memory, final Tense tense) {
        this(memory, memory.time(), tense);
    }

    @Deprecated public Stamper(final Memory memory, long creationTime, final Tense tense) {
        this(memory, creationTime, Stamp.getOccurrenceTime(creationTime, tense, memory.duration()));
    }

    public Stamper(Operation operation, Memory memory, Tense tense) {
        this(operation.getTask().sentence, memory, tense);
    }
    public Stamper(Sentence s, Memory memory, Tense tense) {
        this(s, s.getCreationTime(), Stamp.getOccurrenceTime(s.getCreationTime(), tense, memory.duration()));
    }

    public Stamper(final Memory memory, long creationTime, final long occurenceTime) {
        this.setDuration(memory.duration());
        this.setCreationTime(creationTime);
        this.setOccurrenceTime(occurenceTime);
    }

    public Stamper(long[] evidentialBase, Stamp a, Stamp b, long creationTime, long occurrenceTime, int duration) {
        this.setA(a);
        this.setB(b);
        this.setCreationTime(creationTime);
        this.setOccurrenceTime(occurrenceTime);
        this.setDuration(duration);
        this.setEvidentialBase(evidentialBase);
    }

    public Stamper(Memory memory, long occurrence) {
        this(memory, memory.time(), occurrence);
    }

    public Stamper(Stamp s, long occ) {
        this(s, s.getOccurrenceTime(), occ);
    }
    public Stamper(Task task, long occ) {
        this(task.sentence, occ);
    }

    public Stamper() {
        super();
    }

    public Stamper clone() {
        return new Stamper(getEvidentialBase(), getA(), getB(), getCreationTime(), getOccurrenceTime(), getDuration());
    }

    public Stamper cloneEternal() {
        return new Stamper(getEvidentialBase(), getA(), getB(), getCreationTime(), Stamp.ETERNAL, getDuration());
    }

    public Stamper(long[] evidentialBase, long creationTime, long occurrenceTime, int duration) {
        this(evidentialBase, null, null, creationTime, occurrenceTime, duration);
    }

    public Stamper(Stamp a, long creationTime, long occurrenceTime) {
        this(a, null, creationTime, occurrenceTime);
    }

    public Stamper(Stamp a, Stamp b, long creationTime, long occurrenceTime) {
        this.setA(a);
        this.setB(b);
        this.setCreationTime(creationTime);
        this.setOccurrenceTime(occurrenceTime);
    }

    @Deprecated
    public Stamper<C> setOccurrenceTime(long occurrenceTime) {
        this.occurrenceTime = occurrenceTime;
        return this;
    }

    @Override
    public Stamp setDuration(int d) {
        this.duration = d;
        return this;
    }

    @Override
    public Stamp setEvidentialBase(long[] b) {
        this.evidentialBase = b;
        return this;
    }


    public Stamper<C> setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Stamper<C> setEternal() {
        return setOccurrenceTime(Stamp.ETERNAL);
    }



    @Override public void applyToStamp(final Stamp target) {

        target.setDuration(getDuration())
              .setTime(getCreationTime(), getOccurrenceTime())
              .setEvidence(getEvidentialBase(), getEvidentialSetCached());

    }

    /**
     * creation time of the stamp
     */
    @Override
    public long getCreationTime() {
        return 0;
    }

    /**
     * duration (in cycles) which any contained intervals are measured by
     */
    public int getDuration() {
        if (this.duration == 0) {
            if (getB() !=null)
                return (this.duration = getB().getDuration());
            else if (getA() !=null)
                return (this.duration = getA().getDuration());
            else
                return -1;
        }
        return duration;
    }

    /**
     * estimated occurrence time of the event*
     */
    @Override
    public long getOccurrenceTime() {
        return occurrenceTime;
    }

    @Override
    public Stamp cloneWithNewCreationTime(long newCreationTime) {
        throw new UnsupportedOperationException("Not impl");
    }

    @Override
    public Stamp cloneWithNewOccurrenceTime(long newOcurrenceTime) {
        throw new UnsupportedOperationException("Not impl");
    }

    @Override
    public long[] getEvidentialSet() {
        updateEvidence();
        return getEvidentialSetCached();
    }

    /**
     * serial numbers. not to be modified after Stamp constructor has initialized it
     */
    public long[] getEvidentialBase() {
        updateEvidence();
        return evidentialBase;
    }

    protected void updateEvidence() {
        if (evidentialBase == null) {
            Stamp p = null; //parent to inherit some properties from

            if ((getA() == null) && (getB() == null)) {
                //will be assigned a new serial
            } else if ((getA() != null) && (getB() != null)) {
                //evidentialBase = Stamp.zip(a.getEvidentialSet(), b.getEvidentialSet());
                setEvidentialBase(Stamp.zip(getA().getEvidentialBase(), getB().getEvidentialBase()));
                p = getA();
            }
            else if (getA() == null) p = getB();
            else if (getB() == null) p = getA();


            if (p!=null) {
                this.setEvidentialBase(p.getEvidentialBase());
                this.setEvidentialSetCached(p.getEvidentialSet());
            }
        }
    }


    public boolean isEternal() {
        return getOccurrenceTime() == Stamp.ETERNAL;
    }


    /**
     * optional first parent stamp
     */
    public Stamp getA() {
        return a;
    }

    public void setA(Stamp a) {
        this.a = a;
    }

    /**
     * optional second parent stamp
     */
    public Stamp getB() {
        return b;
    }

    public void setB(Stamp b) {
        this.b = b;
    }

    public long[] getEvidentialSetCached() {
        return evidentialSetCached;
    }

    public void setEvidentialSetCached(long[] evidentialSetCached) {
        this.evidentialSetCached = evidentialSetCached;
    }
}
