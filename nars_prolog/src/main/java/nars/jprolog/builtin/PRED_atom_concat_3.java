//package nars.jprolog.builtin;
//import nars.jprolog.lang.Predicate;
//import nars.jprolog.lang.SymbolTerm;
//import nars.jprolog.lang.PInstantiationException;
//import nars.jprolog.lang.IllegalTypeException;
//import nars.jprolog.lang.ListTerm;
//import nars.jprolog.lang.StructureTerm;
//import nars.jprolog.Prolog;
//import nars.jprolog.lang.Term;
///**
// * <code>atom_concat/3</code><br>
// * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
// * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
// * @version 1.0
//*/
//public class PRED_atom_concat_3 extends Predicate {
//    static SymbolTerm AC_2 = SymbolTerm.makeSymbol("ac", 2);
//    public Term arg1, arg2, arg3;
//
//    public PRED_atom_concat_3(Term a1, Term a2, Term a3, Predicate cont) {
//        arg1 = a1;
//        arg2 = a2;
//        arg3 = a3;
//        this.cont = cont;
//    }
//
//    public PRED_atom_concat_3(){}
//
//    public void setArgument(Term[] args, Predicate cont) {
//        arg1 = args[0];
//        arg2 = args[1];
//        arg3 = args[2];
//        this.cont = cont;
//    }
//
//    public int arity() { return 3; }
//
//    public String toString() {
//        return "atom_concat(" + arg1 + "," + arg2 + "," + arg3 + ")";
//    }
//
//    public Predicate exec(Prolog engine) {
//        engine.setB0();
//        Term a1, a2, a3;
//        a1 = arg1;
//        a2 = arg2;
//        a3 = arg3;
//
//	a3 = a3.dereference();
//	if (a3.isSymbol()) {
//	    String str3 = ((SymbolTerm)a3).name();
//	    int endIndex = str3.length();
//	    Term t = Prolog.Nil;
//	    for (int i=0; i<=endIndex; i++) {
//		Term[] args = {SymbolTerm.makeSymbol(str3.substring(0, i)),
//			       SymbolTerm.makeSymbol(str3.substring(i, endIndex))};
//		t = new ListTerm(new StructureTerm(AC_2, args), t);
//	    }
//	    Term[] args12 = {a1,a2};
//	    return new PRED_member_in_reverse_2(new StructureTerm(AC_2, args12), t, cont);
//	} else if (! a3.isVariable()) {
//	    throw new IllegalTypeException(this, 3, "atom", a3);
//	}
//	// a3 is a variable
//	a1 = a1.dereference();
//	a2 = a2.dereference();
//	if (a1.isVariable())
//	    throw new PInstantiationException(this, 1);
//	if (a2.isVariable())
//	    throw new PInstantiationException(this, 2);
//	if (! a1.isSymbol())
//	    throw new IllegalTypeException(this, 1, "integer", a1);
//	if (! a2.isSymbol())
//	    throw new IllegalTypeException(this, 2, "integer", a2);
//	String str3 = ((SymbolTerm) a1).name().concat(((SymbolTerm) a2).name());
//	if (! a3.unify(SymbolTerm.makeSymbol(str3), engine.trail))
//	    return engine.fail();
//        return cont;
//    }
//}
