package nars.term.transform;

import nars.Op;
import nars.term.Compound;
import nars.term.Variable;
import nars.util.data.map.FastPutsArrayMap;
import nars.util.utf8.Byted;

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
public class VariableNormalization implements VariableTransform {

//    final static Comparator<Map.Entry<Variable, Variable>> comp = new Comparator<Map.Entry<Variable, Variable>>() {
//        @Override
//        public int compare(Map.Entry<Variable, Variable> c1, Map.Entry<Variable, Variable> c2) {
//            return c1.getKey().compareTo(c2.getKey());
//        }
//    };

    /**
     * overridden keyEquals necessary to implement alternate variable hash/equality test for use in normalization's variable transform hashmap
     */
    static final class VariableMap extends FastPutsArrayMap<Variable, Variable> {



        public VariableMap(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public final boolean keyEquals(final Variable a, final Object ob) {
            if (a == ob) return true;
            Variable b = ((Variable) ob);
            return Byted.equals(a, b);
        }

//        @Override
//        public Variable put(Variable key, Variable value) {
//            Variable removed = super.put(key, value);
//            /*if (size() > 1)
//                Collections.sort(entries, comp);*/
//            return removed;
//        }
    }


    /** for use with compounds that have exactly one variable */
    public static final VariableTransform singleVariableNormalization =
            (containing, current, depth) -> Variable.the(current.op, 1);

    VariableMap rename = null;

    final Compound result;
    boolean renamed = false;

    public VariableNormalization(Compound target, boolean destructively) {


        CompoundTransform tx = target.vars() == 1 ?
                singleVariableNormalization : this;



        final Compound result1;

        if (destructively)
            result1 = target.transform(tx);
        else
            result1 = target.cloneTransforming(tx);

        this.result = result1;

        if (rename != null)
            rename.clear(); //assists GC
    }


    @Override
    public Variable apply(final Compound ct, final Variable v, int depth) {
        Variable vname = v;
//            if (!v.hasVarIndep() && v.isScoped()) //already scoped; ensure uniqueness?
//                vname = vname.toString() + v.getScope().name();


        VariableMap rename = this.rename;

        if (rename == null) this.rename = rename = new VariableMap(2); //lazy allocate

//        Variable vv = rename.get(vname);
//        if (vv == null) {
//            //type + id
//            vv = newVariable(v.getType(), rename.size() + 1);
//            rename.put(vname, vv);
//            renamed = !vv.name().equals(v.name());
//        }

        final VariableMap finalRename = rename;
        Variable vv = rename.computeIfAbsent(vname, _vname -> {
            //type + id
            Variable rvv = newVariable(v.op, finalRename.size() + 1);
            renamed = !Byted.equals(rvv, v);
            return rvv;
        });

        return vv;
    }

    final static protected Variable newVariable(final Op type, final int i) {
        return Variable.the(type, i);
    }

    public boolean hasRenamed() {
        return renamed;
    }

    public Compound getResult() {
        return result;
    }
}
