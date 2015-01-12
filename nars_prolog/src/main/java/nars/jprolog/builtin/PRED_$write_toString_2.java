package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.io.PrintWriter;
/**
 * <code>'$write_toString'/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
class PRED_$write_toString_2 extends Predicate {
    Term arg1, arg2;

    public PRED_$write_toString_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }

    public PRED_$write_toString_2() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2;}

    public String toString() { return "$write_toString(" + arg1 + ", " + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	Object stream = null;

	// S_or_a
	a1 = a1.dereference(); 
	if (a1.isVariable()) {
	    throw new PInstantiationException(this, 1);
	} else if (a1.isSymbol()) {
	    if (! engine.getStreamManager().containsKey(a1))
		throw new ExistenceException(this, 1, "stream", a1, "");
	    stream = ((JavaObjectTerm) engine.getStreamManager().get(a1)).object();
	} else if (a1.isJavaObject()) {
	    stream = ((JavaObjectTerm) a1).object();
	} else {
	    throw new IllegalDomainException(this, 1, "stream_or_alias", a1);
	}
	if (! (stream instanceof PrintWriter))
	    throw new PermissionException(this, "output", "stream", a1, "");
	a2 = a2.dereference(); 
	if (! a2.isJavaObject())
	    throw new IllegalTypeException(this, 2, "java", a2);
	// print java
	((PrintWriter) stream).print(((JavaObjectTerm)a2).object().toString());
	return cont;
    }
}
