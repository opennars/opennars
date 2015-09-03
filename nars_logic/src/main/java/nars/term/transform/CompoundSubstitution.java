package nars.term.transform;

import nars.Global;
import nars.term.Compound;
import nars.term.Term;

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

        T subbed = subst.computeIfAbsent(v, _v -> {
            T s = getSubstitute(_v);
            if (s == null) return _v; //unaffected
            return s;
        });

        v = subbed;

        return v;
    }

    final protected Map<T, T> newSubstitutionMap() {
        return Global.newHashMap();
        //return new FastPutsArrayMap<>();
    }

    /**
     * returns the substituted value for the given subterm; null if the subterm should be unaffected
     */
    protected abstract T getSubstitute(T v);
}
