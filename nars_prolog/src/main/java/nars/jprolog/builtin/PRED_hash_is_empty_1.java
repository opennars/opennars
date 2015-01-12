package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;
/**
   <code>hash_is_empty/1</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_hash_is_empty_1 extends Predicate {

    public Term arg1;

    public PRED_hash_is_empty_1(Term a1, Predicate cont) {
        arg1 = a1;
        this.cont = cont;
    }

    public PRED_hash_is_empty_1(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() {
        return "hash_is_empty(" + arg1 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1;
        a1 = arg1;

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
	if (! ((HashtableOfTerm) hash).isEmpty())
	    return engine.fail();
        return cont;
    }
}
