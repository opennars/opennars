//package nars.util.math;
//
//import java.util.Iterator;
//import java.util.NoSuchElementException;
//
///**
// * Lexicographic combinations iterator with recyclable data and
// * shuffling options
// *
// NOT TESTED
// * modified from: https://raw.githubusercontent.com/apache/commons-math/master/src/main/java/org/apache/commons/math4/util/Combinations.java
// *
// *
// * <p>
// * Implementation follows Algorithm T in <i>The Art of Computer Programming</i>
// * Internet Draft (PRE-FASCICLE 3A), "A Draft of Section 7.2.1.3 Generating All
// * Combinations</a>, D. Knuth, 2004.</p>
// * <p>
// * The degenerate cases {@code k == 0} and {@code k == n} are NOT handled by this
// * implementation.  If constructor arguments satisfy {@code k == 0}
// * or {@code k >= n}, no exception is generated, but the iterator is empty.
// * </p>
// */
//public class LexicographicIterator  {
//    /**
//     * Size of subsets returned by the iterator
//     */
//    private final int k;
//
//    /**
//     * c[1], ..., c[k] stores the next combination; c[k + 1], c[k + 2] are
//     * sentinels.
//     * <p>
//     * Note that c[0] is "wasted" but this makes it a little easier to
//     * follow the code.
//     * </p>
//     */
//    final int[] c;
//
//    /**
//     * Return value for {@link #hasNext()}
//     */
//    private boolean more = true;
//
//    /**
//     * Marker: smallest index such that c[j + 1] > j
//     */
//    private int j;
//
//    /**
//     * Construct a CombinationIterator to enumerate k-sets from n.
//     * <p>
//     * NOTE: If {@code k === 0} or {@code k >= n}, the Iterator will be empty
//     * (that is, {@link #hasNext()} will return {@code false} immediately.
//     * </p>
//     *
//     * @param n size of the set from which subsets are enumerated
//     * @param k size of the subsets to enumerate
//     */
//    public LexicographicIterator(int n, int k, boolean shuffle) {
//        this.k = k;
//        c = new int[k + 3];
//        if (k == 0) {
//            more = false;
//            return;
//        }
//
//        if (!shuffle) {
//            // Initialize c to start with lexicographically first k-set
//            for (int i = 1; i <= k; i++) {
//                c[i] = i - 1;
//            }
//        }
//        else {
//
//        }
//        // Initialize sentinels
//        c[k + 1] = n;
//        c[k + 2] = 0;
//        j = k; // Set up invariant: j is smallest index such that c[j + 1] > j
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//
//    public final boolean hasNext() {
//        return more;
//    }
//
//
//    /** 0 <= index < k */
//    public final int get(int index) {
//        return this.c[index+1];
//    }
//
//
//    /* TODO
//    public int[] get(int[] result) {
//            // Copy return value (prepared by last activation)
//        if (ret == null || ret.length!=k)
//            ret = new int[k];
//        System.arraycopy(c, 1, ret, 0, k);
//}
//     */
//
//
//    public final void next() {
//        if (!more) {
//            throw new NoSuchElementException();
//        }
//
//
//        final int[] c = this.c;
//
//        // Prepare next iteration
//        // T2 and T6 loop
//        int x = 0;
//        if (j > 0) {
//            x = j;
//            c[j] = x;
//            j--;
//            return;
//        }
//        // T3
//        if (c[1] + 1 < c[2]) {
//            c[1]++;
//            return;
//        } else {
//            j = 2;
//        }
//        // T4
//        boolean stepDone = false;
//
//        int J = j; //tempoary variable to avoid reaccessing the field
//        while (!stepDone) {
//            c[J - 1] = J - 2;
//            x = c[J] + 1;
//            if (x == c[J + 1]) {
//                J++;
//            } else {
//                stepDone = true;
//            }
//        }
//        this.j = J;
//        // T5
//        if (j > k) {
//            more = false;
//            return;
//        }
//        // T6
//        c[j--] = x;
//        return;
//    }
//
//
// }