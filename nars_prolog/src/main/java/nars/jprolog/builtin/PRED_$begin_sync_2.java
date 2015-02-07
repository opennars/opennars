package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;
/**
 * <code>'$begin_sync'/2</code>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.2
 */
class PRED_$begin_sync_2 extends BlockPredicate {
    Term arg1, arg2;

    public PRED_$begin_sync_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }
    public PRED_$begin_sync_2() {}
    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() { return "$begin_sync(" + arg1 + ',' + arg2 + ')'; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
	a1 = arg1;
	a2 = arg2;

	Object o = null;
	Predicate code = null;

	// 1st. argument
	a1 = a1.dereference();
	if (a1.isVariable())
	    throw new PInstantiationException(this, 1);
	if (! a1.isJavaObject())
	    throw new IllegalTypeException(this, 1, "java", a1);
	o = ((JavaObjectTerm)a1).object();
	// 2nd. argument
	a2 = a2.dereference();
	if (! a2.isVariable())
	    throw new IllegalTypeException(this, 2, "variable", a1);
	((VariableTerm) a2).bind(new JavaObjectTerm(this), engine.trail);
	//
	code = cont;
	this.outOfScope = false;
	this.outOfLoop  = false;
	engine.trail.push(new OutOfLoop(this));
	main_loop:while(true) {
	    synchronized (o) {
		while (! outOfScope) {
		    if (engine.exceptionRaised != 0) {
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
		    if (engine.control.thread == null)
			break main_loop;
		    if (outOfLoop)
			break main_loop;
		    code = code.exec(engine);
		}

	    }
	    while (outOfScope) {
		if (engine.exceptionRaised != 0) {
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
		if (engine.control.thread == null)
		    break main_loop;
		if (outOfLoop)
		    break main_loop;
		code = code.exec(engine);
	    }

	}
	return code;
    }
}
