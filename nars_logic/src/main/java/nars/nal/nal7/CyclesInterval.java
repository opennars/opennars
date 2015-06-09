package nars.nal.nal7;

import com.google.common.primitives.Longs;
import nars.Memory;
import nars.nal.NALOperator;
import nars.nal.term.ImmutableAtom;
import nars.nal.term.Term;

import java.io.IOException;
import java.io.Writer;

/**
 * Interval represented directly as a measure of cycles encoded as an integer in some # of bits
 *
 * TODO realtime subclass which includes a number value that maps to external wall time
 */
public class CyclesInterval extends ImmutableAtom implements AbstractInterval {

    final static int bytesPrecision = 4;

    long cyc;
    int duration;

    public CyclesInterval(long numCycles, int duration) {
        super(interval(numCycles, bytesPrecision));
        this.cyc = numCycles;
        this.duration = duration;
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



    @Override
    public NALOperator operator() {
        return NALOperator.INTERVAL;
    }

    @Override
    public Term clone() {
        return new CyclesInterval(cyc, duration);
    }

    @Override
    public void append(Writer output, boolean pretty) throws IOException {
        output.append('/').append(Long.toString(cyc)).append('/');
    }
}
