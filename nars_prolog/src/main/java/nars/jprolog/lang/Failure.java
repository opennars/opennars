package nars.jprolog.lang;

import nars.jprolog.Prolog;

/**
 * Initial backtrak point.<br>
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.2
 */
public class Failure extends Predicate {
    /** Prolog thread that this <code>Failure</code> belongs to. */
    public PrologControl c;

    /** Constructs a new initial backtrak point. */
    public Failure(){}

    /** Constructs a new initial backtrak point with given Prolog thread. */
    public Failure(PrologControl c) {
	this.c = c;
    }

    public Predicate exec(Prolog engine) {
	c.fail();
	engine.exceptionRaised = 1; // halt
	return null;
    }

    /** Returns a string representation of this <code>Failure</code>. */
    public String toString(){ return "Failure"; }

    /** Returns <code>0</code>. */
    public int arity() { return 0; }
}

