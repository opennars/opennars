/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.language;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.opennars.inference.TemporalRules;
import org.opennars.storage.Memory;
import org.opennars.io.Symbols;

/**
 * Static utility class for static methods related to Variables
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
            if(type == Symbols.VAR_QUERY) {
                return true;
            }
        }
        return false;
    }
    
    /** map is a 2-element array of HashMap<Term,Term>. it may be null, in which case
     * the maps will be instantiated as necessary.  
     * this is to delay the instantiation of the 2 HashMap until necessary to avoid
     * wasting them if they are not used.
     */
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map) {
        return findSubstitute(type, term1, term2, map, false);
    }
    public static boolean findSubstitute(final char type, final Term term1, final Term term2, final Map<Term, Term>[] map, boolean allowPartial) {

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
            Conjunction c1 = (Conjunction) term1;
            Conjunction c2 = (Conjunction) term2;
            //more effective matching for NLP
            if(c1.getTemporalOrder() == TemporalRules.ORDER_FORWARD &&
                    c2.getTemporalOrder() == TemporalRules.ORDER_FORWARD) {
                int size_smaller = c1.size();
                if(c1.size() < c2.size()) {
                    //find an offset that works
                    for(int k=0;k<(c2.term.length - c1.term.length);k++) {
                        Map<Term, Term>[] mapk = (Map<Term, Term>[]) new HashMap<?,?>[2];
                        mapk[0] = new HashMap<Term,Term>();
                        mapk[1] = new HashMap<Term,Term>();
                        if(map[0] == null) {
                            map[0] = new HashMap<Term,Term>();
                        }
                        if(map[1] == null) {
                            map[1] = new HashMap<Term,Term>();
                        }
                        for(Term c : map[0].keySet()) {
                            mapk[0].put(c, map[0].get(c));
                        }
                        for(Term c : map[1].keySet()) {
                            mapk[1].put(c, map[1].get(c));
                        }
                        boolean succeeded = true;
                        for(int j=k;j<k+size_smaller;j++) {
                            int i = j-k;
                            Map<Term, Term>[] mapNew = (Map<Term, Term>[]) new HashMap<?,?>[2];
                            mapNew[0] = new HashMap<Term,Term>();
                            mapNew[1] = new HashMap<Term,Term>();
                            for(Term c : map[0].keySet()) {
                                mapNew[0].put(c, map[0].get(c));
                            }
                            for(Term c : map[1].keySet()) {
                                mapNew[1].put(c, map[1].get(c));
                            }
                            //attempt unification:
                            if(findSubstitute(type,c1.term[i],c2.term[j],mapNew)) {
                                for(Term c : mapNew[0].keySet()) { //ok put back the unifications that were necessary
                                    mapk[0].put(c, mapNew[0].get(c));
                                }
                                for(Term c : mapNew[1].keySet()) {
                                    mapk[1].put(c, mapNew[1].get(c));
                                }
                            } else { //another shift k is needed
                                succeeded = false;
                                break;
                            }
                        }
                        if(succeeded) {
                            for(Term c : mapk[0].keySet()) { //ok put back the unifications that were necessary
                                map[0].put(c, mapk[0].get(c));
                            }
                            for(Term c : mapk[1].keySet()) {
                                map[1].put(c, mapk[1].get(c));
                            }
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
        
        
        /*if (termsEqual) {
            return true;
        }*/
        
        Term t;  
        //variable "renaming" to variable of same type is always valid
        if(term1 instanceof Variable && term2 instanceof Variable) {
            Variable v1 = (Variable) term1;
            Variable v2 = (Variable) term2;
            if(v1.getType() == v2.getType()) {
                Variable CommonVar = makeCommonVariable(term1, term2);    
                if (map[0] == null) {  map[0] = new HashMap(); map[1] = new HashMap(); }                
                map[0].put(v1, CommonVar);
                map[1].put(v2, CommonVar);
                return true;
            }
        }
        if (term1Var && allowUnification(((Variable) term1).getType(), type)) {
            final Variable var1 = (Variable) term1;            
            t = map[0]!=null ? map[0].get(var1) : null;
            
            if (t != null) {
                return findSubstitute(type, t, term2, map);
            } else {
                
                if (map[0] == null) {  map[0] = new HashMap(); map[1] = new HashMap(); }
                
                if ((term2 instanceof Variable) && allowUnification(((Variable) term2).getType(), type)) {
                    Variable CommonVar = makeCommonVariable(term1, term2);                    
                    map[0].put(var1, CommonVar);
                    map[1].put(term2, CommonVar);
                } else {
                    if(term2 instanceof Variable && ((((Variable)term2).getType()==Symbols.VAR_QUERY && ((Variable)term1).getType()!=Symbols.VAR_QUERY) ||
                                                     (((Variable)term2).getType()!=Symbols.VAR_QUERY && ((Variable)term1).getType()==Symbols.VAR_QUERY))) {
                        return false;
                    }
                    map[0].put(var1, term2);
                    if (var1.isCommon()) {
                        map[1].put(var1, term2);
                    }
                }
                return true;
            }
        } else if (term2Var && allowUnification(((Variable) term2).getType(), type)) {
            final Variable var2 = (Variable) term2;            
            t = map[1]!=null ? map[1].get(var2) : null;
            
            if (t != null) {
                return findSubstitute(type, term1, t, map);
            } else {
                
                if (map[0] == null) {  map[0] = new HashMap(); map[1] = new HashMap(); }
                
                map[1].put(var2, term1);
                if (var2.isCommon()) {
                    map[0].put(var2, term1);
                }
                return true;
            }
        } else if ((term1HasVar || term2HasVar) && (term1 instanceof CompoundTerm) && term1.getClass().equals(term2.getClass())) {
            final CompoundTerm cTerm1 = (CompoundTerm) term1;
            final CompoundTerm cTerm2 = (CompoundTerm) term2;
            
            //consider temporal order on term matching
            if(term1 instanceof Conjunction && term2 instanceof Conjunction) {
                if(((Conjunction)term1).getTemporalOrder() != ((Conjunction)term2).getTemporalOrder() ||
                   ((Conjunction)term1).getIsSpatial() != ((Conjunction)term2).getIsSpatial())
                    return false;
            }
            if(term1 instanceof Implication && term2 instanceof Implication) {
                if(((Implication)term1).getTemporalOrder() != ((Implication)term2).getTemporalOrder())
                    return false;
            }
            if(term1 instanceof Equivalence && term2 instanceof Equivalence) {
                if(((Equivalence)term1).getTemporalOrder() != ((Equivalence)term2).getTemporalOrder())
                    return false;
            }
            
            if (cTerm1.size() != cTerm2.size()) {
                return false;
            }
            if ((cTerm1 instanceof ImageExt) && (((ImageExt) cTerm1).relationIndex != ((ImageExt) cTerm2).relationIndex) || (cTerm1 instanceof ImageInt) && (((ImageInt) cTerm1).relationIndex != ((ImageInt) cTerm2).relationIndex)) {
                return false;
            }
            Term[] list = cTerm1.cloneTerms();
            if (cTerm1.isCommutative()) {
                CompoundTerm.shuffle(list, Memory.randomNumber);
                HashSet<Integer> alreadyMatched = new HashSet<Integer>();
                //ok attempt unification
                if(cTerm2 == null || list == null || cTerm2.term == null || list.length != cTerm2.term.length) {
                    return false;
                }
                HashSet<Integer> matchedJ = new HashSet<Integer>(list.length*2);
                for(int i = 0; i < list.length; i++) {
                    boolean succeeded = false;
                    for(int j = 0; j < list.length; j++) {
                        if(matchedJ.contains(j)) { //this one already was used to match one of the i's
                            continue;
                        }
                        Term ti = list[i].clone();
                        //clone map also:
                        Map<Term, Term>[] mapNew = (Map<Term, Term>[]) new HashMap<?,?>[2];
                        mapNew[0] = new HashMap<Term,Term>();
                        mapNew[1] = new HashMap<Term,Term>();
                        if(map[0] == null) {
                            map[0] = new HashMap<Term,Term>();
                        }
                        if(map[1] == null) {
                            map[1] = new HashMap<Term,Term>();
                        }
                        for(Term c : map[0].keySet()) {
                            mapNew[0].put(c, map[0].get(c));
                        }
                        for(Term c : map[1].keySet()) {
                            mapNew[1].put(c, map[1].get(c));
                        }
                        //attempt unification:
                        if(findSubstitute(type,ti,cTerm2.term[i],mapNew)) {
                            for(Term c : mapNew[0].keySet()) { //ok put back the unifications that were necessary
                                map[0].put(c, mapNew[0].get(c));
                            }
                            for(Term c : mapNew[1].keySet()) {
                                map[1].put(c, mapNew[1].get(c));
                            }
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
                Term t1 = list[i];
                Term t2 = cTerm2.term[i];
                if (!findSubstitute(type, t1, t2, map)) {
                    return false;
                }
            }
            return true;
        }
        
        return termsEqual;        
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
     * @param compound1 The compound containing the first term, possibly modified
     * @param compound2 The compound containing the second term, possibly modified
     * @param t The first and second term as an array, which will have been modified upon returning true
     * @return Whether the unification is possible.  't' will refer to the unified terms
     */
    public static boolean unify(final char type, final Term t1, final Term t2, final Term[] compound) { 
        return unify(type, t1, t2, compound, false);
    }
    public static boolean unify(final char type, final Term t1, final Term t2, final Term[] compound, boolean allowPartial) {        
        final Map<Term, Term> map[] = new Map[2]; //begins empty: null,null
        
        final boolean hasSubs = findSubstitute(type, t1, t2, map, allowPartial);
        if (hasSubs) {
            final Term a = applySubstituteAndRenameVariables(((CompoundTerm)compound[0]), map[0]);
            if (a == null) return false;
            final Term b = applySubstituteAndRenameVariables(((CompoundTerm)compound[1]), map[1]);
            if (b == null) return false;
            //only set the values if it will return true, otherwise if it returns false the callee can expect its original values untouched
            if(compound[0] instanceof Variable && ((Variable)compound[0]).hasVarQuery() && (((Variable)a).hasVarIndep() || ((Variable)a).hasVarIndep()) ) {
                return false;
            }
            if(compound[1] instanceof Variable && ((Variable)compound[1]).hasVarQuery() && (((Variable)b).hasVarIndep() || ((Variable)b).hasVarIndep()) ) {
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
        
        Term r = t.applySubstitute(subs);
        
        if (r == null) return null;
        
        if (r.equals(t)) return t;
        
        return r;
    }

    public static Variable makeCommonVariable(final Term v1, final Term v2) {
        //TODO use more efficient string construction
        return new Variable(v2.toString() + v1.toString() + '$'); //v2 first since when type does not match
    } //but it is an allowed rename like $1 -> #1 then the second type should be used
    
    /**
     * Check whether a term is using an
     * independent variable in an invalid way
     *
     * @param n The string name to be checked
     * @return Whether the name contains an independent variable
     */
    public static boolean indepVarUsedInvalid(Term T) {
        
        //if its a conjunction/disjunction, this is invalid: (&&,<$1 --> test>,<$1 --> test2>), while this isnt: (&&,<$1 --> test ==> <$1 --> test2>,others)
        //this means we have to go through the conjunction, and check if the component is a indepVarUsedInvalid instance, if yes, return true
        //
        if(T instanceof Conjunction || T instanceof Disjunction) {
            Term[] part=((CompoundTerm)T).term;
            for(Term t : part) {
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
