package nars.jprolog.lang;
/**
 * Representation error.<br>
 * There will be a representation error when an implementation
 * defined limit has been breached.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class RepresentationException extends BuiltinException {
    /** A functor symbol of <code>representation_error/3</code>. */
    public static SymbolTerm REPRESENTATION_ERROR = SymbolTerm.makeSymbol("representation_error", 3);

    /*
      flag ::= character | character_code | in_character_code | 
               max_arity | max_integer | min_integer
    */
    /** Holds a string representation of flag. */
    public String flag;

    /** Constructs a new <code>RepresentationException</code> with a flag. */
    public RepresentationException(String _flag) {
	flag    = _flag;
    }

    /** Constructs a new <code>RepresentationException</code> with the given arguments. */
    public RepresentationException(Predicate _goal, int _argNo, String _flag) {
	this.goal    = _goal;
	this.argNo   = _argNo;
	flag    = _flag;
    }

    /** Returns a term representation of this <code>RepresentationException</code>:
     * <code>representation_error(goal,argNo,flag)</code>.
     */
    public Term getMessageTerm() {
	Term[] args = {
	    new JavaObjectTerm(goal), 
	    new IntegerTerm(argNo), 
	    SymbolTerm.makeSymbol(flag)};
	return new StructureTerm(REPRESENTATION_ERROR, args);
    }

    /** Returns a string representation of this <code>RepresentationException</code>. */
    public String toString() {
	String s = "{REPRESENTATION ERROR: " + goal.toString();
	if (argNo > 0)
	    s += " - arg " + argNo;
	s += ": limit of " + flag +  " is breached";
	s += "}";
	return s;
    }
}
