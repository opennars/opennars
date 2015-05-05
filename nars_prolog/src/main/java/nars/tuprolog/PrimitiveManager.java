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

import nars.tuprolog.interfaces.IPrimitiveManager;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


/**
 * Administration of primitive predicates
 * @author Alex Benini
 */
public class PrimitiveManager /*Castagna 06/2011*/implements IPrimitiveManager/**/{
    
    private Map<IPrimitives,List<PrimitiveInfo>> libHashMap;
    private Map<String,PrimitiveInfo> directiveHashMap;
    private Map<String,PrimitiveInfo> predicateHashMap;
    private Map<String,PrimitiveInfo> functorHashMap;
    
    public PrimitiveManager() {
        libHashMap        = Collections.synchronizedMap(new IdentityHashMap<>());
        directiveHashMap  = Collections.synchronizedMap(new HashMap<>());
        predicateHashMap  = Collections.synchronizedMap(new HashMap<>());
        functorHashMap    = Collections.synchronizedMap(new HashMap<>());
    }
    
    /**
     * Config this Manager
     */
    void initialize(Prolog vm) {
        createPrimitiveInfo(new BuiltIn(vm)); 
    }
    
    void createPrimitiveInfo(IPrimitives src) {
        Map<Integer,List<PrimitiveInfo>> prims = src.getPrimitives();
        Iterator<PrimitiveInfo> it = prims.get(PrimitiveInfo.DIRECTIVE).iterator();
        while(it.hasNext()) {
            PrimitiveInfo p = it.next();
            directiveHashMap.put(p.getKey(),p);
        }
        it = prims.get(PrimitiveInfo.PREDICATE).iterator();
        while(it.hasNext()) {
            PrimitiveInfo p = it.next();
            predicateHashMap.put(p.getKey(),p);
        }
        it = prims.get(PrimitiveInfo.FUNCTOR).iterator();
        while(it.hasNext()) {
            PrimitiveInfo p = it.next();
            functorHashMap.put(p.getKey(),p);
        }
        List<PrimitiveInfo> primOfLib = new LinkedList<>(prims.get(PrimitiveInfo.DIRECTIVE));
        primOfLib.addAll(prims.get(PrimitiveInfo.PREDICATE));
        primOfLib.addAll(prims.get(PrimitiveInfo.FUNCTOR));
        libHashMap.put(src,primOfLib);
    }
    
    
    void deletePrimitiveInfo(IPrimitives src) {
        Iterator<PrimitiveInfo> it = libHashMap.remove(src).iterator();
        while(it.hasNext()) {
            String k = it.next().invalidate();
            directiveHashMap.remove(k);
            predicateHashMap.remove(k);
            functorHashMap.remove(k);
        }
    }
    
    
    /**
     * Identifies the term passed as argument.
     *
     * This involves identifying structs representing builtin
     * predicates and functors, and setting up related structures and links
     *
     * @parm term the term to be identified
     * @return term with the identified built-in directive
     */
    public Term identifyDirective(Term term) {
        identify(term,PrimitiveInfo.DIRECTIVE);
        return term;
    }
    
    public boolean evalAsDirective(Struct d) throws Throwable {
        PrimitiveInfo pd = ((Struct) identifyDirective(d)).getPrimitive();
        if (pd != null) {
            try {
                pd.evalAsDirective(d);
                return true;
            } catch (InvocationTargetException ite) {
                throw ite.getTargetException();
            }
        } else
            return false;
    }
    
    public void identifyPredicate(Term term) {
        identify(term,PrimitiveInfo.PREDICATE);
    }
    
    public void identifyFunctor(Term term) {
        identify(term,PrimitiveInfo.FUNCTOR);
    }
    
    private void identify(Term term, int typeOfPrimitive) {
        if (term == null) {
            return;
        }
        term = term.getTerm();
        if (!(term instanceof Struct)) {
            return;
        }
        Struct t = (Struct) term;
        
        int arity = t.getArity();
        String name = t.getName();
        //------------------------------------------
        if (name.equals(",") || name.equals("':-'") || name.equals(":-")) {
            for (int c = 0; c < arity; c++) {
                identify( t.getArg(c), PrimitiveInfo.PREDICATE);
            }
        } else {
            for (int c = 0; c < arity; c++) {
                identify( t.getArg(c), PrimitiveInfo.FUNCTOR);
            }                        
        }
        //------------------------------------------
        //log.debug("Identification "+t);    
        PrimitiveInfo prim = null;
        String key = name + '/' + arity;
        
        switch (typeOfPrimitive) {
        case PrimitiveInfo.DIRECTIVE :
            prim = directiveHashMap.get(key);
            //log.debug("Assign predicate "+prim+" to "+t);
            break;
        case PrimitiveInfo.PREDICATE :
            prim = predicateHashMap.get(key);
            //log.debug("Assign predicate "+prim+" to "+t);
            break;
        case PrimitiveInfo.FUNCTOR :
            prim = functorHashMap.get(key);
            //log.debug("Assign functor "+prim+" to "+t);
            break;
        }
        t.setPrimitive(prim);
    }
    
    
    Library getLibraryDirective(String name, int nArgs) {
        try {
            return (Library) directiveHashMap.get(name + '/' + nArgs).getSource();
        } catch(NullPointerException e) {
            return null;
        }
    }
    
    Library getLibraryPredicate(String name, int nArgs) {
        try {
            return (Library) predicateHashMap.get(name + '/' + nArgs).getSource();
        } catch(NullPointerException e) {
            return null;
        }
    }
    
    Library getLibraryFunctor(String name, int nArgs) {
        try {
            return (Library) functorHashMap.get(name + '/' + nArgs).getSource();
        } catch(NullPointerException e) {
            return null;
        }
    }
    
    /*Castagna 06/2011*/
    public boolean containsTerm(String name, int nArgs) {
		return (functorHashMap.containsKey(name + '/' + nArgs) || predicateHashMap.containsKey(name + '/' + nArgs));
	}
    /**/
}