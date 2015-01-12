package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.io.PrintWriter;
/**
   <code>put_code/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_put_code_2 extends Predicate {

    public Term arg1, arg2;

    public PRED_put_code_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_put_code_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "put_code(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	int c;
	Object stream = null;

	// Char
	a2 = a2.dereference(); 
	if (a2.isVariable())
	    throw new PInstantiationException(this, 2);
	if (! a2.isInteger())
	    throw new IllegalTypeException(this, 2, "integer", a2);
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
	// print single character
	c = ((IntegerTerm)a2).intValue();
	if (! Character.isDefined(c))
	    throw new RepresentationException(this, 2, "character");
	((PrintWriter) stream).print((char)c);
	return cont;
    }
}
