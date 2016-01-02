package nars.util.math;


import static com.google.common.math.IntMath.factorial;

/** from: http://stackoverflow.com/a/5578494 */
public final class Combinations {

    private final int[] a;
    private final int n;
    private final int r;

    //TODO avoid BigInteger, maybe long
    private int numLeft;
    private final int total;


    //------------
    // Constructor
    //------------
    public Combinations(int n, int r) {
        if (n < 1) {
            throw new IllegalArgumentException("Set must have at least one element");
        }
        if (r > n) {
            throw new IllegalArgumentException("Subset length can not be greater than set length");
        }
        this.n = n;
        this.r = r;
        a = new int[r];
        int nFact = factorial(n);
        int rFact = factorial(r);
        int nminusrFact = factorial(n - r);
        total = nFact / (rFact * nminusrFact);
        reset();
    }

    //------
    // Reset
    //------
    public void reset() {
        int[] a = this.a;

        int alen = a.length;
        for (int i = 0; i < alen; i++) {
            a[i] = (i) % alen;
        }
        numLeft = total;
    }

    //------------------------------------------------
    // Return number of combinations not yet generated
    //------------------------------------------------
    public int remaining() {
        return numLeft;
    }

    //-----------------------------
    // Are there more combinations?
    //-----------------------------
    public boolean hasNext() {
        return numLeft > 0;
    }

    //------------------------------------
    // Return total number of combinations
    //------------------------------------
    public int getTotal() {
        return total;
    }

//    //------------------
//    // Compute factorial
//    //------------------
//    private BigInteger getFactorial(int n) {
//        BigInteger fact = BigInteger.ONE;
//        for (int i = n; i > 1; i--) {
//            fact = fact.multiply(new BigInteger(Integer.toString(i)));
//        }
//        return fact;
//    }

    public int[] prev() {
        return a;
    }

    //--------------------------------------------------------
    // Generate next combination (algorithm from Rosen p. 286)
    //--------------------------------------------------------
    public int[] next() {

        if (numLeft == total) {
            numLeft--;
            return a;
        }

        int[] a = this.a;


        int i = r - 1;
        while (a[i] == n - r + i) {
            i--;
        }
        a[i]++;
        for (int j = i + 1; j < r; j++) {
            a[j] = a[i] + j - i;
        }

        numLeft--;
        return a;

    }
}
