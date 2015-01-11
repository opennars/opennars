package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.lang.IllegalTypeException;
import nars.jprolog.lang.OutOfScope;
import nars.jprolog.lang.SystemException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
/**
 * <code>'$end_sync'/1</code>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
class PRED_$end_sync_1 extends Predicate {
    Term arg1;

    public PRED_$end_sync_1(Term a1, Predicate cont) {
	arg1 = a1;
	this.cont = cont;
    }
    public PRED_$end_sync_1() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() { return "$end_sync(" + arg1 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1;
	a1 = arg1;

	a1 = a1.dereference();
	if (! a1.isJavaObject())
	    throw new IllegalTypeException(this, 1, "java", a1);
	Object obj = ((JavaObjectTerm)a1).object();
	if (! (obj instanceof PRED_$begin_sync_2))
	    throw new SystemException("a1 must be an object of PRED_$begin_sync_2: " + this);
	PRED_$begin_sync_2 p = ((PRED_$begin_sync_2) obj);
	p.outOfScope = true;
	engine.trail.push(new OutOfScope(p));
	return cont;
    }
}
