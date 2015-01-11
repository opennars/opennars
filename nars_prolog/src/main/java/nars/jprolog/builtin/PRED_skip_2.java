package nars.jprolog.builtin;
import nars.jprolog.lang.IntegerTerm;
import nars.jprolog.lang.NumberTerm;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.ExistenceException;
import nars.jprolog.lang.PermissionException;
import nars.jprolog.lang.IllegalDomainException;
import nars.jprolog.lang.RepresentationException;
import nars.jprolog.lang.Term;
import nars.jprolog.lang.TermException;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.BuiltinException;
import nars.jprolog.lang.Arithmetic;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import java.io.*;
/**
   <code>skip/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_skip_2 extends Predicate {
    public static IntegerTerm INT_EOF = new IntegerTerm(-1);
    public Term arg1, arg2;

    public PRED_skip_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_skip_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "skip(" + arg1 + "," + arg2 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	int n;
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
	if (! Character.isDefined(n))
	    throw new RepresentationException(this, 2, "character_code");
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
	if (! (stream instanceof PushbackReader))
	    throw new PermissionException(this, "input", "stream", a1, "");
	// skip
	try {
	    PushbackReader in = (PushbackReader) stream;
	    int c = in.read();
	    while(c != n) {
		c = in.read();
		if (c == -1) // EOF
		    return cont;
		if (! Character.isDefined(c))
		    throw new RepresentationException(this, 0, "character");
	    } 
	    return cont;
	} catch (IOException e) {
	    throw new TermException(new JavaObjectTerm(e));
	}
    }
}
