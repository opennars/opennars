package nars.util.utf8;

/**
 * Has and can change its byte representation
 *
 */
public interface Byted {

    /**
     * ordinary array equals comparison with some conditions removed
     * instance equality between A and B will most likely already performed prior to calling this, so it is not done in this method
     */
    static boolean equals(final Byted A, final Byted B) {

        if (A.hashCode() != B.hashCode())
            return false;

        final byte[] a = A.bytes();
        final byte[] b = B.bytes();

        if (a == b)
            return true;

        final int aLen = a.length;
        if (b.length != aLen)
            return false;

        for (int i = 0; i < aLen; i++) {
            if (a[i] != b[i]) {
                //if this happens, it could indicate a BAD HASHING strategy
                return false;
            }
        }

        return true;
    }

    static int compare(final Byted A, final Byted B) {
        //if (A==B) return 0;

        int d = Integer.compare(A.hashCode(), B.hashCode());
        if (d!=0) return d;


        final byte[] a = A.bytes();
        final byte[] b = B.bytes();

        if (a == b)
            return 0;

        int alen = a.length;
        int e = Integer.compare(alen, b.length);
        if (e!=0) return e;

        for(int i = 0; i < alen; ++i) {
            int compareResult = Integer.compare(a[i], b[i]);
            if(compareResult != 0) {
                return compareResult;
            }
        }


        /*
            //determined to be equal, share instances
            B.setBytes(a);
        }*/

        return 0;
    }



    public byte[] bytes();

    public void setBytes(byte[] b);


}
