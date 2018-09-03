/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.language;

import org.opennars.inference.TemporalRules;
import org.opennars.io.Symbols;
import org.opennars.storage.Memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Static utility class for static methods related to Variables
 *
 * @author Patrick Hammer
 */
public class Variables {
    
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term> map1, final Map<Term, Term> map2) {
        return findSubstitute(type, term1, term2, new Map[] { map1, map2 });
    }
    
    public static boolean allowUnification(final char type, final char uniType)
    { //it is valid to allow dependent var unification in case that a independent var unification is happening,
        //as shown in the 
        // <(&&,<$1 --> [ENGLISH]>, <$2 --> [CHINESE]>, <(*, $1, #3) --> REPRESENT>, <(*, $2, #3) --> REPRESENT>) ==> <(*, $1, $2) --> TRANSLATE>>.
        //example by Kai Liu.
        //1.7.0 and 2.0.1 also already allowed this, so this is for v1.6.x now.
        
        if(uniType == type) { //the usual case
            return true;
        }
        if(uniType == Symbols.VAR_INDEPENDENT) { //the now allowed case
            if(type == Symbols.VAR_DEPENDENT ||
               type == Symbols.VAR_QUERY) {
                return true;
            }
        }
        if(uniType == Symbols.VAR_DEPENDENT) { //the now allowed case
            return type == Symbols.VAR_QUERY;
        }
        return false;
    }
    
    /** map is a 2-element array of Map&lt;Term,Term&gt;. it may be null, in which case
     * the maps will be instantiated as necessary.  
     * this is to delay the instantiation of the 2 Map until necessary to avoid
     * wasting them if they are not used.
     */
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map) {
        return findSubstitute(type, term1, term2, map, false);
    }
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map, final boolean allowPartial) {

        boolean term1HasVar = term1.hasVar(type);
        if(type == Symbols.VAR_INDEPENDENT) {
            term1HasVar |= term1.hasVarDep();
            term1HasVar |= term1.hasVarQuery();
        }
        if(type == Symbols.VAR_DEPENDENT) {
            term1HasVar |= term1.hasVarQuery();
        }
        final boolean term2HasVar = term2.hasVar(type);
        
        
        final boolean term1Var = term1 instanceof Variable;
        final boolean term2Var = term2 instanceof Variable;
        
        if(allowPartial && term1 instanceof Conjunction && term2 instanceof Conjunction) {
            final Conjunction c1 = (Conjunction) term1;
            final Conjunction c2 = (Conjunction) term2;
            //more effective matching for NLP
            if(c1.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                    c2.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                final int size_smaller = c1.size();
                if(c1.size() < c2.size()) {
                    //find an offset that works
                    for(int k=0;k<(c2.term.length - c1.term.length);k++) {
                        final Map<Term, Term>[] mapk = (Map<Term, Term>[]) new HashMap<?,?>[2];
                        mapk[0] = new HashMap<>();
                        mapk[1] = new HashMap<>();
                        if(map[0] == null) {
                            map[0] = new HashMap<>();
                        }
                        if(map[1] == null) {
                            map[1] = new HashMap<>();
                        }
                        appendToMap(map[0], mapk[0]);
                        appendToMap(map[1], mapk[1]);
                        boolean succeeded = true;
                        for(int j=k;j<k+size_smaller;j++) {
                            final int i = j-k;
                            final Map<Term, Term>[] mapNew = (Map<Term, Term>[]) new HashMap<?,?>[2];
                            mapNew[0] = new HashMap<>();
                            mapNew[1] = new HashMap<>();
                            appendToMap(map[0], mapNew[0]);
                            appendToMap(map[1], mapNew[1]);
                            //attempt unification:
                            if(findSubstitute(type,c1.term[i],c2.term[j],mapNew)) {
                                appendToMap(mapNew[0], mapk[0]);
                                appendToMap(mapNew[1], mapk[1]);
                            } else { //another shift k is needed
                                succeeded = false;
                                break;
                            }
                        }
                        if(succeeded) {
                            appendToMap(mapk[0], map[0]);
                            appendToMap(mapk[1], map[1]);
                            return true;
                        }
                    }
                }
            }
        }
        
        final boolean termsEqual = term1.equals(term2);
        if (!term1Var && !term2Var && termsEqual)  {
            return true;
        }
        
        //variable "renaming" to variable of same type is always valid
        if(term1 instanceof Variable && term2 instanceof Variable) {
            final Variable v1 = (Variable) term1;
            final Variable v2 = (Variable) term2;
            if(v1.getType() == v2.getType()) {
                final Variable CommonVar = makeCommonVariable(term1, term2);
                if (map[0] == null) {  map[0] = new HashMap(); map[1] = new HashMap(); }                
                map[0].put(v1, CommonVar);
                map[1].put(v2, CommonVar);
                return true;
            }
        }

        final boolean term1VarUnifyAllowed = term1Var && allowUnification(((Variable) term1).getType(), type);
        final boolean term2VarUnifyAllowed = term2Var && allowUnification(((Variable) term2).getType(), type);

        if (term1VarUnifyAllowed || term2VarUnifyAllowed) {

            Term termA = term1VarUnifyAllowed ? term1 : term2;
            Term termB = term1VarUnifyAllowed ? term2 : term1;
            Variable termAAsVariable = (Variable)termA;

            if (map[0] == null) {  map[0] = new HashMap(); map[1] = new HashMap(); }

            if (term1VarUnifyAllowed) {

                if ((termB instanceof Variable) && allowUnification(((Variable) termB).getType(), type)) {
                    final Variable CommonVar = makeCommonVariable(termA, termB);
                    map[0].put(termAAsVariable, CommonVar);
                    map[1].put(termB, CommonVar);
                } else {
                    if(termB instanceof Variable && ((((Variable)termB).getType()==Symbols.VAR_QUERY && ((Variable)termA).getType()!=Symbols.VAR_QUERY) ||
                        (((Variable)termB).getType()!=Symbols.VAR_QUERY && ((Variable)termA).getType()==Symbols.VAR_QUERY))) {
                        return false;
                    }
                    map[0].put(termAAsVariable, termB);
                    if (termAAsVariable.isCommon()) {
                        map[1].put(termAAsVariable, termB);
                    }
                }
            } else {
                map[1].put(termAAsVariable, termB);
                if (termAAsVariable.isCommon()) {
                    map[0].put(termAAsVariable, termB);
                }
            }

            return true;

        } else {
            final boolean hasAnyTermVars = term1HasVar || term2HasVar;
            final boolean termsHaveSameClass = term1.getClass().equals(term2.getClass());
            
            if (!(hasAnyTermVars && termsHaveSameClass && term1 instanceof CompoundTerm)) {
                return termsEqual;
            }

            final CompoundTerm cTerm1 = (CompoundTerm) term1;
            final CompoundTerm cTerm2 = (CompoundTerm) term2;

            //consider temporal order on term matching
            final boolean isSameOrder = term1.getTemporalOrder() == term2.getTemporalOrder();
            final boolean isSameSpatial = term1.getIsSpatial() == term2.getIsSpatial();
            final boolean isSameOrderAndSameSpatial = isSameOrder && isSameSpatial;

            final boolean areBothConjuctions = term1 instanceof Conjunction && term2 instanceof Conjunction;
            final boolean areBothImplication = term1 instanceof Implication && term2 instanceof Implication;
            final boolean areBothEquivalence = term1 instanceof Equivalence && term2 instanceof Equivalence;

            if((areBothConjuctions && !isSameOrderAndSameSpatial) ||
                ((areBothEquivalence || areBothImplication) && !isSameOrder)
            ) {
                return false;
            }

            if (cTerm1.size() != cTerm2.size()) {
                return false;
            }
            if ((cTerm1 instanceof ImageExt) && (((ImageExt) cTerm1).relationIndex != ((ImageExt) cTerm2).relationIndex) || (cTerm1 instanceof ImageInt) && (((ImageInt) cTerm1).relationIndex != ((ImageInt) cTerm2).relationIndex)) {
                return false;
            }
            final Term[] list = cTerm1.cloneTerms();
            if (cTerm1.isCommutative()) {
                CompoundTerm.shuffle(list, Memory.randomNumber);
                //ok attempt unification
                if(cTerm2 == null || list == null || cTerm2.term == null || list.length != cTerm2.term.length) {
                    return false;
                }
                final Set<Integer> matchedJ = new HashSet<>(list.length * 2);
                for(int i = 0; i < list.length; i++) {
                    boolean succeeded = false;
                    for(int j = 0; j < list.length; j++) {
                        if(matchedJ.contains(j)) { //this one already was used to match one of the i's
                            continue;
                        }
                        final Term ti = list[i].clone();
                        //clone map also:
                        final Map<Term, Term>[] mapNew = (Map<Term, Term>[]) new HashMap<?,?>[2];
                        mapNew[0] = new HashMap<>();
                        mapNew[1] = new HashMap<>();
                        if(map[0] == null) {
                            map[0] = new HashMap<>();
                        }
                        if(map[1] == null) {
                            map[1] = new HashMap<>();
                        }
                        appendToMap(map[0], mapNew[0]);
                        appendToMap(map[1], mapNew[1]);
                        //attempt unification:
                        if(findSubstitute(type,ti,cTerm2.term[i],mapNew)) {
                            appendToMap(mapNew[0], map[0]);
                            appendToMap(mapNew[1], map[1]);
                            succeeded = true;
                            matchedJ.add(j);
                            break;
                        }
                    }
                    if(!succeeded) {
                        return false;
                    }
                }
                return true;
            }
            for (int i = 0; i < cTerm1.size(); i++) {
                final Term t1 = list[i];
                final Term t2 = cTerm2.term[i];
                if (!findSubstitute(type, t1, t2, map)) {
                    return false;
                }
            }
            return true;

        }
    }

    private static void appendToMap(Map<Term, Term> source, Map<Term, Term> target) {
        for(final Term c : source.keySet()) {
            target.put(c, source.get(c));
        }
    }


    /**
     * Check whether a string represent a name of a term that contains a
     * variable
     *
     * @param n The string name to be checked
     * @return Whether the name contains a variable
     */
    public static boolean containVar(final CharSequence n) {
        if (n == null) return false;
        final int l = n.length();
        for (int i = 0; i < l; i++) {
            switch (n.charAt(i)) {                
                case Symbols.VAR_INDEPENDENT:
                case Symbols.VAR_DEPENDENT:
                case Symbols.VAR_QUERY:
                    return true;
            }
        }        
        return false;
    }
    
    public static final boolean containVar(final Term[] t) {
        for (final Term x : t)
            if (x instanceof Variable)
                return true;
        return false;
    }
    

    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final char type, final Term[] t) {
        return unify(type, t[0], t[1], t);
    }

 
    /**
     * To unify two terms
     *
     * @param type The type of variable that can be substituted
     * @param t1 The compound containing the first term, possibly modified
     * @param t2 The compound containing the second term, possibly modified
     * @param compound The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final char type, final Term t1, final Term t2, final Term[] compound) { 
        return unify(type, t1, t2, compound, false);
    }
    public static boolean unify(final char type, final Term t1, final Term t2, final Term[] compound, final boolean allowPartial) {
        final Map<Term, Term> map[] = new Map[2]; //begins empty: null,null
        
        final boolean hasSubs = findSubstitute(type, t1, t2, map, allowPartial);
        if (hasSubs) {
            final Term a = (compound[0] instanceof Variable && map[0].containsKey(compound[0])) ? 
                            map[0].get(compound[0]) : 
                            applySubstituteAndRenameVariables(((CompoundTerm)compound[0]), map[0]);
            if (a == null) return false;
            final Term b = (compound[1] instanceof Variable && map[1].containsKey(compound[1])) ? 
                            map[1].get(compound[1]) :
                            applySubstituteAndRenameVariables(((CompoundTerm)compound[1]), map[1]);
            if (b == null) return false;
            //only set the values if it will return true, otherwise if it returns false the callee can expect its original values untouched
            if(compound[0] instanceof Variable && compound[0].hasVarQuery() && (a.hasVarIndep() || a.hasVarIndep()) ) {
                return false;
            }
            if(compound[1] instanceof Variable && compound[1].hasVarQuery() && (b.hasVarIndep() || b.hasVarIndep()) ) {
                return false;
            }
            compound[0] = a;
            compound[1] = b;
            return true;
        }
        return false;
    }

    /** appliesSubstitute and renameVariables, resulting in a cloned object, 
     *  will not change this instance  */
    private static Term applySubstituteAndRenameVariables(final CompoundTerm t, final Map<Term, Term> subs) {
        if ((subs == null) || (subs.isEmpty())) {
            //no change needed
            return t;
        }
        
        final Term r = t.applySubstitute(subs);
        
        if (r == null) return null;
        
        if (r.equals(t)) return t;
        
        return r;
    }

    public static Variable makeCommonVariable(final Term v1, final Term v2) {
        //TODO use more efficient string construction
        return new Variable(v2.toString() + v1.toString() + '$'); //v2 first since when type does not match
    } //but it is an allowed rename like $1 -> #1 then the second type should be used
    
    /**
     * examines whether a term is using an
     * independent variable in an invalid way
     *
     * @param T term to be examined
     * @return Whether the term contains an independent variable
     */
    public static boolean indepVarUsedInvalid(final Term T) {
        
        //if its a conjunction/disjunction, this is invalid: (&&,<$1 --> test>,<$1 --> test2>), while this isnt: (&&,<$1 --> test ==> <$1 --> test2>,others)
        //this means we have to go through the conjunction, and check if the component is a indepVarUsedInvalid instance, if yes, return true
        //
        if(T instanceof Conjunction || T instanceof Disjunction) {
            final Term[] part=((CompoundTerm)T).term;
            for(final Term t : part) {
                if(indepVarUsedInvalid(t)) {
                    return true;
                }
            }
        }
        
        if(!(T instanceof Inheritance) && !(T instanceof Similarity)) {
            return false;
        }

        return T.hasVarIndep();
    }

    /**
     * Check if two terms can be unified
     *
     * @param type The type of variable that can be substituted
     * @param term1 The first term to be unified
     * @param term2 The second term to be unified
     * @return Whether there is a substitution
     */
    public static boolean hasSubstitute(final char type, final Term term1, final Term term2) {
        return findSubstitute(type, term1, term2, new HashMap<>(), new HashMap<>());
    }
    
}
