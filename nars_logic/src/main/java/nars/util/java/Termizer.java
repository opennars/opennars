package nars.util.java;

import nars.nal.nal1.Negation;
import nars.term.Atom;
import nars.term.Term;

/**
 * Converts POJOs to NAL Term's, and vice-versa
 */
public interface Termizer {

    public final static Atom TRUE = Atom.the("true");
    public final static Negation FALSE = Atom.notThe("true");
    public final static Atom VOID = Atom.the("void");
    public final static Atom EMPTY = Atom.the("empty");
    public final static Atom NULL = Atom.the("null");

    public Term term(Object o);
    public Object object(Term t);

}
