package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.io.IOException;
import java.io.PushbackReader;
/**
   <code>peek_char/2</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_peek_char_2 extends Predicate {
    public static SymbolTerm SYM_EOF = SymbolTerm.makeSymbol("end_of_file");
    public Term arg1, arg2;

    public PRED_peek_char_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_peek_char_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "peek_char(" + arg1 + "," + arg2 + ")";
    }

    boolean inCharacter(Term t) {
	if (! t.isSymbol())
	    return false;
	if (t.equals(SYM_EOF))
	    return true;
	return ((SymbolTerm)t).name().length() == 1;
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;
	Object stream = null;

	// Char
	a2 = a2.dereference(); 
	if (! a2.isVariable() && ! inCharacter(a2))
	    throw new IllegalTypeException(this, 2, "in_character", a2);
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
	// read single character
	try {
	    int c = ((PushbackReader)stream).read();
	    if (c < 0) { // EOF
		if (! a2.unify(SYM_EOF, engine.trail))
		    return engine.fail();
		return cont;
	    } 
	    if (! Character.isDefined(c))
		throw new RepresentationException(this, 0, "character");
	    ((PushbackReader)stream).unread(c);
	    if (! a2.unify(SymbolTerm.makeSymbol(String.valueOf((char)c)), engine.trail))
		return engine.fail();
	    return cont;
	} catch (IOException e) {
	    throw new TermException(new JavaObjectTerm(e));
	}
    }
}
