package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.lang.IntegerTerm;
import nars.jprolog.lang.SymbolTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import nars.jprolog.lang.Token;
/**
 * <code>'$atom_type0'/2</code><br>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.0
 */
class PRED_$atom_type0_2 extends Predicate {
    Term arg1, arg2;

    public PRED_$atom_type0_2(Term a1, Term a2, Predicate cont){ 
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }

    public PRED_$atom_type0_2(){}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() { return "$atom_type0(" + arg1 + ", " + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
	int type;
	a1 = arg1;
	a2 = arg2;

	a1 = a1.dereference();
	if(! a1.isSymbol())
	    return engine.fail();
	type = Token.getStringType(((SymbolTerm)a1).name());
	if(! a2.unify(new IntegerTerm(type), engine.trail)) 
	    return engine.fail();
	return cont;
    }
}
