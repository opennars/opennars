package nars.nal.term;

import nars.nal.term.Term;

/** has, or is associated with a specific term */
public interface Termed<TT extends Term>  {
    public TT getTerm();
}
