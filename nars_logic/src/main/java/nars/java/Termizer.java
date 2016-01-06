package nars.java;

import nars.$;
import nars.term.Term;
import nars.term.atom.Atom;

/**
 * Converts POJOs to NAL Term's, and vice-versa
 */
public interface Termizer {

	Atom TRUE = $.the("true");
	Term FALSE = $.neg(TRUE);
	Atom VOID = $.the("void");
	Atom EMPTY = $.the("empty");
	Atom NULL = $.the("null");

	Term term(Object o);
	Object object(Term t);

}
