package nars.jprolog.lang;
import nars.jprolog.Prolog;
import java.io.Serializable;
/**
 * The superclass of classes for predicates.
 * The subclasses of <code>Predicate</code> must override
 * the <code>exec</code> and <code>arity</code> methods.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public abstract class Predicate implements Serializable {
    /** Holds a continuation goal */
    public Predicate cont = null;

    /** 
     * Executes this predicate and returns a continuation goal.
     * @param engine current Prolog engine
     * @exception PrologException if a Prolog exception is raised.
     * @see Prolog
     */
    public abstract Predicate exec(Prolog engine) throws PrologException;

    /** Returns the arity of this predicate. */
    public abstract int arity();

    /** Sets the specified arguments and continuation goal. */
    public void setArgument(Term[] args, Predicate cont){}
}
