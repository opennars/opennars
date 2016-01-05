package nars.nal.nal7;

import nars.term.Term;
import nars.term.TermMetadata;
import nars.term.compound.Compound;

/**
 * Stores a sequence of "inner intervals" that quantify the
 * timing of Sequence subterms
 */
interface Intermval extends Interval, TermMetadata {



    /** a conj sequence will return a long[NUM_TERMS+1]
     * index i means the interval preceding term i
     * the final index is the interval following the final term
     */
    int[] intervals();



    /** l1 distance: sum absolute differnce of items normalized to total length
     *  distance = 0: equal
     * */
    default long distance1(Intermval other) {
        return distance1(other, Long.MAX_VALUE);
    }

    /** return distance1 but as soon as distance exceeds 'onlyIfLessThan'
     *  threshold. otherwise returns Long.MAX_VALUE */
    default long distance1(Intermval other, long onlyIfLessthan) {
        int[] a = intervals();
        int[] b = other.intervals();

        int alength = a.length;
        if (alength !=b.length)
            throw new RuntimeException("differnt length arrays comparison not impl yet");
        long dist = 0;
        for (int i = 0; i < alength; i++) {
            float d = a[i] - b[i];
            if (d < 0) d = -d;
            dist += d;
            if (dist > onlyIfLessthan)
                return Long.MAX_VALUE;
        }

        return dist;
    }


    //TODO: interpolating distance between two terms that start and end at different times. the area they share in common is evaluated for
    // alignment, optionally: absolute, scaled, or translated in time.

    @FunctionalInterface
    interface IntermvalVisitor {
        /** return true to continue, false to cancel, prev/next null at the ends, dur is the visited intermval's duration in cycles */
        boolean onInterval(Compound superterm, Term prev, int dur, Term next);
    }

    /** UNTESTED */
    static void visit(Compound c, IntermvalVisitor v, boolean includeZero, boolean recurse) {
        if (!c.op().isA(Intermval.metadataBits)) return;
        int[] x = ((Intermval)c).intervals();
        Term prev = null;
        for (int i = 0; i < x.length; i++) {
            if (!includeZero && x[i]==0) continue;
            Term next = (i == x.length-1) ? null : c.term(i+1);
            v.onInterval(c, prev, x[i], next);
            if (recurse && next instanceof Compound) {
                visit((Compound)next, v, includeZero, recurse);
            }
            prev = next;
        }
    }

}
