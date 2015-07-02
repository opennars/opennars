package nars.nal.term;

/** has, or is associated with a specific term */
public interface Termed<TT extends Term>  {
    public TT getTerm();
}
