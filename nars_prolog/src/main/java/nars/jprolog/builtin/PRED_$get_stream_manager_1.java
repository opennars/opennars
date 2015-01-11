package nars.jprolog.builtin;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.IllegalTypeException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
/**
   <code>'$get_stream_manager'/1</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
class PRED_$get_stream_manager_1 extends Predicate {

    public Term arg1;

    public PRED_$get_stream_manager_1(Term a1, Predicate cont) {
        arg1 = a1;
        this.cont = cont;
    }

    public PRED_$get_stream_manager_1(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() {
        return "$get_stream_manager(" + arg1 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1;
        a1 = arg1;

	a1 = a1.dereference();
	if (! a1.isVariable())
	    throw new IllegalTypeException(this, 1, "variable", a1);
	if (! a1.unify(new JavaObjectTerm(engine.getStreamManager()), engine.trail))
	    return engine.fail();
        return cont;
    }
}
