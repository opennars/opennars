package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.Term;
import nars.jprolog.lang.TermException;
/**
 * <code>raise_exception/1</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class PRED_raise_exception_1 extends Predicate {
    Term arg1;

    public PRED_raise_exception_1() {}
    public PRED_raise_exception_1(Term a1, Predicate cont) {
	arg1 = a1;
	this.cont = cont;
    }

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() { return "raise_exception(" + arg1 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1;
	a1 = arg1;

	a1 = a1.dereference();
	if (a1.isVariable())
	    throw new PInstantiationException(this, 1);
	throw new TermException(a1);
    }
}
