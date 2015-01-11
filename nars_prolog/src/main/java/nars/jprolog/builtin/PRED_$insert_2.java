package nars.jprolog.builtin;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.IntegerTerm;
import nars.jprolog.lang.IllegalTypeException;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
/**
   <code>'$insert'/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.1
*/
class PRED_$insert_2 extends Predicate {
    public Term arg1, arg2;

    public PRED_$insert_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_$insert_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "$insert(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	int idx;

	a2 = a2.dereference();
	if (! a2.isVariable())
	    throw new IllegalTypeException(this, 2, "variable", a2);
	a1 = a1.dereference();
	idx = engine.internalDB.insert(a1);
	if (! a2.unify(new IntegerTerm(idx), engine.trail))
	    return engine.fail();
	return cont;
    }
}
