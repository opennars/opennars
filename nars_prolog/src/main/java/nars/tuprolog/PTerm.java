/*
 * tuProlog - Copyright (C) 2001-2007  aliCE team at deis.unibo.it
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

import nars.nal.term.Term;
import nars.tuprolog.util.OneWayList;
import nars.util.data.Utf8;

import java.util.*;

/**
 * Prolog Term interface extending NARS Term interface
 * the root abstract class for prolog data type
 * 
 * @see Struct
 * @see Var
 * @see PNum
 */
public interface PTerm extends nars.nal.term.Term, SubGoalElement {


    // true and false constants
    public static final PTerm TRUE  = new Struct("true");
    public static final PTerm FALSE = new Struct("false");




    @Override
    public String toString();

    @Override
    public int hashCode();

    /*@Override
    public int compareTo(Term o) {
        return toString().compareTo(o.toString());
    }*/

    /**
     * Tests for the equality of two object terms
     *
     * The comparison follows the same semantic of
     * the isEqual method.
     *
     */
    /*@Override
    default public boolean equals(final Object t) {
        if (!(t instanceof Term))
            return false;
        return isEqual((Term) t);
    }*/
    public boolean equals(final Object t);




    
    //
    

    /** is this term a prolog (alphanumeric) atom? */
    public boolean isAtom();
    

    /** is this term a ground term? */
    public boolean isGround();




    
    
    
    /**
     * is term greater than term t?
     */
    public boolean isGreater(Term t);
    public boolean isGreaterRelink(Term t, ArrayList<String> vorder);
    
    /**
     * Tests if this term is (logically) equal to another
     */
    public boolean isEqual(Term t);
    
    /**
	 * Gets the actual term referred by this Term. if the Term is a bound variable, the method gets the Term linked to the variable
	 */
    public Term getTerm();
    
    
    /**
     * Unlink variables inside the term
     */
    public void free();
    
    
    /**
     * Resolves variables inside the term, starting from a specific time count.
     *
     * If the variables has been already resolved, no renaming is done.
     * @param count new starting time count for resolving process
     * @return the new time count, after resolving process
     */
    public long resolveTerm(long count);
    
    
    /**
     * Resolves variables inside the term
     * 
     * If the variables has been already resolved, no renaming is done.
     */
    default public PTerm resolveTerm() {
        resolveTerm(System.currentTimeMillis());
        return this;
    }

    
    /**
     * gets a engine's copy of this term.
     * @param idExecCtx Execution Context identified
     */
    default public Term copyGoal(Map<Var,Var> vars, int idExecCtx) {
        return copy(vars,idExecCtx);
    }




    /**
     * gets a copy of this term for the output
     */
    default public Term copyResult(Collection<Var> goalVars, List<Var> resultVars) {
        IdentityHashMap<Var,Var> originals = new IdentityHashMap<>();
        for (Var key: goalVars) {
            Var clone = new Var();
            if (!key.isAnonymous())
                clone = new Var(key.getOriginalName());
            originals.put(key,clone);
            resultVars.add(clone);
        }
        return copy(originals,new IdentityHashMap<>());
    }




    /**
     * gets a copy (with renamed variables) of the term.
     *
     * The list argument passed contains the list of variables to be renamed
     * (if empty list then no renaming)
     * @param idExecCtx Execution Context identifier
     */
    public PTerm copy(Map<Var, Var> vMap, int idExecCtx);
    
    /**
     * gets a copy for result.
     */
    public PTerm copy(Map<Var,Var> vMap, Map<PTerm,Var> substMap);


    default public boolean unify(final Prolog mediator, final Term t1) {
        return unify(mediator, t1, new ArrayList(), new ArrayList());
    }
    /**
     * Try to unify two terms
     * @param mediator have the reference of EngineManager
     * @param t1 the term to unify
     * @return true if the term is unifiable with this one
     */
    default public boolean unify(final Prolog engine, final Term t1, ArrayList<Var> v1, ArrayList<Var> v2) {
        resolveTerm();
        if (t1 instanceof PTerm)
            ((PTerm)t1).resolveTerm();

        v1.clear(); v2.clear();
        boolean ok = unify(v1,v2,t1);
        if (ok) {
            ExecutionContext ec = engine.getCurrentContext();
            if (ec != null) {
                Engine.State env = engine.getEnv();
                int id = (env==null)? Var.PROGRESSIVE : env.nDemoSteps;
                // Update trailingVars
                ec.trailingVars = new OneWayList<>(v1,ec.trailingVars);
                // Renaming after unify because its utility regards not the engine but the user
                int count = 0;
                for(final Var v:v1){
                    v.rename(id,count);
                    if(id>=0){
                        id++;
                    }else{
                        count++;
                    }
                }
                for(final Var v:v2){
                    v.rename(id,count);
                    if(id>=0){
                        id++;
                    }else{
                        count++;
                    }
                }
            }
            return true;
        }
        Var.free(v1);
        Var.free(v2);
    	return false;
    }

