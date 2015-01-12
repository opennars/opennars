package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;
/**
 * <code>atom_lengt/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
*/
public class PRED_atom_length_2 extends Predicate {

    public Term arg1, arg2;

    public PRED_atom_length_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_atom_length_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "atom_length(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	int length;

	a1 = a1.dereference();
	a2 = a2.dereference();

	if (a1.isVariable())
	    throw new PInstantiationException(this, 1);
	if (! a1.isSymbol())
	    throw new IllegalTypeException(this, 1, "atom", a1);
	length = ((SymbolTerm)a1).name().length();
	if (a2.isVariable()) {
	    if (! a2.unify(new IntegerTerm(length), engine.trail))
		return engine.fail();
	} else if (a2.isInteger()) {
	    int n = ((IntegerTerm)a2).intValue();
	    if (n < 0)
		throw new IllegalDomainException(this, 2, "not_less_than_zero", a2);
	    if (length != n)
		return engine.fail();
	} else {
	    throw new IllegalTypeException(this, 1, "integer", a2);
	}
        return cont;
    }
}
