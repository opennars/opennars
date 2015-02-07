package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;
/**
   <code>new_hash/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_new_hash_2 extends Predicate {
    public static SymbolTerm SYM_NIL     = SymbolTerm.makeSymbol("[]");
    public static SymbolTerm SYM_ALIAS_1 = SymbolTerm.makeSymbol("alias", 1);
    public Term arg1, arg2;

    public PRED_new_hash_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_new_hash_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "new_hash(" + arg1 + ',' + arg2 + ')';
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;

	a1 = a1.dereference();
	if (! a1.isVariable())
	    throw new IllegalTypeException(this, 1, "variable", a1);
	Term newHash = new JavaObjectTerm(new HashtableOfTerm());
	a2 = a2.dereference();
	if (a2.isNil()) {
	    if (! a1.unify(newHash, engine.trail))
		return engine.fail();
	    return cont;
	} else if (! a2.isList()) {
	    throw new IllegalTypeException(this, 2, "list", a2);
	}
	// a2 is list
	Term tmp = a2;
	while (! tmp.isNil()) {
	    if (tmp.isVariable())
		throw new PInstantiationException(this, 2);
	    if (! tmp.isList())
		throw new IllegalTypeException(this, 2, "list", a2);
	    Term car = ((ListTerm) tmp).car().dereference();
	    if (car.isVariable())
		throw new PInstantiationException(this, 2);
	    if (car.isStructure()) {
		SymbolTerm functor = ((StructureTerm) car).functor();
		Term[] args = ((StructureTerm) car).args();
		if (functor.equals(SYM_ALIAS_1)) {
		    Term alias = args[0].dereference();
		    if (! alias.isSymbol())
			throw new IllegalDomainException(this, 2, "hash_option", car);
		    else {
			if (engine.getHashManager().containsKey(alias))
			    throw new PermissionException(this, "new", "hash", car, "");
			engine.getHashManager().put(alias, newHash);
		    }
		} else {
		    throw new IllegalDomainException(this, 2, "hash_option", car);
		}
	    } else {
		throw new IllegalDomainException(this, 2, "hash_option", car);
	    }
	    tmp = ((ListTerm) tmp).cdr().dereference();
	}
	if (! a1.unify(newHash, engine.trail))
	    return engine.fail();
        return cont;
    }
}
