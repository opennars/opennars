package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.ClosureTerm;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.Term;
/**
 * <code>'$call_closure'/1</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
class PRED_$call_closure_1 extends Predicate {
    Term arg1;
    Predicate cont;

    public PRED_$call_closure_1(Term a1, Predicate cont) {
	arg1 = a1;
	this.cont = cont;
    }

    public PRED_$call_closure_1() {}

    public void setArgument(Term args[], Predicate cont) {
	arg1 = args[0];
	this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() {
	return "$call_closure(" + arg1 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1;
	Predicate code;

	// a1 must be closure
	a1 = arg1.dereference();

	if (! a1.isClosure())
	    return engine.fail();
	code = ((ClosureTerm) a1).getCode();
	code.cont = this.cont;
	return code;
    }
}


