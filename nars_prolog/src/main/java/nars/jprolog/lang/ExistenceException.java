package nars.jprolog.lang;
/**
 * Existence error.<br>
 * There will be an existence error when the object
 * on which an operation is to be performed does not exist.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class ExistenceException extends BuiltinException {
    /** A functor symbol of <code>existence_error/5</code>. */
    public static SymbolTerm EXISTENCE_ERROR = SymbolTerm.makeSymbol("existence_error", 5);

    /* objType ::= procedure | source_sink | stream | hash */
    /** Holds a string representation of object type. */
    public String objType;

    /** Holds the argument or one of its components which caused the error. */
    public Term   culprit;

    /** Holds a string representation of detail message. */
    public String message;

    /** Constructs a new <code>ExistenceException</code>
     * with a object type, its culprit, and message. */
    public ExistenceException(String _objType, Term _culprit, String _message) {
	objType    = _objType;
	culprit    = _culprit;
	message    = _message;
    }

    /** Constructs a new <code>ExistenceException</code> 
     * with the given arguments. */
    public ExistenceException(Predicate _goal, int _argNo, String _objType, Term _culprit, String _message) {
	this.goal  = _goal;
	this.argNo = _argNo;
	objType    = _objType;
	culprit    = _culprit;
	message    = _message;
    }

    /** Returns a term representation of this <code>ExistenceException</code>:
     * <code>existence_error(goal,argNo,objType,culprit,message)</code>.
     */
    public Term getMessageTerm() {
	Term[] args = {
	    new JavaObjectTerm(goal), 
	    new IntegerTerm(argNo),
	    SymbolTerm.makeSymbol(objType),
	    culprit,
	    SymbolTerm.makeSymbol(message)};
	return new StructureTerm(EXISTENCE_ERROR, args);
    }

    /** Returns a string representation of this <code>ExistenceException</code>. */
    public String toString() {
	String s = "{EXISTENCE ERROR:";
	if (argNo > 0)
	    s += ' ' + goal.toString() + " - arg " + argNo + ':';
	s += objType + ' ' + culprit.toString() + " does not exist";
	s += "}";
	return s;
    }
}
