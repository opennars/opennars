package nars.jprolog.lang;
/**
 * Domain error.<br>
 * There will be a domain error when the type of an argument
 * is correct, but the value is outside the domain for which
 * the procedure is defined.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class IllegalDomainException extends BuiltinException {
    /** A functor symbol of <code>domain_error/4</code>. */
    public static SymbolTerm DOMAIN_ERROR = SymbolTerm.makeSymbol("domain_error", 4);

    /*
      domain ::= character_code_list | close_option | flag_value | io_mode | 
                 non_empty_list | not_less_than_zero | operator_priority |
		 operator_specifier | prolog_flag | read_option | source_sink |
		 stream | steam_option | stream_or_alias | stream_position |
		 stream_property | write_option |
		 hash_or_alias| hash_option | 'arithmetic expression'
    */
    /** Holds a string representation of valid domain. */
    public String domain;

    /** Holds the argument or one of its components which caused the error. */
    public Term culprit;

    /** Constructs a new <code>IllegalDomainException</code>
     * with a valid domain and its culprit. */
    public IllegalDomainException(String _domain, Term _culprit) {
	domain    = _domain;
	culprit = _culprit;
    }

    /** Constructs a new <code>IllegalDomainException</code> 
     * with the given arguments. */
    public IllegalDomainException(Predicate _goal, int _argNo, String _domain, Term _culprit) {
	this.goal    = _goal;
	this.argNo   = _argNo;
	domain    = _domain;
	culprit = _culprit;
    }

    /** Returns a term representation of this <code>IllegalDomainException</code>:
     * <code>domain_error(goal,argNo,type,culprit)</code>.
     */
    public Term getMessageTerm() {
	Term[] args = {
	    new JavaObjectTerm(goal), 
	    new IntegerTerm(argNo), 
	    SymbolTerm.makeSymbol(domain),
	    culprit};
	return new StructureTerm(DOMAIN_ERROR, args);
    }

    /** Returns a string representation of this <code>IllegalDomainException</code>. */
    public String toString() {
	String s = "{DOMAIN ERROR: " + goal.toString();
	if (argNo > 0)
	    s += " - arg " + argNo;
	s += ": expected " + domain;
	s += ", found " + culprit.toString();
	s += "}";
	return s;
    }
}
