/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package nars.tuprolog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 * Primitive class
 * referring to a builtin predicate or functor
 *
 * @see Struct
 */
public class PrimitiveInfo {
    
    public final static int DIRECTIVE  = 0;
    public final static int PREDICATE  = 1;
    public final static int FUNCTOR    = 2;
    
    private int type;
    /**
	 * method to be call when evaluating the built-in
	 */
    final private Method method;
    /**
	 * lib object where the builtin is defined
	 */
    final private IPrimitives source;
    /**
	 * for optimization purposes
	 */
    final private Object[] primitive_args;
    private String primitive_key;
    
    
    public PrimitiveInfo(int type, String key, Library lib, Method m, int arity) throws NoSuchMethodException {
        if (m==null) {
            throw new NoSuchMethodException();
        }
        this.type = type;
        primitive_key = key;
        source = lib;
        method = m;
        primitive_args=new PTerm[arity];
    }
    
    
    /**
     * Method to invalidate primitives. It's called just mother library removed
     */
    public synchronized String invalidate() {
        String key = primitive_key;
        primitive_key = null;
        return key;
    }
    
    
    public String getKey() {
        return primitive_key;
    }
    
    public boolean isDirective() {
        return (type == DIRECTIVE);
    }
    
    public boolean isFunctor() {
        return (type == FUNCTOR);
    }
    
    public boolean isPredicate() {
        return (type == PREDICATE);
    }
    
    
    public int getType() {
        return type;
    }
    
    public IPrimitives getSource() {
        return source;
    }
    
    
    /**
     * evaluates the primitive as a directive
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws Exception if invocation directive failure
     */
    public synchronized void evalAsDirective(Struct g) throws IllegalAccessException, InvocationTargetException {
        for (int i=0; i<primitive_args.length; i++) {
            primitive_args[i] = g.getTerm(i);
        }
        method.invoke(source,primitive_args);
    }
    
    
    /**
     * evaluates the primitive as a predicate
     * @throws Exception if invocation primitive failure
     */
    public synchronized boolean evalAsPredicate(Struct g) throws Throwable {
        for (int i=0; i<primitive_args.length; i++) {
            primitive_args[i] = g.getTermX(i);
        }
        try {
        	//System.out.println("PRIMITIVE INFO evalAsPredicate sto invocando metodo "+method.getName());
            return (Boolean) method.invoke(source, primitive_args);
        } catch (Exception e) {
            throw e.getCause();
        }
    }
    
    
    /**
     * evaluates the primitive as a functor
     * @throws Throwable 
     */
    public synchronized PTerm evalAsFunctor(Struct g) throws Throwable {
        try {
            for (int i=0; i<primitive_args.length; i++) {
                primitive_args[i] = g.getTerm(i);
            }
            return ((PTerm)method.invoke(source,primitive_args));
        } catch (Exception ex) {
            throw ex.getCause();
        }
    }
    
    
    
    public String toString() {
        return "[ primitive: method "+method.getName()+" - "+ Arrays.toString(primitive_args) +" - N args: "+primitive_args.length+" - "+source.getClass().getName()+" ]\n";
    }
    
}