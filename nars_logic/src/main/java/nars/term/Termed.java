package nars.term;

/** has, or is associated with a specific term */
@FunctionalInterface
public interface Termed<TT extends Term>  {

    TT term();

}
