package nars.nal.term.transform;

import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.util.data.FastPutsArrayMap;

import java.util.Map;

/**
 * Created by me on 6/1/15.
 */
abstract public class CompoundSubstitution<I extends Compound, T extends Term> implements CompoundTransform<I, T> {

    public Map<T, T> subst = null;

    @Override
    public T apply(I containingCompound, T v, int depth) {

        if (subst == null)
            subst = newSubstitutionMap();

        T subbed = subst.get(v);

        if (subbed == null) {
            subbed = getSubstitute(v);
            if (subbed == null) return v; //unaffected

            subst.put(v, subbed);
        }

        v = subbed;

        return v;
    }

    protected Map<T, T> newSubstitutionMap() {
        // //Global.newHashMap();
        return new FastPutsArrayMap();
    }

    /**
     * returns the substituted value for the given subterm; null if the subterm should be unaffected
     */
    protected abstract T getSubstitute(T v);
}
