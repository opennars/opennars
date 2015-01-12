package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.util.Arrays;
/**
 * <code>sort/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PRED_sort_2 extends Predicate {
    static SymbolTerm Nil = SymbolTerm.makeSymbol("[]");
    Term arg1, arg2;

    public PRED_sort_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }

    public PRED_sort_2() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2 ; }

    public String toString() { return "sort(" + arg1 + "," + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
	a1 = arg1;
	a2 = arg2;
	int len;
	Term tmp, tmp2;
	Term[] list;

	a1 = a1.dereference();
	if (a1.isVariable()) {
	    throw new PInstantiationException(this, 1);
	} else if (a1.equals(Nil)) {
	    if (! a2.unify(Nil, engine.trail))
		return engine.fail();
	    return cont;
	} else if (! a1.isList()) {
	    throw new IllegalTypeException(this, 1, "list", a1);
	}
	len = ((ListTerm)a1).length();
	list = new Term[len];
	tmp = a1;
	for (int i=0; i<len; i++) {
	    if (! tmp.isList())
		throw new IllegalTypeException(this, 1, "list", a1);
	    list[i] = ((ListTerm)tmp).car().dereference();
	    tmp = ((ListTerm)tmp).cdr().dereference();
	}
	if (! tmp.equals(Nil))
	    throw new PInstantiationException(this, 1);
	try {
	    Arrays.sort(list);
	} catch (ClassCastException e) {
	    throw new JavaException(this, 1, e);
	}
	tmp = Nil;
	tmp2 = null;
	for (int i=list.length-1; i>=0; i--) {
	    if (! list[i].equals(tmp2))
		tmp = new ListTerm(list[i], tmp);
	    tmp2 = list[i];
	}
	if(! a2.unify(tmp, engine.trail)) 
	    return engine.fail();
	return cont;
    }
}
