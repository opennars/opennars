package nars.term;

import nars.Op;

import java.util.EnumMap;


/** type should not be PATTERN_VAR */
public class CommonVariable extends Variable  {

    private final Op type;

    CommonVariable(Op type, String n) {
        super(n, type);
        this.type = type;
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
    public final int varIndep() {
        return op() == Op.VAR_INDEPENDENT ? 1 : 0;
    }

    @Override
    public final int varDep() {
        return op() == Op.VAR_DEPENDENT ? 1 : 0;
    }

    @Override
    public final int varQuery() {
        return op() == Op.VAR_QUERY ? 1 : 0;
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

        String a = v1.id;
        String b = v2.id;

        int cmp = a.compareTo(b);

        //lexical ordering: swap
        if (cmp > 0) {
            String t = a;
            a = b;
            b = t;
        }
        else if (cmp == 0) {
            throw new RuntimeException("variables equal");
        }

        return new CommonVariable(type, a + type + b);
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



}
