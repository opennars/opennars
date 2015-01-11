package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
/**
 * <code>'$fast_write'/1</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class PRED_$fast_write_1 extends Predicate {
    Term arg1;

    public PRED_$fast_write_1(Term a1, Predicate cont) {
	arg1 = a1;
	this.cont = cont;
    }
    public PRED_$fast_write_1() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() { return "$fast_write(" + arg1 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1;
	a1 = arg1.dereference();
	engine.getCurrentOutput().print(a1.toString());
	return cont;
    }
}
