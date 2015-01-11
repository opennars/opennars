package nars.jprolog.builtin;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.SymbolTerm;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.HashtableOfTerm;
import nars.jprolog.lang.ListTerm;
import nars.jprolog.lang.ExistenceException;
import nars.jprolog.lang.IllegalDomainException;
import nars.jprolog.lang.InternalException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import java.util.Hashtable;
/**
   <code>'$hash_addz'/3</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
class PRED_$hash_addz_3 extends Predicate {
    public static SymbolTerm SYM_NIL = SymbolTerm.makeSymbol("[]");
    public Term arg1, arg2, arg3;

    public PRED_$hash_addz_3(Term a1, Term a2, Term a3, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        arg3 = a3;
        this.cont = cont;
    }

    public PRED_$hash_addz_3(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        arg3 = args[2];
        this.cont = cont;
    }

    public int arity() { return 3; }

    public String toString() {
        return "$hash_addz(" + arg1 + "," + arg2 + "," + arg3 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2, a3;
        a1 = arg1;
        a2 = arg2;
        a3 = arg3;

	Object hash = null;

	a1 = a1.dereference();
	if (a1.isVariable()) {
	    throw new PInstantiationException(this, 1);
	} else if (a1.isSymbol()) {
	    if (! engine.getHashManager().containsKey(a1))
		throw new ExistenceException(this, 1, "hash", a1, "");
	    hash = ((JavaObjectTerm) engine.getHashManager().get(a1)).object();
	} else if (a1.isJavaObject()) {
	    hash = ((JavaObjectTerm) a1).object();
	} else {
	    throw new IllegalDomainException(this, 1, "hash_or_alias", a1);
	}
	if (! (hash instanceof HashtableOfTerm))
	    throw new InternalException(this + ": Hash is not HashtableOfTerm");
	a2 = a2.dereference();
	Term elem = ((HashtableOfTerm) hash).get(a2);
	if (elem == null)
	    elem = SYM_NIL;
	a3 = a3.dereference();
	if (elem.isNil()) {
	    elem = new ListTerm(a3, elem);
	} else {
	    Term x = elem;
	    Term y;
	    while(true) {
		if (! x.isList())
		    throw new InternalException(this + ": the valus of " + a2 + " is not list structure");
		y = ((ListTerm)x).cdr().dereference();
		if (y.isNil()) {
		    ((ListTerm)x).setCdr(new ListTerm(a3, SYM_NIL));
		    break;
		}
		x = y;
	    }
	}
	((HashtableOfTerm) hash).put(a2, elem);
        return cont;
    }
}
