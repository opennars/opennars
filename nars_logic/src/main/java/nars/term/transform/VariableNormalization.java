package nars.term.transform;

import nars.$;
import nars.Global;
import nars.term.compound.Compound;
import nars.term.variable.Variable;

import java.util.Map;

/**
 * Variable normalization
 *
 * Destructive mode modifies the input Compound instance, which is
 * fine if the concept has been created and unreferenced.
 *
 * The term 'destructive' is used because it effectively destroys some
 * information - the particular labels the input has attached.
 *
 */
public class VariableNormalization extends VariableTransform {

//    final static Comparator<Map.Entry<Variable, Variable>> comp = new Comparator<Map.Entry<Variable, Variable>>() {
//        @Override
//        public int compare(Map.Entry<Variable, Variable> c1, Map.Entry<Variable, Variable> c2) {
//            return c1.getKey().compareTo(c2.getKey());
//        }
//    };

//    /**
//     * overridden keyEquals necessary to implement alternate variable hash/equality test for use in normalization's variable transform hashmap
//     */
//    static final class VariableMap extends FastPutsArrayMap<Pair<Variable,Term>, Variable> {
//
//
//
//        public VariableMap(int initialCapacity) {
//            super(initialCapacity);
//        }
//
//        @Override
//        public final boolean keyEquals(final Variable a, final Object ob) {
//            if (a == ob) return true;
//            Variable b = ((Variable) ob);
//            return Byted.equals(a, b);
//        }
//
////        @Override
////        public Variable put(Variable key, Variable value) {
////            Variable removed = super.put(key, value);
////            /*if (size() > 1)
////                Collections.sort(entries, comp);*/
////            return removed;
////        }
//    }


    /** for use with compounds that have exactly one variable */
    public static final VariableTransform singleVariableNormalization = new VariableTransform() {

        @Override
        public Variable apply(Compound containing, Variable current, int depth) {
            //      (containing, current, depth) ->
            return $.v(current.op(), 1);
        }
    };


    final Map<Variable, Variable> rename = Global.newHashMap(8);

    protected final Compound result;
    boolean renamed = false;


    public static VariableNormalization normalize(Compound target) {
        return new VariableNormalization(target, null);
    }

    /** allows using the single variable normalization,
     * which is safe if the term doesnt contain pattern variables */
    public static VariableNormalization normalizeFast(Compound target) {
        return new VariableNormalization(target, target.vars() == 1 ?
                singleVariableNormalization : null);
    }


    public VariableNormalization(Compound target) {
        this(target, null);
    }

    public VariableNormalization(Compound target, CompoundTransform tx) {

        if (tx == null) tx = this;


        result = target.get(tx);

    }

    public Variable apply(Variable v) {
        return apply(null, v, -1);
    }

    @Override
    public Variable apply(Compound ct, Variable v, int depth) {

        Map<Variable, Variable> rename = this.rename;

        return rename.computeIfAbsent(resolve(v), (_vname) -> {
            Variable rvv = newVariable(v, rename.size()+1);
            if (!renamed) {//test for any rename to know if we need to rehash
                renamed = rvv.equals(v);//!Byted.equals(rvv, v);
            }
            return rvv;
        });

    }

    /** allows subclasses to provide a different name of a variable */
    protected Variable resolve(Variable v) {
        return v;
    }

    /** if already normalized, alreadyNormalized will be non-null with the value */
    protected Variable newVariable(Variable v, int serial) {
        return $.v(v.op(), serial);  //type + id
    }

    public final Compound get() {
        return result;
    }
}
