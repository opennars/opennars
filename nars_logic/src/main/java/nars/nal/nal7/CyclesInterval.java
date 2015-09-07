package nars.nal.nal7;

import com.google.common.primitives.Longs;
import nars.AbstractMemory;
import nars.Memory;
import nars.Op;
import nars.term.ImmutableAtom;
import nars.term.Term;

import java.io.IOException;
import java.io.Writer;

/**
 * Interval represented directly as a measure of cycles encoded as an integer in some # of bits
 *
 * TODO realtime subclass which includes a number value that maps to external wall time
 */
public class CyclesInterval extends ImmutableAtom implements AbstractInterval {

    final static int bytesPrecision = 4;

    final static CyclesInterval zero = new CyclesInterval(0, 0);

    long cyc;
    int duration;

    @Override final public boolean hasVar(Op type) {
        return false;
    }

    public static CyclesInterval make(long numCycles) {
        return new CyclesInterval(numCycles);
    }

    public static CyclesInterval make(long numCycles, AbstractMemory m) {
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
        super(interval(numCycles, bytesPrecision));

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
    public long cycles(Memory m) {
        return cyc;
    }



    public int structure() { return 0;     }


    @Override
    public Op op() {
        return Op.INTERVAL;
    }

    @Override
    public Term clone() {
        return new CyclesInterval(cyc, duration);
    }

    @Override
    public boolean hasVar() {
        return false;
    }

    @Override
    public int vars() {
        return 0;
    }

    @Override
    public void append(Writer output, boolean pretty) throws IOException {
        output.append('/').append(Long.toString(cyc)).append('/');
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
