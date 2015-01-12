package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.util.Vector;
/**
 * <code>java_conversion/2</code>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PRED_java_conversion_2 extends Predicate {
    Term arg1, arg2;

    public PRED_java_conversion_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }
    public PRED_java_conversion_2() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() { return "java_conversion(" + arg1 + "," + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
	a1 = arg1;
	a2 = arg2;

	a1 = a1.dereference();
	a2 = a2.dereference();
	if (a1.isVariable()) { // a1 = var
	    if (a2.isJavaObject()) { // a1 = var /\ a2 = java
		((VariableTerm)a1).bind(inverseConversion(((JavaObjectTerm)a2).object()), engine.trail);
	    } else { // a1 = var /\ a2 = nonjava
		((VariableTerm)a1).bind(a2, engine.trail);
	    }
	} else if (! a2.isVariable()) { // a1 = nonvar /\ a2 = nonvar
	    throw new IllegalTypeException(this, 2, "variable", a2);
	} else { // a1 = nonvar /\ a2 = var
	    // (a1 = java \/  a1 = str \/ a1 = clo) /\ a2 = var
	    if (a1.isJavaObject() || a1.isStructure() || a1.isClosure()) {
		((VariableTerm)a2).bind(a1, engine.trail);
	    } else { // a1 != java /\ a1 != str /\ a1 != clo /\ a2 = var
		((VariableTerm)a2).bind(new JavaObjectTerm(a1.toJava()), engine.trail);
	    }
	}
	return cont;
    }

    protected Term inverseConversion(Object o) {
	if (o == null) {
	    throw new EvaluationException(this, 2, "undefined");
	} else if (o instanceof Byte    || 
		   o instanceof Short   ||
		   o instanceof Integer ||
		   o instanceof Long) {
	    return new IntegerTerm(((Number)o).intValue());
	} else if (o instanceof Float || 
		   o instanceof Double) {
	    return new DoubleTerm(((Number)o).doubleValue());
	} else if (o instanceof String) {
	    return SymbolTerm.makeSymbol((String) o);
	} else if (o instanceof Vector) {
	    Vector v = (Vector) o;
	    Term t = Prolog.Nil;
	    for(int i= v.size(); i>0; i--) {
		t = new ListTerm(inverseConversion(v.elementAt(i-1)), t);
	    }
	    return t;
	}
	return new JavaObjectTerm(o);
    }
}
