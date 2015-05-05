package nars.util.data.sexpression;


/** S-expression "Pair" (or "Cell") interface (read-only) */
public interface IPaired {

    /** car, the first element */
    public Object first();

    /** cdr, the remainder (null if non-existent in the case of an atom) */
    public Object rest();

}
