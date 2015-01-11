package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.lang.IntegerTerm;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.ListTerm;
import nars.jprolog.lang.ExistenceException;
import nars.jprolog.lang.PermissionException;
import nars.jprolog.lang.IllegalDomainException;
import nars.jprolog.lang.RepresentationException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import nars.jprolog.lang.TermException;
import java.io.*;
/**
 * <code>read_line/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
class PRED_read_line_2 extends Predicate {
    Term arg1, arg2;

    public PRED_read_line_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }
    public PRED_read_line_2() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() { return "read_line(" + arg1 + "," + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	Object stream = null;
	String line;
	char[] chars;
	Term t;

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
	// read line
	try {
	    line = (new BufferedReader((PushbackReader)stream)).readLine();
	    if (line == null) { // end_of_stream
		if(! a2.unify(new IntegerTerm(-1), engine.trail))
		    return engine.fail();
		return cont;
	    }
	    chars = line.toCharArray();
	    t = Prolog.Nil;
	    for (int i=chars.length; i>0; i--) {
		if (! Character.isDefined((int)chars[i-1]))
		    throw new RepresentationException(this, 0, "character");
		t = new ListTerm(new IntegerTerm((int)chars[i-1]), t);
	    }
	    if(! a2.unify(t, engine.trail))
		return engine.fail();
	    return cont;
	} catch (IOException e) {
	    throw new TermException(new JavaObjectTerm(e));
	}
    }
}
