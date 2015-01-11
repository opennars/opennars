package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.lang.JavaException;
import nars.jprolog.lang.PrologException;
import nars.jprolog.lang.BlockPredicate;
import nars.jprolog.lang.SystemException;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import nars.jprolog.lang.OutOfLoop;
/**
 * <code>'$begin_exception'/1</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.2
 */
class PRED_$begin_exception_1 extends BlockPredicate {
    Term arg1;

    public PRED_$begin_exception_1(Term a1, Predicate cont) {
	arg1 = a1;
	this.cont = cont;
    }

    public PRED_$begin_exception_1() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	this.cont = cont;
    }

    public int arity() { return 1; }

    public String toString() { return "$begin_exception(" + arg1 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1;
	a1 = arg1;

	if (! a1.unify(new JavaObjectTerm(this), engine.trail))
	    return engine.fail();
	Predicate code = cont;
	int B = engine.stack.top();
	this.outOfScope = false;
	this.outOfLoop  = false;
	engine.trail.push(new OutOfLoop(this));

	try {
	    main_loop:while(true) {
		while (engine.exceptionRaised == 0) {
		    if (engine.control.thread == null)
			break main_loop;
		    if (outOfLoop)
			break main_loop;
		    code = code.exec(engine);
		}
		switch (engine.exceptionRaised) {
		case 1:  // halt/0
		    break main_loop;
		case 2:  // freeze/2
		    throw new SystemException("freeze/2 is not supported yet");
		    // Do something here
                    // engine.exceptionRaised = 0 ;
		    // break
		default:
		    break main_loop;
		}
	    }
	} catch (PrologException e) {
	    if (outOfScope)
		throw e;
	    engine.setException(engine.copy(e.getMessageTerm()));
	    engine.cut(B);
	    return engine.fail();
	} catch (Exception e) {
	    if (outOfScope)
		throw new JavaException(e);
	    engine.setException(new JavaObjectTerm(e));
	    engine.cut(B);
	    return engine.fail();
	}
	return code;
    }
}
