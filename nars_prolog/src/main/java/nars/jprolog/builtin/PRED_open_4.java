package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.io.*;
/**
   <code>open/4</code><br>
   @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
   @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
   @version 1.0
*/
public class PRED_open_4 extends Predicate {
    public static SymbolTerm SYM_NIL         = SymbolTerm.makeSymbol("[]");
    public static SymbolTerm SYM_TEXT        = SymbolTerm.makeSymbol("text");
    //    public static SymbolTerm SYM_BINARY      = SymbolTerm.makeSymbol("binary");
    public static SymbolTerm SYM_READ        = SymbolTerm.makeSymbol("read");
    public static SymbolTerm SYM_WRITE       = SymbolTerm.makeSymbol("write");
    public static SymbolTerm SYM_APPEND      = SymbolTerm.makeSymbol("append");
    public static SymbolTerm SYM_INPUT       = SymbolTerm.makeSymbol("input");
    public static SymbolTerm SYM_OUTPUT      = SymbolTerm.makeSymbol("output");
    public static SymbolTerm SYM_ALIAS_1     = SymbolTerm.makeSymbol("alias", 1);
    public static SymbolTerm SYM_MODE_1      = SymbolTerm.makeSymbol("mode", 1);
    public static SymbolTerm SYM_TYPE_1      = SymbolTerm.makeSymbol("type", 1);
    public static SymbolTerm SYM_FILE_NAME_1 = SymbolTerm.makeSymbol("file_name", 1);

    public Term arg1, arg2, arg3, arg4;

    public PRED_open_4(Term a1, Term a2, Term a3, Term a4, Predicate cont) {
        arg1 = a1;
        arg2 = a2;
        arg3 = a3;
        arg4 = a4;
        this.cont = cont;
    }

    public PRED_open_4(){}

    public void setArgument(Term[] args, Predicate cont) {
        arg1 = args[0];
        arg2 = args[1];
        arg3 = args[2];
        arg4 = args[3];
        this.cont = cont;
    }

    public int arity() { return 4; }

    public String toString() {
        return "open(" + arg1 + ',' + arg2 + ',' + arg3 + ',' + arg4 + ')';
    }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	File file;
	Term alias = null;
	Term opts = SYM_NIL;
	JavaObjectTerm streamObject;
        Term a1, a2, a3, a4;
        a1 = arg1;
        a2 = arg2;
        a3 = arg3;
        a4 = arg4;

	// stream
	a3 = a3.dereference();
	if (! a3.isVariable())
	    throw new IllegalTypeException(this, 3, "variable", a3);
	// source_sink
	a1 = a1.dereference();
	if (a1.isVariable())
	    throw new PInstantiationException(this, 1);
	if (! a1.isSymbol())
	    throw new IllegalDomainException(this, 1, "source_sink", a1);
	file = new File(((SymbolTerm) a1).name());
	// io_mode
	a2 = a2.dereference();
	if (a2.isVariable())
	    throw new PInstantiationException(this, 2);
	if (! a2.isSymbol()) 
	    throw new IllegalTypeException(this, 2, "atom", a2);
	try {
	    if (a2.equals(SYM_READ)) {
		if (! file.exists())
		    throw new ExistenceException(this, 1, "source_sink", a1, "");
		PushbackReader in = 
		    new PushbackReader(new BufferedReader(new FileReader(file)), Prolog.PUSHBACK_SIZE);
		streamObject = new JavaObjectTerm(in);
		opts = new ListTerm(SYM_INPUT, opts);
	    } else if (a2.equals(SYM_WRITE)) {
		PrintWriter out = 
		    new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
		streamObject = new JavaObjectTerm(out);
		opts = new ListTerm(SYM_OUTPUT, opts);
	    } else if (a2.equals(SYM_APPEND)) {
		PrintWriter out = 
		    new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
		streamObject = new JavaObjectTerm(out);
		opts = new ListTerm(SYM_OUTPUT, opts);
	    } else {
		throw new IllegalDomainException(this, 2, "io_mode", a2);
	    }
	} catch (IOException e) {
	    throw new PermissionException(this, "open", "source_sink", a1, "");
	}
	if (engine.getStreamManager().containsKey(streamObject))
	    throw new InternalException("stream object is duplicated");
	// stream_options
	a4 = a4.dereference();
	Term tmp = a4;
	while (! tmp.isNil()) {
	    if (tmp.isVariable())
		throw new PInstantiationException(this, 4);
	    if (! tmp.isList())
		throw new IllegalTypeException(this, 4, "list", a4);
	    Term car = ((ListTerm) tmp).car().dereference();
	    if (car.isVariable())
		throw new PInstantiationException(this, 4);
	    if (car.isStructure()) {
		SymbolTerm functor = ((StructureTerm) car).functor();
		Term[] args = ((StructureTerm) car).args();
		if (functor.equals(SYM_ALIAS_1)) {
		    alias = args[0].dereference();
		    if (! alias.isSymbol())
			throw new IllegalDomainException(this, 4, "stream_option", car);
		    if (engine.getStreamManager().containsKey(alias))
			throw new PermissionException(this, "open", "source_sink", car, "");
		} else {
		    throw new IllegalDomainException(this, 4, "stream_option", car);
		}
	    } else {
		throw new IllegalDomainException(this, 4, "stream_option", car);
	    }
	    tmp = ((ListTerm) tmp).cdr().dereference();
	}
	Term[] args1 = {SYM_TEXT};
	Term[] args2 = {a2};
	Term[] args3 = {SymbolTerm.makeSymbol(file.getAbsolutePath())};
	opts = new ListTerm(new StructureTerm(SYM_TYPE_1, args1), opts);
	opts = new ListTerm(new StructureTerm(SYM_MODE_1, args2), opts);
	opts = new ListTerm(new StructureTerm(SYM_FILE_NAME_1, args3), opts);
	if (alias != null) {
	    engine.getStreamManager().put(alias, streamObject);
	    Term[] as = {alias};
	    opts = new ListTerm(new StructureTerm(SYM_ALIAS_1, as), opts);
	}
	((VariableTerm)a3).bind(streamObject, engine.trail);
	engine.getStreamManager().put(streamObject, opts);
        return cont;
    }
}



