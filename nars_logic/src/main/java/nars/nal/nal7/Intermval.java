package nars.nal.nal7;

/**
 * Stores a sequence of "inner intervals" that quantify the
 * timing of Sequence subterms
 */
public interface Intermval extends Interval {

    /** a conj sequence will return a long[NUM_TERMS+1]
     * index i means the interval preceding term i
     * the final index is the interval following the final term
     */
    int[] intervals();



    /** l1 distance: sum absolute differnce of items normalized to total length
     *  distance = 0: equal
     * */
    default long distance1(final Intermval other) {
        return distance1(other, Long.MAX_VALUE);
    }

    /** return distance1 but as soon as distance exceeds 'onlyIfLessThan'
     *  threshold. otherwise returns Long.MAX_VALUE */
    default long distance1(final Intermval other, final long onlyIfLessthan) {
        final int[] a = intervals();
        final int[] b = other.intervals();

        if (a.length!=b.length)
            throw new RuntimeException("differnt length arrays comparison not impl yet");
        long dist = 0;
        for (int i = 0; i < a.length; i++) {
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

}
