package nars.term.transform;

import nars.Global;
import nars.term.Compound;
import nars.term.Term;

import java.util.Map;

/**
 * Created by me on 6/1/15.
 */
abstract public class CompoundSubstitution<I extends Compound, T extends Term> implements CompoundTransform<I, T> {

    public final Map<T, T> subst;


    public CompoundSubstitution() {
        this(Global.newHashMap(1));
    }

    public CompoundSubstitution(Map<T, T> subst) {
        this.subst = subst;
    }

    @Override
    public final T apply(I containingCompound, T v, int depth) {
        return subst.computeIfAbsent(v, this::getSubstitute);
    }

    /**
     * returns the substituted value for the given subterm;
     * should not return null
     */
    protected abstract T getSubstitute(T v);
}
