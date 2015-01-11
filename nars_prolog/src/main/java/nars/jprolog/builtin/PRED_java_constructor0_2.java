package nars.jprolog.builtin;
import  nars.jprolog.lang.Predicate;
import nars.jprolog.lang.JavaException;
import nars.jprolog.lang.SymbolTerm;
import nars.jprolog.lang.PInstantiationException;
import nars.jprolog.lang.IllegalTypeException;
import nars.jprolog.lang.ExistenceException;
import nars.jprolog.lang.StructureTerm;
import nars.jprolog.lang.JavaPredicate;
import nars.jprolog.lang.JavaObjectTerm;
import nars.jprolog.Prolog;
import nars.jprolog.lang.Term;
import java.lang.reflect.*;
/**
 * <code>java_constructor0/2</code>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PRED_java_constructor0_2 extends JavaPredicate {
    Term arg1, arg2;

    public PRED_java_constructor0_2(Term a1, Term a2, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	this.cont = cont;
    }
    public PRED_java_constructor0_2() {}

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	this.cont = cont;
    }

    public int arity() { return 2; }

    public String toString() { return "java_constructor0(" + arg1 + "," + arg2 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2;
	a1 = arg1;
	a2 = arg2;

	Class clazz = null;
	Object instance = null;
	int arity;
	Constructor[] constrs = null;
	Term[] pArgs = null;
	Object[] jArgs = null;
	Constructor c = null;

	try {
	    // 2nd. argument (unbound variable)
	    a2 = a2.dereference();
	    if (! a2.isVariable())
		throw new IllegalTypeException(this, 2, "variable", a2);
	    // 1st. argument (atom or callable term)
	    a1 = a1.dereference();
	    if (a1.isVariable())
		throw new PInstantiationException(this, 1);
	    if (!a1.isSymbol() && !a1.isStructure())
		throw new IllegalTypeException(this, 1, "callable", a1);
	    if (a1.isSymbol()) { // No argument constructor
		clazz = Class.forName(((SymbolTerm)a1).name());
		instance = clazz.newInstance();
		if (! a2.unify(toPrologTerm(instance), engine.trail))
		    return engine.fail();
		return cont;
	    } 
	    // Parameterized constructor
	    clazz = Class.forName(((StructureTerm)a1).name());
	    arity  = ((StructureTerm)a1).arity();
	    constrs = clazz.getConstructors();
	    if (constrs.length == 0)
		throw new ExistenceException(this, 1, "constructor", a1, "");
	    pArgs  = ((StructureTerm)a1).args();
	    jArgs  = new Object[arity];
	    for (int i=0; i<arity; i++) {
		pArgs[i] = pArgs[i].dereference();
		if (! pArgs[i].isJavaObject())
		    pArgs[i] = new JavaObjectTerm(pArgs[i]);
		jArgs[i] = pArgs[i].toJava();
	    }
	    for (int i=0; i<constrs.length; i++) {
		if (checkParameterTypes(constrs[i].getParameterTypes(), pArgs)) {
		    try {
			c = constrs[i]; 
			//c.setAccessible(true);
			instance = c.newInstance(jArgs);
			break;    // Succeeds to create new instance
		    } catch (Exception e) {
			c = null; // Back to loop
		    }
		}
	    }
	    if (c == null)
		throw new ExistenceException(this, 1, "constructor", a1, "");
	    if (! a2.unify(toPrologTerm(instance), engine.trail)) 
		return engine.fail();
	    return cont;
	} catch (ClassNotFoundException e) {    // Class.forName(..)
	    throw new JavaException(this, 1, e);
	} catch (InstantiationException e) {    // Class.forName(..) or Constructor.newInstance()
	    throw new JavaException(this, 1, e);
	} catch (IllegalAccessException e) {    // Class.forName(..) or Constructor.newInstance()
	    throw new JavaException(this, 1, e);
	} catch (SecurityException e) {         // Class.getConstructors()
	    throw new JavaException(this, 1, e);
	} catch (IllegalArgumentException e) {  // Constructor.newInstance()
	    throw new JavaException(this, 1, e);
	}
    }

    private Term toPrologTerm(Object obj) {
	if (Term.instanceOfTerm(obj))
	    return (Term)obj;
	else 
	    return new JavaObjectTerm(obj);
    }
}
