package nars.nal.stamp;

import nars.Memory;
import nars.nal.Sentence;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.term.Compound;

/**
 * applies Stamp information to a sentence. default IStamp implementation.
 */
public class Stamper<C extends Compound> implements IStamp<C> {

    /**
     * serial numbers. not to be modified after Stamp constructor has initialized it
     */
    public long[] evidentialBase = null;

    /**
     * duration (in cycles) which any contained intervals are measured by
     */
    private int duration;
    /**
     * creation time of the stamp
     */
    private long creationTime;

    /**
     * estimated occurrence time of the event*
     */
    private long occurrenceTime;

//    /**
//     * used when the occurrence time cannot be estimated, means "unknown"
//     */
    //public static final long UNKNOWN = Integer.MAX_VALUE;


    /**
     * optional first parent stamp
     */
    public Stamp a = null;

    /**
     * optional second parent stamp
     */
    public Stamp b = null;

    @Deprecated public Stamper(final Memory memory, final Tense tense) {
        this(memory, memory.time(), tense);
    }

    @Deprecated public Stamper(final Memory memory, long creationTime, final Tense tense) {
        this(memory, creationTime, Stamp.occurrence(creationTime, tense, memory.duration()));
    }

    public Stamper(Operation operation, Memory memory, Tense tense) {
        this(operation.getTask().sentence, memory, tense);
    }
    public Stamper(Sentence s, Memory memory, Tense tense) {
        this(s, s.getCreationTime(), Stamp.occurrence(s.getCreationTime(), tense, memory.duration()));
    }

    public Stamper(final Memory memory, long creationTime, final long occurenceTime) {
        this.duration = memory.duration();
        this.creationTime = creationTime;
        this.occurrenceTime = occurenceTime;
    }

    public Stamper(long[] evidentialBase, Stamp a, Stamp b, long creationTime, long occurrenceTime, int duration) {
        this.a = a;
        this.b = b;
        this.creationTime = creationTime;
        this.occurrenceTime = occurrenceTime;
        this.duration = duration;
        this.evidentialBase = evidentialBase;
    }

    public Stamper clone() {
        return new Stamper(evidentialBase, a, b, creationTime, occurrenceTime, duration);
    }

    public Stamper cloneEternal() {
        return new Stamper(evidentialBase, a, b, creationTime, Stamp.ETERNAL, duration);
    }

    public Stamper(long[] evidentialBase, long creationTime, long occurrenceTime, int duration) {
        this(evidentialBase, null, null, creationTime, occurrenceTime, duration);
    }

    public Stamper(Stamp a, long creationTime, long occurrenceTime) {
        this(a, null, creationTime, occurrenceTime);
    }

    public Stamper(Stamp a, Stamp b, long creationTime, long occurrenceTime) {
        this.a = a;
        this.b = b;
        this.creationTime = creationTime;
        this.occurrenceTime = occurrenceTime;
    }

    @Deprecated
    public Stamper<C> setOccurrenceTime(long occurrenceTime) {
        this.occurrenceTime = occurrenceTime;
        return this;
    }


    public Stamper<C> setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public Stamper<C> setEternal() {
        return setOccurrenceTime(Stamp.ETERNAL);
    }

    @Override
    public void stamp(Sentence<C> t) {

        Stamp p = null; //parent to inherit some properties from

        if ((a == null) && (b == null)) {
            //will be assigned a new serial
        } else if ((a != null) && (b != null)) {
            evidentialBase = Stamp.zip(a.getEvidentialSet(), b.getEvidentialSet());
            p = a;
        }
        else if (a == null) p = b;
        else if (b == null) p = a;


        if (p!=null) {
            this.duration = p.getDuration();
            this.evidentialBase = p.getEvidentialBase();
        }

        if (t!=null) {
            t.setDuration(duration);
            t.setTime(creationTime, getOccurrenceTime());
            t.setEvidentialBase(evidentialBase);
        }
    }


    public long[] getEvidentialBase() {
        if (evidentialBase == null) {
            stamp(null); //compute any missing values
        }
        return evidentialBase;
    }

    @Override
    public boolean isCyclic() {
        long[] eb = getEvidentialBase();
        if (Stamp.isCyclic(eb)) {

        }
        throw new RuntimeException(this + " unable to calculate evidentialBase");

    }

    public boolean isEternal() {
        return occurrenceTime == Stamp.ETERNAL;
    }


    public long getOccurrenceTime() {
        return occurrenceTime;
    }


}
