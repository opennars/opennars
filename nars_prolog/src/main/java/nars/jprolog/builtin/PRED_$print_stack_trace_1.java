package nars.jprolog.builtin;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.IllegalTypeException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
/**
 * <code>'$print_stack_trace'/1</code>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
*/
class PRED_$print_stack_trace_1 extends Predicate {

    public Term arg1;

    public PRED_$print_stack_trace_1(Term a1, Predicate cont) {
        arg1 = a1;
        this.cont = cont;
    }

    public PRED_$print_stack_trace_1(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() {
        return "$print_stack_trace(" + arg1 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1;
        a1 = arg1;

	a1 = a1.dereference();
	if (a1.isVariable())
	    throw new PInstantiationException(this, 1);
	if (! a1.isJavaObject())
	    throw new IllegalTypeException(this, 1, "java", a1);
	Object obj = ((JavaObjectTerm) a1).object();
	if (obj instanceof InterruptedException)
	    System.exit(1);
	if (engine.getPrintStackTrace().equals("on"))
	    ((Exception) obj).printStackTrace();
        return cont;
    }
}
