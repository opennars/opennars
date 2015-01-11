package nars.jprolog.builtin;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.NumberTerm;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.ExistenceException;
import nars.jprolog.lang.BuiltinException;
import nars.jprolog.lang.Arithmetic;
import nars.jprolog.lang.PermissionException;
import nars.jprolog.lang.IllegalDomainException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import java.io.*;
/**
   <code>tab/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_tab_2 extends Predicate {

    public Term arg1, arg2;

    public PRED_tab_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_tab_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "tab(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	int n;
	String s = "";
	Object stream = null;

	// Char
	a2 = a2.dereference(); 
	if (a2.isVariable())
	    throw new PInstantiationException(this, 2);
	if (! a2.isInteger()) {
	    try {
		a2 = Arithmetic.evaluate(a2);
	    } catch (BuiltinException e) {
		e.goal = this;
		e.argNo = 2;
		throw e;
	    }
	}
	n = ((NumberTerm)a2).intValue();
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
	// tab
	for (int i=0; i<n; i++)
	    s += " ";
	((PrintWriter) stream).print(s);
	return cont;
    }
}
