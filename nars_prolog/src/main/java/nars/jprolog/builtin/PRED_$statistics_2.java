package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;
/**
   <code>'$statistics'/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
class PRED_$statistics_2 extends Predicate {
    public static SymbolTerm Nil         = SymbolTerm.makeSymbol("[]");
    public static SymbolTerm SYM_RUNTIME = SymbolTerm.makeSymbol("runtime");
    public static SymbolTerm SYM_TRAIL   = SymbolTerm.makeSymbol("trail");
    public static SymbolTerm SYM_CHOICE  = SymbolTerm.makeSymbol("choice");

    Term arg1, arg2;

    public PRED_$statistics_2(Term a1, Term a2, Predicate cont){
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }
    public PRED_$statistics_2(){}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2 ; }

    public String toString(){ return "$statistics(" + arg1 + "," + arg2 + ")"; }

    public Predicate exec(Prolog engine){
        engine.setB0();
	Term a1, a2;
	a1 = arg1;
	a2 = arg2;
	Term result = null;

	a1 = a1.dereference();
	if (a1.isVariable()) {
	    throw new PInstantiationException(this, 1);
	} else if (! a1.isSymbol()) {
	    throw new IllegalTypeException(this, 1, "atom", a1);
	} else if (a1.equals(SYM_RUNTIME)) {
	    long val1, val2;
	    Term start, previous;
	    val1 = System.currentTimeMillis() - engine.getStartRuntime();
	    val2 = val1 - engine.getPreviousRuntime();
	    engine.setPreviousRuntime(val1);
	    start    = new IntegerTerm((int)val1);
	    previous = new IntegerTerm((int)val2);
	    result   = new ListTerm(start, new ListTerm(previous, Nil));
	} else if (a1.equals(SYM_TRAIL)) {
	    int top, max;
	    Term free, used;
	    top    = engine.trail.top();
	    max    = engine.trail.max();
	    used   = new IntegerTerm(top);
	    free   = new IntegerTerm(max - top);
	    result = new ListTerm(used, new ListTerm(free, Nil));
	} else if (a1.equals(SYM_CHOICE)) {
	    int top, max;
	    Term free, used;
	    top    = engine.stack.top();
	    max    = engine.stack.max();
	    used   = new IntegerTerm(top);
	    free   = new IntegerTerm(max - top);
	    result = new ListTerm(used, new ListTerm(free, Nil));
	} else {
	    return engine.fail();
	}
	if (! a2.unify(result, engine.trail)) 
	    return engine.fail();
	return cont;
    }
}
