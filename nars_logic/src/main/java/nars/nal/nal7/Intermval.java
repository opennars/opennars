package nars.nal.nal7;

/**
 * Stores a set of intervals that will be associated with a
 * conjunction sequence or other temporal term with which
 * this can describe the delays between and around zero
 * or more subterms in the sequence
 */
public interface Intermval {

    /** a conj sequence will return a long[NUM_TERMS+1]
     * index i means the interval preceding term i
     * the final index is the interval following the final term
     */
    public long[] intervals();


    /** total amount of time contained from begining to end */
    default public long intervalLength() {
        long l = 0;
        for (final long x : intervals())
            l += x;
        return l;
    }

    /** l1 distance: sum absolute differnce of items normalized to total length
     *  distance = 0: equal
     * */
    default public long distance1(final Intermval other) {
        return distance1(other, Long.MAX_VALUE);
    }

    /** TODO - return distance1 but as soon as distance exceeds 'onlyIfLessThan'
     *  threshold. otherwise returns Long.MAX_VALUE */
    default public long distance1(final Intermval other, final long onlyIfLessthan) {
        final long[] a = intervals();
        final long[] b = other.intervals();
        long dist = 0;

        if (a.length!=b.length)
            throw new RuntimeException("differnt length arrays comparison not impl yet");
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
