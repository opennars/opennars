package nars.term;

import nars.Op;

/** has, or is associated with a specific term */
@FunctionalInterface
public interface Termed<TT extends Term>  {

    TT term();

    default Op op() { return term().op(); }

    default boolean isAny(int vector) { return term().isAny(vector); }

    default int opRel() {
        return term().opRel();
    }
}
