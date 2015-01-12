package nars.jprolog.builtin;

import nars.jprolog.Prolog;
import nars.jprolog.lang.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * <code>java_method0/3</code>
 * @author Mutsunori Banbara (banbara@kobe-u.ac.jp)
 * @author Naoyuki Tamura (tamura@kobe-u.ac.jp)
 * @version 1.1
 */
public class PRED_java_method0_3 extends JavaPredicate {
    Term arg1, arg2, arg3;

    public PRED_java_method0_3() {}
    public PRED_java_method0_3(Term a1, Term a2, Term a3, Predicate cont) {
	arg1 = a1;
	arg2 = a2;
	arg3 = a3;
	this.cont = cont;
    }

    public void setArgument(Term[] args, Predicate cont){
	arg1 = args[0];
	arg2 = args[1];
	arg3 = args[2];
	this.cont = cont;
    }

    public int arity() { return 3; }

    public String toString() { return "java_method0(" + arg1 + "," + arg2 + "," + arg3 + ")"; }

    public Predicate exec(Prolog engine) {
        engine.setB0();
	Term a1, a2, a3;
	a1 = arg1;
	a2 = arg2;
	a3 = arg3;

	Class clazz = null;
	Object instance = null;
	Method[] methods = null;
	Method m = null;
	Object value = null;
	int arity;
	Term[] pArgs = null;
	Object[] jArgs = null;
	String methodName = null;

	// 3rd. argument (unbound variable)
	a3 = a3.dereference();
	if (! a3.isVariable())
	    throw new IllegalTypeException(this, 3, "variable", a3);
	try {
	    // 1st. argument (atom or java term)
	    a1 = a1.dereference();
	    if (a1.isVariable()) {
		throw new PInstantiationException(this, 1);
	    } else if (a1.isSymbol()){      // class
		clazz = Class.forName(((SymbolTerm)a1).name());
	    } else if (a1.isJavaObject()) { // instance
		instance = ((JavaObjectTerm)a1).object();
		clazz = ((JavaObjectTerm)a1).getClazz();
	    } else {
		throw new IllegalTypeException(this, 1, "atom_or_java", a1);
	    }
	    // 2nd. argument (atom or callable term)
	    a2 = a2.dereference();
	    if (a2.isVariable()) {
		throw new PInstantiationException(this, 2);
	    } else if (a2.isSymbol()) {    // No argument method
		m = clazz.getMethod(((SymbolTerm)a2).name());
		//m.setAccessible(true);
		value = m.invoke(instance);
	    } else if (a2.isStructure()) { // Parameterized method
		methodName = ((StructureTerm)a2).name();
		arity      = ((StructureTerm)a2).arity();
		methods = clazz.getMethods();
		if (methods.length == 0)
		    throw new ExistenceException(this, 2, "method", a2, "");
		pArgs = ((StructureTerm)a2).args();
		jArgs = new Object[arity];
		for (int i=0; i<arity; i++) {
		    pArgs[i] = pArgs[i].dereference();
		    if (! pArgs[i].isJavaObject())
			pArgs[i] = new JavaObjectTerm(pArgs[i]);
		    jArgs[i] = pArgs[i].toJava();
		}
		for (int i=0; i<methods.length; i++) {
		    if (methods[i].getName().equals(methodName) 
			&&  checkParameterTypes(methods[i].getParameterTypes(), pArgs)) {
			try {
			    m = methods[i];
			    //m.setAccessible(true);
			    value = m.invoke(instance, jArgs); 
			    break;     // Succeeds to invoke the method
			} catch (Exception e) {
			    m = null;  // Back to loop
			}
		    }
		}
		if (m == null)
		    throw new ExistenceException(this, 2, "method", a2, "");
	    } else {
		throw new IllegalTypeException(this, 2, "callable", a2);
	    }
	    if (value == null)
		return cont;
	    if (! a3.unify(toPrologTerm(value), engine.trail))
		return engine.fail();
	    return cont; 
	} catch (ClassNotFoundException e) {    // Class.forName
	    throw new JavaException(this, 1, e);
	} catch (NoSuchMethodException e) {     // Class.getDeclaredMethod
	    throw new JavaException(this, 2, e);
	} catch (SecurityException e) {         // Class.getDeclaredMethods
	    throw new JavaException(this, 2, e);
	} catch (IllegalAccessException e) {    // Method.invoke
	    throw new JavaException(this, 2, e);
	} catch (IllegalArgumentException e) {  // Method.invoke
	    throw new JavaException(this, 2, e);
	} catch (InvocationTargetException e) { // Method.invoke
	    throw new JavaException(this, 2, e);
	} catch (NullPointerException e) {      // Method.invoke
	    throw new JavaException(this, 2, e);
	}
    }

    private Term toPrologTerm(Object obj) {
	if (Term.instanceOfTerm(obj))
	    return (Term)obj;
	else 
	    return new JavaObjectTerm(obj);
    }
}
