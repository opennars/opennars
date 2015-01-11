package nars.jprolog.lang;

import nars.jprolog.Prolog;

/**
 * Initial continuation goal.<br>
 * That is to say, this <code>Success</code> will be executed 
 * every time the Prolog Cafe system finds an answer.
 *
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
public class Success extends Predicate {
    /** Prolog thread that this <code>Success</code> belongs to. */
    public PrologControl c;

    /** Constructs a new initial continuation goal. */
    public Success(){}

    /** Constructs a new initial continuation goal with given Prolog thread. */
    public Success(PrologControl c) {
	this.c = c;
    }

    /**
     * Backtracks and returns a next clause
     * after invoking the <code>PrologControl.success()</code>.
     * @param engine Prolog engine
     * @see PrologControl#success
     */
    public Predicate exec(Prolog engine) {
	c.success();
	return engine.fail();
    }

    /** Returns a string representation of this <code>Success</code>. */
    public String toString(){ return "Success"; }

    /** Returns <code>0</code>. */
    public int arity() { return 0; }
}

