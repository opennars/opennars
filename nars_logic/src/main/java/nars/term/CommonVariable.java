package nars.term;

import nars.Op;
import org.infinispan.commons.equivalence.ByteArrayEquivalence;

import java.util.EnumMap;

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

        final Op type = v1.op();
        if (v2.op()!=type)
            throw new RuntimeException("differing types");

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


                    int diff = a[1] - b[1];

                    if (diff==0) {
                        throw new RuntimeException("variables equal");
                    }
                    else if (diff > 0) {
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

    /** variables x 10 (digits) x (1..10) (digits) cache;
     *  triangular matrix because the pairs is sorted */
    static final EnumMap<Op,CommonVariable[][]> common = new EnumMap(Op.class);
    static {
        for (Op o : new Op[] { Op.VAR_PATTERN, Op.VAR_QUERY, Op.VAR_INDEPENDENT, Op.VAR_DEPENDENT }) {
            CommonVariable[][] cm = new CommonVariable[10][];
            for (int i = 0; i < 10; i++) {
                cm[i] = new CommonVariable[i+2];
            }
            common.put(o, cm);
        }
    }

    /** sorted: a < b */
    private static CommonVariable make(final Op type, final byte a, final byte b) {
        //attempt to provide cached version if both are digit chars
        //assumes beginning at %1, %2...
        final int ca = a - '1';
        final int cb = b - '1';
        if (((ca >= 0) && (ca < 10)) && (cb >= 0) && (cb < 10)) {
            CommonVariable[] commonCA = common.get(type)[ca];
            CommonVariable cv = commonCA[cb];
            if (cv == null) {
                commonCA[cb] = cv = make2(type, b, a);
            }
            return cv;
        }
        return make2(type, a, b);
    }

    static CommonVariable make2(Op type, byte a, byte b) {
        byte[] n = new byte[] { (byte)type.ch, a, (byte)type.ch, b };
        return new CommonVariable(n);
    }

    CommonVariable(byte[] b) {
        super(b);
    }


}
