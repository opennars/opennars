package nars.term;

import nars.Op;
import org.infinispan.commons.equivalence.ByteArrayEquivalence;

import java.util.EnumMap;


public class CommonVariable extends Variable  {

    private final Op type;

    CommonVariable(Op type, byte[] n) {
        super();
        this.type = type;
        setBytes(n);
    }

    @Override
    public final Op op() {
        return type;
    }

    @Override
    public final int structure() {
        return 1 << op().ordinal();
    }

    @Override
    public final int vars() {
        return 1;
    }

    @Override
    public final boolean hasVarIndep() {
        return op() == Op.VAR_INDEPENDENT;
    }

    @Override
    public final boolean hasVarDep() {
        return op() == Op.VAR_DEPENDENT;
    }

    @Override
    public final boolean hasVarQuery() {
        return op() == Op.VAR_QUERY;
    }

    @Override
    public final int varIndep() {
        return hasVarIndep() ? 1 : 0;
    }

    @Override
    public final int varDep() {
        return hasVarDep() ? 1 : 0;
    }

    @Override
    public final int varQuery() {
        return hasVarQuery() ? 1 : 0;
    }

    public static CommonVariable make(Variable v1, Variable v2) {


//        if (v1 instanceof CommonVariable) {
//            return (CommonVariable)v1; //combine into common common variable
//            //System.out.println(v1 + " " + v2);
//        }
//        if (v2 instanceof CommonVariable) {
//            return (CommonVariable)v2; //combine into common common variable
//            //System.out.println(v1 + " " + v2);
//        }

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
                    //optimized case: very common, since vars are normalized to digit %1,%2,...%3 often


                    int diff = a[0] - b[0];

                    if (diff==0) {
                        throw new RuntimeException("variables equal");
                    }
                    else if (diff > 0) {
                        return make(type, a[0], b[0]);
                    }
                    else {
                        return make(type, b[0], a[0]);
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



        byte[] c = new byte[len1 + len2 + 1];
        System.arraycopy(a, 0, c, 0, len1);
        System.arraycopy(b, 0, c, len1+1, len2);
        c[len1] = (byte)type.ch;

        return new CommonVariable(type, c);
    }

    //TODO use a 2d array not an enum map, just flatten the 4 op types to 0,1,2,3
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
        byte[] n = new byte[] { a, (byte)type.ch, b };
        return new CommonVariable(type, n);
    }

//    CommonVariable(Op op, byte[] b) {
//        super(op, b);
//    }


}
