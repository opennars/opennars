package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
/**
 * <code>close/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
*/
public class PRED_close_2 extends Predicate {
    public static SymbolTerm SYM_ALIAS_1 = SymbolTerm.makeSymbol("alias", 1);
    public static SymbolTerm SYM_FORCE_1 = SymbolTerm.makeSymbol("force", 1);
    public static SymbolTerm SYM_TRUE    = SymbolTerm.makeSymbol("true");
    public static SymbolTerm SYM_FALSE   = SymbolTerm.makeSymbol("false");
    public Term arg1, arg2;

    public PRED_close_2(Term a1, Term a2, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        this.cont = cont;
    }

    public PRED_close_2(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() {
        return "close(" + arg1 + ',' + arg2 + ')';
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
        Term a1, a2;
        a1 = arg1;
        a2 = arg2;

	boolean forceFlag = false;
	Object stream = null;

	// close options
	a2 = a2.dereference();
	Term tmp = a2;
	while (! tmp.isNil()) {
	    if (tmp.isVariable())
		throw new PInstantiationException(this, 2);
	    if (! tmp.isList())
		throw new IllegalTypeException(this, 2, "list", a2);
	    Term car = ((ListTerm) tmp).car().dereference();
	    if (car.isVariable())
		throw new PInstantiationException(this, 2);
	    if (car.isStructure()) {
		SymbolTerm functor = ((StructureTerm) car).functor();
		Term[] args = ((StructureTerm) car).args();
		if (functor.equals(SYM_FORCE_1)) {
		    Term bool = args[0].dereference();
		    if (bool.equals(SYM_TRUE))
			forceFlag = true;
		    else if (bool.equals(SYM_FALSE))
			forceFlag = false;
		    else 
			throw new IllegalDomainException(this, 2, "close_option", car);
		} else {
		    throw new IllegalDomainException(this, 2, "close_option", car);
		}
	    } else {
		throw new IllegalDomainException(this, 2, "close_option", car);
	    }
	    tmp = ((ListTerm) tmp).cdr().dereference();
	}
	//stream
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
	if (stream instanceof PushbackReader) {
	    PushbackReader in = (PushbackReader) stream;
	    if (in.equals(engine.getUserInput()))
		return cont;
	    if (in.equals(engine.getCurrentInput()))
		engine.setCurrentInput(engine.getUserInput());
	    try {
		in.close();
	    } catch (IOException e) {
		throw new TermException(new JavaObjectTerm(e));
	    }
	} else if (stream instanceof PrintWriter) {
	    PrintWriter out = (PrintWriter) stream;
	    if (out.checkError()) {
		if (! forceFlag)
		    throw new SystemException("output stream error");
	    }
	    out.flush();
	    if (out.equals(engine.getUserOutput()) || out.equals(engine.getUserError()))
		return cont;
	    if (out.equals(engine.getCurrentOutput()))
		engine.setCurrentOutput(engine.getUserOutput());
	    out.close();
	} else {
	    throw new IllegalDomainException(this, 1, "stream_or_alias", a1);
	}
	// delete associated entries from the stream manager
	HashtableOfTerm streamManager = engine.getStreamManager();
	if (a1.isSymbol()) {
	    streamManager.remove(engine.getStreamManager().get(a1));
	    streamManager.remove(a1);
	} else if (a1.isJavaObject()) {
	    Term tmp2 = streamManager.get(a1);
	    while (! tmp2.isNil()) {
		Term car = ((ListTerm) tmp2).car().dereference();
		if (car.isStructure()) {
		    SymbolTerm functor = ((StructureTerm) car).functor();
		    Term[] args = ((StructureTerm) car).args();
		    if (functor.equals(SYM_ALIAS_1)) {
			Term alias = args[0].dereference();
			streamManager.remove(alias);
		    }
		}
		tmp2 = ((ListTerm) tmp2).cdr().dereference();
	    }
	    streamManager.remove(a1);
	} else {
	    throw new IllegalDomainException(this, 1, "stream_or_alias", a1);
	}
        return cont;
    }
}