    @Override
    default public PTerm getValue() {
        return this;
    }
    
    
    /**
     * Tests if this term is unifiable with an other term.
     * No unification is done.
     *
     * The test is done outside any demonstration context
     * @param t the term to checked
     *
     * @return true if the term is unifiable with this one
     */
    default public boolean match(Term t, long time, ArrayList<Var> v1, ArrayList<Var> v2) {
        v1.clear(); v2.clear();

        resolveTerm(time);
        if (t instanceof PTerm)
            ((PTerm)t).resolveTerm(time);

        boolean ok = unify(v1,v2,t);
        Var.free(v1);
        Var.free(v2);
        return ok;
    }

    default public boolean match(Term t) {
        return match(t, System.currentTimeMillis(), new ArrayList(), new ArrayList());
    }

    /**
     * Tries to unify two terms, given a demonstration context
     * identified by the mark integer.
     *
     * Try the unification among the term and the term specified
     * @param varsUnifiedArg1 Vars unified in myself
     * @param varsUnifiedArg2 Vars unified in term t
     */
    public boolean unify(List<Var> varsUnifiedArg1, List<Var> varsUnifiedArg2, Term t);


    /**
     * Static service to create a Term from a string.
     * @param st the string representation of the term
     * @return the term represented by the string
     * @throws InvalidTermException if the string does not represent a valid term
     */
    public static PTerm createTerm(final String st) {
        return Parser.parseSingleTerm(st);
    }

    /**
     * Static service to create a Term from a string, providing an
     * external operate manager.
     * @param st the string representation of the term
     * @param op the operate manager used to build the term
     * @return the term represented by the string
     * @throws InvalidTermException if the string does not represent a valid term
     */
    public static PTerm createTerm(String st, Operators op) {
        return Parser.parseSingleTerm(st, op);
    }
    

    
    /**
     * Gets an iterator providing
     * a term stream from a source text
     */
    public static java.util.Iterator<PTerm> getIterator(String text) {
        return new Parser(text).iterator();
    }
    
    // term representation
    
    /**
     * Gets the string representation of this term
     * as an X argument of an operate, considering the associative property.
     */
    default String toStringAsArgX(Operators op,int prio) {
        return toStringAsArg(op,prio,true);
    }
    
    /**
     * Gets the string representation of this term
     * as an Y argument of an operate, considering the associative property.
     */
    default String toStringAsArgY(Operators op,int prio) {
        return toStringAsArg(op,prio,false);
    }
    
    /**
     * Gets the string representation of this term
     * as an argument of an operate, considering the associative property.
     *
     *  If the boolean argument is true, then the term must be considered
     *  as X arg, otherwise as Y arg (referring to prolog associative rules)
     */
    default String toStringAsArg(Operators op,int prio,boolean x) {
        return toString();
    }
    
    //
    
    /**
     * The iterated-goal term G of a term T is a term defined
     * recursively as follows:
     * <ul>
     * <li>if T unifies with ^(_, Goal) then G is the iterated-goal
     * term of Goal</li>
     * <li>else G is T</li>
     * </ul>
     */
    default public PTerm iteratedGoalTerm() {
        return this;
    }
    
    /*Castagna 06/2011*/
    /**
	 * Visitor pattern
	 * @param tv - Visitor
	 */
	public void accept(TermVisitor tv);
    /**/


    @Override
    default public int compareTo(nars.nal.term.Term o) {
        return Utf8.compare(name(), o.name());
    }

    @Override
    default public byte[] name() {
        //TODO cache this result
        return Utf8.toUtf8(toString());
    }
    /**/



    @Override
    default public boolean isConstant() {
        return false; //TODO
    }

    @Override
    default public boolean containsTerm(nars.nal.term.Term target) {
        return false; //TODO
    }

    @Override
    default public boolean containsTermRecursivelyOrEquals(nars.nal.term.Term target) {
        return false; //TODO
    }

    public PTerm clone();

    /** applies the default copy parameters when it is a PTerm, otherwise it just clones a Term */
    public static Term copy(Term value) {
        if (value instanceof PTerm)
            return ((PTerm)value).copy(new HashMap<>(),Var.ORIGINAL);
        return value.clone();
    }
    /** applies the default copy parameters when it is a PTerm, otherwise it just clones a Term */
    public static PTerm copyp(Term value) {
        if (value instanceof PTerm)
            return ((PTerm)value).copy(new HashMap<>(),Var.ORIGINAL);
        return null;
    }

    /** true unless it's a PTerm then it checks for isGround() */
    public static boolean isGround(Term t) {
        if (t instanceof PTerm)
            return ((PTerm)t).isGround();
        return true;
    }

    /** returns the term cast to PTerm if it is, otherwise returns null */
    static PTerm p(Term t) {
        if (t instanceof PTerm)
            return ((PTerm)t);
        return null;
    }
}