package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.IntegerTerm;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.SymbolTerm;
import nars.jprolog.lang.Term;
/**
   <code>'$get_prolog_impl_flag'/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
class PRED_$get_prolog_impl_flag_2 extends Predicate {
    static SymbolTerm TRUE                      = SymbolTerm.makeSymbol("true");
    static SymbolTerm FALSE                     = SymbolTerm.makeSymbol("false");
    static SymbolTerm BOUNDED                   = SymbolTerm.makeSymbol("bounded");
    static SymbolTerm MAX_INTEGER               = SymbolTerm.makeSymbol("max_integer");
    static SymbolTerm MIN_INTEGER               = SymbolTerm.makeSymbol("min_integer");
    static SymbolTerm INTEGER_ROUNDING_FUNCTION = SymbolTerm.makeSymbol("integer_rounding_function");
    static SymbolTerm CHAR_CONVERSION           = SymbolTerm.makeSymbol("char_conversion");
    static SymbolTerm DEBUG                     = SymbolTerm.makeSymbol("debug");
    static SymbolTerm MAX_ARITY                 = SymbolTerm.makeSymbol("max_arity");
    static SymbolTerm UNKNOWN                   = SymbolTerm.makeSymbol("unknown");
    static SymbolTerm DOUBLE_QUOTES             = SymbolTerm.makeSymbol("double_quotes");
    static SymbolTerm PRINT_STACK_TRACE         = SymbolTerm.makeSymbol("print_stack_trace");

    public Term arg1, arg2;

    public PRED_$get_prolog_impl_flag_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_$get_prolog_impl_flag_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "$get_prolog_impl_flag(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	a1 = a1.dereference();
	a2 = a2.dereference();

	if (a1.equals(BOUNDED)) {
	    if (engine.isBounded()) {
		if (! a2.unify(TRUE, engine.trail))
		    return engine.fail();
	    } else {
		if (! a2.unify(FALSE, engine.trail))
		    return engine.fail();
	    }
	} else if (a1.equals(MAX_INTEGER)) {
	    if (! a2.unify(new IntegerTerm(engine.getMaxInteger()), engine.trail))
		return engine.fail();
	} else if (a1.equals(MIN_INTEGER)) {
	    if (! a2.unify(new IntegerTerm(engine.getMinInteger()), engine.trail))
		return engine.fail();
	} else if (a1.equals(INTEGER_ROUNDING_FUNCTION)) {
	    if (! a2.unify(SymbolTerm.makeSymbol(engine.getIntegerRoundingFunction()), engine.trail))
		return engine.fail();
	} else if (a1.equals(CHAR_CONVERSION)) {
	    if (! a2.unify(SymbolTerm.makeSymbol(engine.getCharConversion()), engine.trail))
		return engine.fail();
	} else if (a1.equals(DEBUG)) {
	    if (! a2.unify(SymbolTerm.makeSymbol(engine.getDebug()), engine.trail))
		return engine.fail();
	} else if (a1.equals(MAX_ARITY)) {
	    if (! a2.unify(new IntegerTerm(engine.getMaxArity()), engine.trail))
		return engine.fail();
	} else if (a1.equals(UNKNOWN)) {
	    if (! a2.unify(SymbolTerm.makeSymbol(engine.getUnknown()), engine.trail))
		return engine.fail();
	} else if (a1.equals(DOUBLE_QUOTES)) {
	    if (! a2.unify(SymbolTerm.makeSymbol(engine.getDoubleQuotes()), engine.trail))
		return engine.fail();
	} else if (a1.equals(PRINT_STACK_TRACE)) {
	    if (! a2.unify(SymbolTerm.makeSymbol(engine.getPrintStackTrace()), engine.trail))
		return engine.fail();
	} else {
	    return engine.fail();
	}
        return cont;
    }
}
