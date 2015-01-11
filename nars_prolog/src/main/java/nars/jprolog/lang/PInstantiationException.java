package nars.jprolog.lang;
/**
 * Instantiation error.<br>
 * There will be an instantiation error 
 * when an argument of one of its components is a variable,
 * and an instantiated argument or component is required.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class PInstantiationException extends BuiltinException {
    /** A functor symbol of <code>instantiation/2</code>. */
    public static SymbolTerm INSTANTIATION_ERROR = SymbolTerm.makeSymbol("instantiation_error", 2);

    /** Constructs a new <code>PInstantiationException</code>. */
    public PInstantiationException(){}

    /** Constructs a new <code>PInstantiationException</code>
     * with the given arguments. */
    public PInstantiationException(Predicate _goal, int _argNo) {
	this.goal    = _goal;
	this.argNo   = _argNo;
    }

    /** Returns a term representation of this <code>PInstantiationException</code>:
     * <code>instantiation_error(goal,argNo)</code>.
     */
    public Term getMessageTerm() {
	Term[] args = {new JavaObjectTerm(goal), new IntegerTerm(argNo)};
	return new StructureTerm(INSTANTIATION_ERROR, args);
    }

    /** Returns a string representation of this <code>PInstantiationException</code>. */
    public String toString() {
	String s = "{INSTANTIATION ERROR: " + goal.toString();
	if (argNo > 0)
	    s += " - arg " + argNo;
	s += "}";
	return s;
    }
}
