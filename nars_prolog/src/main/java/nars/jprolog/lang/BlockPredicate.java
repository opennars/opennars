package nars.jprolog.lang;
/**
 * The <code>BlockPredicate</code> class is used to implement
 * builtin-predicates. For example,
 * <ul>
 * <li><code>catch/3</code>
 * <li><code>synchronized/2</code> (Prolog Cafe specific)
 * </ul>
 * <font color="red">This document is under construction.</font>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public abstract class BlockPredicate extends Predicate {
    public boolean outOfScope = false;
    public boolean outOfLoop  = false;
}




