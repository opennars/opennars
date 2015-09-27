package nars.nal.nal7;

import com.google.common.primitives.Longs;
import nars.Memory;
import nars.Op;
import nars.term.Atom;
import nars.term.Term;

import java.io.IOException;

/**
 * Interval represented directly as a measure of cycles encoded as an integer in some # of bits
 *
 * TODO realtime subclass which includes a number value that maps to external wall time
 */
public class CyclesInterval extends Atom implements AbstractInterval {

    final static int bytesPrecision = 4;

    final static CyclesInterval zero = new CyclesInterval(0, 0);

    final long cyc;
    final int duration;

    @Override final public boolean hasVar(Op type) {
        return false;
    }

    @Override
    public void rehash() {
        //nothing
    }

    @Override
    public final int hashCode() {
        throw new RuntimeException("N/A");
    }
    @Override
    public final int complexity() {
        throw new RuntimeException("N/A");
    }
    @Override
    public final int volume() {
        throw new RuntimeException("N/A");
    }

    public static CyclesInterval make(long numCycles) {
        return new CyclesInterval(numCycles);
    }

    public static CyclesInterval make(long numCycles, Memory m) {
        return make(numCycles, m.duration());
    }

    public static CyclesInterval make(long numCycles, int duration) {
        if (numCycles == 0) return zero;
        return new CyclesInterval(numCycles, duration);
    }

    protected CyclesInterval(long numCycles) {
        this(numCycles, 0);
    }

    protected CyclesInterval(long numCycles, int duration) {
        super((byte[]) null); //interval(numCycles, bytesPrecision));

        this.cyc = numCycles;
        this.duration = duration;
    }


    public static CyclesInterval intervalLog(long mag) {
        long time = Math.round( Interval.time(mag, 5 /* memory.duration()*/) );
        return new CyclesInterval(time, 0);
    }

    public static byte[] interval(long numCycles, int bytesPrecision) {
        /*switch (bytesPrecision) {
            case 1:
        }*/
        return Longs.toByteArray(numCycles);
    }

    @Override
    public final long cycles(Memory m) {
        return cyc;
    }


    public final int structure() { return 0;     }


    @Override
    public final Op op() {
        return Op.INTERVAL;
    }

    @Override
    public final Term clone() {
        return this; /*new CyclesInterval(cyc, duration); */
    }

    @Override
    public final boolean hasVar() {
        return false;
    }

    @Override
    public final int vars() {
        return 0;
    }

    @Override
    public final void append(Appendable output, boolean pretty) throws IOException {
        output.append('/').append(Long.toString(cyc)).append('/');
    }


    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    public StringBuilder toStringBuilder(final boolean pretty) {
        StringBuilder sb = new StringBuilder();
        try {
            append(sb, pretty);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb;
    }

    @Override
    public String toString() {
        return toStringBuilder(false).toString();
    }

    /** filter any zero CyclesIntervals from the list and return a new one */
    public static Term[] removeZeros(final Term[] relterms) {
        int zeros = 0;
        for (Term x : relterms)
            if (x == zero)
                zeros++;
        Term[] t = new Term[relterms.length - zeros];

        int p = 0;
        for (Term x : relterms)
            if (x != zero)
                t[p++] = x;

        return t;
    }
}
