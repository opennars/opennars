package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
/**
 * <code>nl/0</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class PRED_nl_0 extends Predicate {

    public PRED_nl_0(Predicate cont) {
	this.cont = cont;
    }
    public PRED_nl_0() {}

    public void setArgument(Term[] args, Predicate cont){
	this.cont = cont;
    }

    public int arity() { return 0; }
    public String toString() { return "nl"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	engine.getCurrentOutput().println();
	return cont;
    }
}
