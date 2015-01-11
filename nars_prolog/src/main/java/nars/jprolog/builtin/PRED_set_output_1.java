package nars.jprolog.builtin;
import nars.jprolog.lang.Predicate;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.ExistenceException;
import nars.jprolog.lang.PermissionException;
import nars.jprolog.lang.IllegalDomainException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import java.io.PrintWriter;
/**
   <code>set_output/1</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_set_output_1 extends Predicate {

    public Term arg1;

    public PRED_set_output_1(Term a1, Predicate cont) {
        arg1 = a1;
        this.cont = cont;
    }

    public PRED_set_output_1(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() {
        return "set_output(" + arg1 + ")";
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1;
        a1 = arg1;
	Object stream = null;

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
	engine.setCurrentOutput((PrintWriter)stream);
        return cont;
    }
}
