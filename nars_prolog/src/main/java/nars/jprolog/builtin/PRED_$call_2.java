package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.lang.IntegerTerm;
import nars.jprolog.lang.SymbolTerm;
import nars.jprolog.lang.IllegalTypeException;
import nars.jprolog.lang.ExistenceException;
import nars.jprolog.lang.StructureTerm;
import nars.jprolog.lang.SystemException;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import java.lang.reflect.*;
/**
 * <code>'$call'/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
class PRED_$call_2 extends Predicate {
    Term arg1, arg2;
    Predicate cont;
    public static SymbolTerm SYM_SLASH_2 = SymbolTerm.makeSymbol("/", 2);

    public PRED_$call_2() {}
    public PRED_$call_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }

    public void setArgument(Term args[], Predicate cont) {
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() { return "$call(" + arg1 + "," + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
	a1 = arg1.dereference(); // a1 must be atom of package name
	a2 = arg2.dereference(); // a2 must be callable name

	String functor;
	int arity;
	Term[] args;
	Class clazz;
	Constructor constr;
	Predicate pred;

	try {
	    if (! a1.isSymbol())
		throw new IllegalTypeException(this, 1, "atom", a1);
	    if (a2.isSymbol()) {
		functor = ((SymbolTerm)a2).name();
		args    = null;
		arity   = 0;
	    } else if (a2.isStructure()) {
		functor = ((StructureTerm)a2).functor().name();
		args    = ((StructureTerm)a2).args();
		arity   = ((StructureTerm)a2).arity();
	    } else {
		throw new IllegalTypeException(this, 2, "callable", a2);
	    }
	    try {
		clazz = engine.pcl.loadPredicateClass(((SymbolTerm)a1).name(), functor, arity, true);
	    } catch (ClassNotFoundException e) {
		try {
		    clazz = engine.pcl.loadPredicateClass("jp.ac.kobe_u.cs.prolog.builtin", functor, arity, true);
		} catch (ClassNotFoundException ee) {
		    if ((engine.getUnknown()).equals("fail"))
			return engine.fail();
		    Term[] fa = {SymbolTerm.makeSymbol(functor), new IntegerTerm(arity)};
		    throw new ExistenceException(this, 0, "procedure", new StructureTerm(SYM_SLASH_2, fa), "");
		}
	    }
	    constr = clazz.getConstructor();
	    constr.setAccessible(true);
	    pred = (Predicate)constr.newInstance();
	    pred.setArgument(args, cont);
	    return pred;
	} catch (NoSuchMethodException e) {
	    throw new SystemException(e.toString() + " in " + this.toString());
	} catch (InstantiationException e) {
	    throw new SystemException(e.toString() + " in " + this.toString());
	} catch (IllegalAccessException e) {
	    throw new SystemException(e.toString() + " in " + this.toString());
	} catch (SecurityException e) {
	    throw new SystemException(e.toString() + " in " + this.toString());
	} catch (IllegalArgumentException e) {
	    throw new SystemException(e.toString() + " in " + this.toString());
	} catch (InvocationTargetException e) {
	    throw new SystemException(e.toString() + " in " + this.toString());
	}
    }
}


