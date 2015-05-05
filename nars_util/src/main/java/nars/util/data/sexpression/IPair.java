package nars.util.data.sexpression;



/** Mutable form of IPaired the return value allow the implementation to indicate that the set value
 *  has been overridden, or not applied.  */
public interface IPair extends IPaired {

    /** @return the value which was actually set, or null if nothing changed */
    public Object setFirst(Object first);

    /** @return the value which was actually set, or null if nothing changed */
    public Object setRest(Object rest);


    /**
     * Like Common Lisp first; car of a Pair, or null for anything else. *
     */
    public static Object first(Object x) {
        return (x instanceof IPair) ? ((IPair) x).first() : null;
    }


}
