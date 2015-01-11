package nars.jprolog.lang;
/**
 * Syntax error.<br>
 * There will be a syntax error when a sequence of characters
 * which are being input as read-term do not conform to the syntax.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class SyntaxException extends BuiltinException {
    /** A functor symbol of <code>syntax_error/5</code>. */
    public static SymbolTerm SYNTAX_ERROR = SymbolTerm.makeSymbol("syntax_error", 5);

    /** Holds a string representation of valid type. */
    public String type;

    /** Holds the argument or one of its components which caused the error. */
    public Term culprit;

    /** Holds a string representation of detail message. */
    public String message;

    /** Constructs a new <code>SyntaxException</code> 
     * with a valid type, its culprit, and message. */
    public SyntaxException(String _type, Term _culprit, String _message) {
	type       = _type;
	culprit    = _culprit;
	message    = _message;
    }

    /** Constructs a new <code>SyntaxException</code> with the given arguments. */
    public SyntaxException(Predicate _goal, int _argNo, String _type, Term _culprit, String _message) {
	this.goal  = _goal;
	this.argNo = _argNo;
	type       = _type;
	culprit    = _culprit;
	message    = _message;
    }

    /** Returns a term representation of this <code>SyntaxException</code>:
     * <code>syntax_error(goal,argNo,type,culprit,message)</code>.
     */
    public Term getMessageTerm() {
	Term[] args = {
	    new JavaObjectTerm(goal), 
	    new IntegerTerm(argNo), 
	    SymbolTerm.makeSymbol(type),
	    culprit,
	    SymbolTerm.makeSymbol(message) };
	return new StructureTerm(SYNTAX_ERROR, args);
    }

    /** Returns a string representation of this <code>SyntaxException</code>. */
    public String toString() {
	String s = "{SYNTAX ERROR: " + goal.toString();
	if (argNo > 0)
	    s += " - arg " + argNo;
	s += ": expected " + type;
	s += ", found " + culprit.toString();
	s += "}";
	return s;
    }
}
