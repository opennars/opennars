package nars.term;

/** has, or is associated with a specific term */
public interface Termed<TT extends Term>  {

    TT getTerm();

}
