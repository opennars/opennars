package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.Term;
/**
 * <code>current_engine/1</code>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class PRED_current_engine_1 extends Predicate {
     Term arg1;

    public PRED_current_engine_1(Term a1, Predicate cont) {
	arg1 = a1;
	this.cont = cont;
    }
    public PRED_current_engine_1() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() { return "current_engine(" + arg1 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1;
	a1 = arg1;

	a1 = a1.dereference();
	if (! a1.unify(new JavaObjectTerm(engine), engine.trail))
	    return engine.fail();
	return cont;
    }
}
