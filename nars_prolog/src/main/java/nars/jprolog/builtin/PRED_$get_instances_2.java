package nars.jprolog.builtin;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.IntegerTerm;
import nars.jprolog.lang.SymbolTerm;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.IllegalTypeException;
import nars.jprolog.lang.ListTerm;
import nars.jprolog.lang.StructureTerm;
import nars.jprolog.lang.RepresentationException;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
/**
   <code>'$get_instances'/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.1
*/
class PRED_$get_instances_2 extends Predicate {
    public Term arg1, arg2;
    public static SymbolTerm COMMA = SymbolTerm.makeSymbol(",", 2);

    public PRED_$get_instances_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_$get_instances_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "$get_instances(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	int idx;

	a1 = a1.dereference();
	if (a1.isNil())
	    return engine.fail();
	if (! a1.isList())
	    throw new IllegalTypeException(this, 1, "list", a1);
	Term x = Prolog.Nil;
	Term tmp = a1;
	while(! tmp.isNil()) {
	    if (! tmp.isList())
		throw new IllegalTypeException(this, 1, "list", a1);
	    Term car = ((ListTerm)tmp).car().dereference();
	    if (car.isVariable())
		throw new PInstantiationException(this, 1);
	    if (! car.isInteger()) 
		throw new RepresentationException(this, 1, "integer");
	    // car is an integer
	    int i = ((IntegerTerm)car).intValue();
	    Term e = engine.internalDB.get(i);
	    if (e != null) {
		Term[] arg = {e, car};
		x = new ListTerm(new StructureTerm(COMMA, arg), x);
	    } 
	    //	    else {
	    //		System.out.println("index " + i + " is deleted.");
	    //	    }

	    //	    if (e == null)
	    //		throw new SystemException("invalid index");
	    //	    Term[] arg = {e, car};
	    //	    x = new ListTerm(new StructureTerm(COMMA, arg), x);
	    tmp = ((ListTerm)tmp).cdr().dereference();
	}
	if (! a2.unify(x, engine.trail))
	    return engine.fail();
	return cont;
    }
}
