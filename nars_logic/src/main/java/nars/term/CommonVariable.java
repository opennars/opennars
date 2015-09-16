package nars.term;

import org.infinispan.commons.equivalence.ByteArrayEquivalence;

/**
 * Created by me on 9/9/15.
 */
public class CommonVariable extends Variable {

    public static CommonVariable make(Variable v1, Variable v2) {

        if (v1 instanceof CommonVariable) {
            return (CommonVariable)v1; //combine into common common variable
            //System.out.println(v1 + " " + v2);
        }
        if (v2 instanceof CommonVariable) {
            return (CommonVariable)v2; //combine into common common variable
            //System.out.println(v1 + " " + v2);
        }

        //TODO use more efficient string construction
        byte[] a = v1.bytes();
        byte[] b = v2.bytes();


        int len1 = a.length;
        int len2 = b.length;
        int cmp = Integer.compare(len1, len2);
        if (cmp == 0) {
            //same length
            switch (len1) {
                case 1:
                    //vars would be at least 2 bytes
                    throw new RuntimeException("should not happen");
                    //return Byte.compare(a[0], b[0]);


                case 2:
                    //optimized case: very common, since vars are normalized to digit %1,%2,...%3 often
                    byte type = a[0];
                    if (b[0]!=type) throw new RuntimeException("common variable type mismatch");
                    int diff = a[1] - b[1];

                    if (diff==0) {
                        throw new RuntimeException("variables equal");
                    }
                    else if (diff < 0) {
                        return CommonVariable.make(type, a[1], b[1]);
                    }
                    else {
                        return CommonVariable.make(type, b[1], a[1]);
                    }

                default:
                    cmp = ByteArrayEquivalence.INSTANCE.compare(a, b);
            }
        }


        //lexical ordering: swap
        if (cmp > 0) {
            byte[] t = a;
            a = b;
            b = t;

            len1 = a.length;
            len2 = b.length;
        }
        else if (cmp == 0) {
            throw new RuntimeException("variables equal");
        }


        int len = len1 + len2;
        byte[] c = new byte[len];
        System.arraycopy(a, 0, c, 0, len1);
        System.arraycopy(b, 0, c, len1, len2);

        return new CommonVariable(c);
    }

    static CommonVariable common[][] = new CommonVariable[10][];
    static {
        for (int i = 0; i < 10; i++)
            common[i] = new CommonVariable[10-i];
    }

    /** sorted: a < b */
    private static CommonVariable make(final byte type, final byte a, final byte b) {
        //attempt to provide cached version if both are digit chars
        //assumes beginning at %1, %2...
        final int ca = a - '1';
        final int cb = b - '1';
        if (((ca >= 0) && (ca < 10)) && (cb >= 0) && (cb < 10)) {
            CommonVariable[] commonCA = common[ca];
            CommonVariable cv = commonCA[cb];
            if (cv == null) {
                commonCA[cb] = cv = make2(type, a, b);
            }
            return cv;
        }
        return make2(type, a, b);
    }

    static CommonVariable make2(byte type, byte a, byte b) {
        byte[] n = new byte[] { type, a, type, b };
        return new CommonVariable(n);
    }

    CommonVariable(byte[] b) {
        super(b);
    }


}
