package nars.nal.transform;

import nars.nal.term.Compound;
import nars.nal.term.Variable;
import nars.util.data.FastPutsArrayMap;
import nars.util.utf8.Utf8;

import java.util.Comparator;

/**
 * Variable normalization
 *
 * Destructive mode modifies the input Compound instance, which is
 * fine if the concept has been created and unreferenced.
 *
 * The term 'destructive' is used because it effectively destroys some
 * information - the particular labels the input has attached.
 */
public class VariableNormalization implements VariableTransform {

    /**
     * overridden keyEquals necessary to implement alternate variable hash/equality test for use in normalization's variable transform hashmap
     */
    static final class VariableMap extends FastPutsArrayMap<Variable, Variable> {

        final static Comparator<Entry<Variable, Variable>> comp = new Comparator<Entry<Variable, Variable>>() {
            @Override
            public int compare(Entry<Variable, Variable> c1, Entry<Variable, Variable> c2) {
                return c1.getKey().compareTo(c2.getKey());
            }
        };

        public VariableMap(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        public boolean keyEquals(final Variable a, final Object ob) {
            if (a == ob) return true;
            Variable b = ((Variable) ob);
            if (!b.isScoped() || !a.isScoped())
                return false;
            return a.name().equals(b.name());
        }

        @Override
        public Variable put(Variable key, Variable value) {
            Variable removed = super.put(key, value);
            /*if (size() > 1)
                Collections.sort(entries, comp);*/
            return removed;
        }
    }

    VariableMap rename = null;

    final Compound result;
    boolean renamed = false;

    public VariableNormalization(Compound target) {
        this(target, false);
    }

    public VariableNormalization(Compound target, boolean destructively) {


        Compound result1;
        if (destructively)
            result1 = target.transform(this);
        else
            result1 = target.cloneTransforming(this);

        this.result = result1;

        if (rename != null)
            rename.clear(); //assists GC
    }


    @Override
    public Variable apply(final Compound ct, final Variable v, int depth) {
        Variable vname = v;
//            if (!v.hasVarIndep() && v.isScoped()) //already scoped; ensure uniqueness?
//                vname = vname.toString() + v.getScope().name();


        if (rename == null) rename = new VariableMap(2); //lazy allocate

        Variable vv = rename.get(vname);

        if (vv == null) {
            //type + id
            vv = new Variable(
                    Variable.name(v.getType(), rename.size() + 1),
                    true
            );
            rename.put(vname, vv);
            renamed = !vv.name().equals(v.name());
        }

        return vv;
    }

    public boolean hasRenamed() {
        return renamed;
    }

    public Compound getResult() {
        return result;
    }
}
