package nars.nal.nal7;

import nars.Op;
import nars.term.Atomic;
import nars.term.Term;

import java.io.IOException;
import java.util.Map;

/**
 * Interval represented directly as a measure of cycles encoded as an integer in some # of bits
 *
 * A virtual term which does not survive past normalization,
 * its value being collected into Sequence or Parallel intermval
 * components
 *
 * Its appearance in terms other than Sequence and Parallel
 * is meaningless.
 *
 * TODO realtime subclass which includes a number value that maps to external wall time
 */
final public class CyclesInterval extends Atomic implements Interval {

    final static CyclesInterval zero = new CyclesInterval(0);

    final int cyc;

    @Override
    public int varIndep() {
        return 0;
    }

    @Override
    public int varDep() {
        return 0;
    }

    @Override
    public int varQuery() {
        return 0;
    }

    public static CyclesInterval make(int numCycles) {
        if (numCycles == 0) return zero;
        return new CyclesInterval(numCycles);
    }

    protected CyclesInterval(int numCycles) {
        super(null); //interval(numCycles, bytesPrecision));

        if (numCycles < 0)
            throw new RuntimeException("cycles must be >= 0");

        this.cyc = numCycles;
    }


//    public static CyclesInterval intervalLog(long mag) {
//        long time = Math.round( LogInterval.time(mag, 5 /* memory.duration()*/) );
//        return new CyclesInterval(time, 0);
//    }
//
//    public static byte[] interval(long numCycles, int bytesPrecision) {
//        /*switch (bytesPrecision) {
//            case 1:
//        }*/
//        return Longs.toByteArray(numCycles);
//    }

    @Override
    public final int duration() {
        return cyc;
    }


    @Override
    public final int structure() { return 0;     }


    @Override
    public final Op op() {
        return Op.INTERVAL;
    }

//    @Override
//    public final Term clone() {
//        return this; /*new CyclesInterval(cyc, duration); */
//    }

    @Override
    public final void append(Appendable output, boolean pretty) throws IOException {
        output.append('/').append(Long.toString(cyc));//.append('/');
    }


    /** preferably use toCharSequence if needing a CharSequence; it avoids a duplication */
    @Override
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
    public Term substituted(Map<Term, Term> subs) {
        return this;
    }

    @Override
    public String toString() {
        return toStringBuilder(false).toString();
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
    public boolean hasVarIndep() {
        return false;
    }

    @Override
    public boolean hasVarDep() {
        return false;
    }

    @Override
    public boolean hasVarQuery() {
        return false;
    }

    @Override
    public int complexity() {
        return 0;
    }

//    /** filter any zero CyclesIntervals from the list and return a new one */
//    public static Term[] removeZeros(final Term[] relterms) {
//        int zeros = 0;
//        for (Term x : relterms)
//            if (x == zero)
//                zeros++;
//        Term[] t = new Term[relterms.length - zeros];
//
//        int p = 0;
//        for (Term x : relterms)
//            if (x != zero)
//                t[p++] = x;
//
//        return t;
//    }
}


///*
// * Copyright (C) 2014 peiwang
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package nars.nal.nal7;
//
//import nars.Memory;
//import nars.term.Atom;
//
///**
// * This stores the magnitude of a time difference, which is the logarithm of the time difference
// * in base D=duration ( @see Param.java ).  The actual printed value is +1 more than the stored
// * magnitude, so for example, it will have name() "+1" if magnitude=0, and "+2" if magnitude=1.
// *
// * @author peiwang
// */
//@Deprecated public class LogInterval extends Atom implements AbstractInterval {
//
//
//    static final int INTERVAL_POOL_SIZE = 16;
//    static final LogInterval[] INTERVAL = new LogInterval[INTERVAL_POOL_SIZE];
//
////    public final int magnitude;
////
////    public static AbstractInterval interval(final String i) {
////        return interval(Integer.parseInt(i.substring(1)) - 1);
////    }
////
////    public static LogInterval interval(final long time, final Memory m) {
////        return interval(magnitude(time, m.duration));
////    }
////
////    public static LogInterval interval(final long time, final AtomicDuration duration) {
////        return interval(magnitude(time, duration));
////    }
////
////    public static LogInterval interval(int magnitude) {
////        if (magnitude >= INTERVAL_POOL_SIZE)
////            return new LogInterval(magnitude, true);
////        else if (magnitude < 0)
////            magnitude = 0;
////
////        LogInterval existing = INTERVAL[magnitude];
////        if (existing == null) {
////            existing = new LogInterval(magnitude, true);
////            INTERVAL[magnitude] = existing;
////        }
////        return existing;
////    }
//
////
////    // time is a positive integer
////    protected LogInterval(final long timeDiff, final AtomicDuration duration) {
////        this(magnitude(timeDiff, duration), true);
////    }
////
////
////    /** this constructor has an extra unused argument to differentiate it from the other one,
////     * for specifying magnitude directly.
////     */
////    protected LogInterval(final int magnitude, final boolean yesMagnitude) {
////        super(Symbols.INTERVAL_PREFIX_OLD + String.valueOf(1+magnitude));
////        this.magnitude = magnitude;
////    }
//
//    public static int magnitude(final long timeDiff, final AtomicDuration duration) {
//        int m = (int) Math.round(Math.log(timeDiff) / duration.getSubDurationLog());
//        if (m < 0) return 0;
//        return m;
//    }
//
//    public static double time(final double magnitude, final double subdurationLog) {
//        return Math.exp(magnitude * subdurationLog);
//    }
//
//    public static double time(final double magnitude, final AtomicDuration duration) {
//        if (magnitude <= 0)
//            return 1;
//        return time(magnitude, duration.getSubDurationLog());
//    }
//
//    public static long cycles(final int magnitude, final AtomicDuration duration) {
//        return Math.round(time(magnitude, duration));
//    }
//
//    /** Calculates the average of the -0.5, +0.5 interval surrounding the integer magnitude
//     *  EXPERIMENTAL
//     * */
//    public static long cyclesAdjusted(final int magnitude, final AtomicDuration duration) {
//        //TODO cache this result because it will be equal for all similar integer magnitudes
//        double magMin = magnitude - 0.5;
//        double magMax = magnitude + 0.5;
//        return Math.round((time(magMin, duration) + time(magMax, duration))/2.0);
//    }
//
//    @Override
//    public final long cycles(final Memory m) {
//        return cycles(m.duration);
//    }
//
////    public final long cycles(final AtomicDuration duration) {
////        //TODO use a lookup table for this
////        return cycles(magnitude, duration);
////    }
//
//
////    /** returns a sequence of intervals which approximate a time period with a maximum number of consecutive Interval terms */
////    public static List<AbstractInterval> intervalSequence(final long t, final int maxTerms, final Memory memory) {
////        if (maxTerms == 1)
////            return Lists.newArrayList(interval(t, memory));
////
////        Interval first = interval(t, memory);
////        long a = first.cycles(memory); //current approximation value
////        if (a == t) return Lists.newArrayList(first);
////        else if (a < t) {
////            //ok we will add to it. nothing to do here
////        }
////        else if ((a > t) && (first.magnitude > 0)) {
////            //use next lower magnitude
////            first = interval(first.magnitude - 1);
////            a = first.cycles(memory);
////        }
////
////        List c = new ArrayList(maxTerms);
////        c.add(first);
////
////        long remaining = t - a;
////        c.addAll( intervalSequence(remaining, maxTerms - 1, memory));
////
////        /*
////        Interval approx = Interval.intervalTime(t, memory);
////        System.out.println(t + " = " + c + "; ~= " +
////                        approx + " (t=" + t + ", seq=" + intervalSequenceTime(c, memory) + ", one=" + approx.getTime(memory) + ")");
////        */
////
////        return c;
////    }
////
////    /** sum the time period contained in the Intervals (if any) in a sequence of objects (usually list of Terms) */
////    public static long intervalSequence(final Iterable s, final Memory memory) {
////        long time = 0;
////        for (final Object t : s) {
////            if (t instanceof AbstractInterval) {
////                AbstractInterval i = (AbstractInterval)t;
////                time += i.cycles(memory);
////            }
////        }
////        return time;
////    }
//
//
//
//}