/*
 * CompoundTerm.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.language;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import nars.core.Memory;
import nars.entity.TermLink;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;
import static nars.language.CompoundTerm.makeCompoundName;


public abstract class CompoundTerm extends Term {
    
    /**
     * list of (direct) term
     * TODO make final again
     */
    public Term[] term;
    
    /**
     * syntactic complexity of the compound, the sum of those of its term
 plus 1
     */
    public short complexity;
    
    
    /** Whether contains a variable */
    private boolean hasVar;
    
    transient int containedTemporalRelations = -1;
    

    /**
     * Abstract method to get the operator of the compound
     */
    public abstract NativeOperator operator();

    /**
     * Abstract clone method
     *
     * @return A clone of the compound term
     */
    @Override public abstract CompoundTerm clone();
    

    
    
    /* ----- object builders, called from subclasses ----- */
    /**
     * Constructor called from subclasses constructors to clone the fields
     *
     * @param name Name
     * @param components Component list
     * @param isConstant Whether the term refers to a concept
     * @param complexity Complexity of the compound term
     */
    @Deprecated protected CompoundTerm(final CharSequence name, final Term[] components, final boolean isConstant, final short complexity) {
        this(components);
    }

    /**
     * High-performance constructor that avoids recalculating some Term metadata when created.
     * Similar to other constructors, except it does not invoke super(name) to avoid recomputing hashcode 
     * and containsVar.  Instead, all necessary values are provided directly from the callee.
     * This should perform better than the other constructor that invokes super constructor; this does not.
     */
    @Deprecated protected CompoundTerm(final CharSequence name, final Term[] components, final boolean isConstant, final boolean containsVar, final short complexity) {
        this(components);
    }
    
    
    /**
     * Constructor called from subclasses constructors to initialize the fields
     *
     * @param name Name of the compound
     * @param components Component list
     */
    @Deprecated protected CompoundTerm(final CharSequence name, final Term[] components) {
        this(components);
    }

    /** should call refresh(components) after pre-initialization */
    protected CompoundTerm() {
        super();
    }
    
    public CompoundTerm(Term[] components) {
        setTerms(components);
    }
    
    /** call this after changing Term[] contents */
    public void setTerms(Term[] components) {
        this.hasVar = false;
        
        int numVariableSubTerms = 0;
        for (Term t : components) {
            if (t.containVar()) {                 
                hasVar = true; 
                numVariableSubTerms++;
                break; 
            }
        }
        
        //use > 1 to maintain ordinary variable names if it won't interfere
        if (numVariableSubTerms > 0) {
            
            //System.out.print("in: " + Arrays.toString(components));
            this.term = normalizeVariableNames("", components, new HashMap<>());
            //System.out.println("   out: " + Arrays.toString(term));
        }
        else {
            this.term = components;
        }
        
        setName(makeName());
        
        this.complexity = calcComplexity();
    }
    
    public final CompoundTerm clone(final Term[] replaced) {
        CompoundTerm c = clone();
        c.setTerms(replaced);
        return c;
    }



    private Term[] ensureValidComponents(final Term[] components) {
        if (components.length < getMinimumRequiredComponents()) {
            throw new RuntimeException(getClass().getSimpleName() + " requires >=" + getMinimumRequiredComponents() + " components, invalid argument:" + Arrays.toString(components));
        }
        
        //return Collections.unmodifiableList( term );
        return components;
    }
    
    public int getMinimumRequiredComponents() {
        return 2;
    }

            

    protected boolean calcContainedVariables() {
        return Variables.containVar(name());
    }
    
    /**
     * The complexity of the term is the sum of those of the term plus 1
     */
    protected short calcComplexity() {
        int c = 1;
        for (final Term t : term) {
            c += t.getComplexity();        
        }
        return (short)c;
    }
 



        
//
//    /**
//     * Orders among terms: variable < atomic < compound
//     *
//     * @param that The Term to be compared with the current Term
//\     * @return The order of the two terms
//     */
//    @Override
//    public int compareTo(final AbstractTerm that) {
//        if (this == that) return 0;
//        
//        if (that instanceof CompoundTerm) {
//            final CompoundTerm t = (CompoundTerm) that;
//            if (size() == t.size()) {
//                int opDiff = this.operator().ordinal() - t.operator().ordinal(); //should be faster faster than Enum.compareTo                
//                if (opDiff != 0) {
//                    return opDiff;
//                }
//                
//                int tDiff = this.getTemporalOrder() - t.getTemporalOrder(); //should be faster faster than Enum.compareTo                
//                if (tDiff != 0) {
//                    return tDiff;
//                }
//
//                for (int i = 0; i < term.length; i++) {
//                    final int diff = term[i].compareTo(t.term[i]);
//                    if (diff != 0) {
//                        return diff;
//                    }
//                }
//
//                return 0;
//            } else {
//                return size() - t.size();
//            }
//        } else {
//            return 1;
//        }
//    }

    @Override
    public int containedTemporalRelations() {
        if (containedTemporalRelations == -1) {
            
            containedTemporalRelations = 0;
            
            if ((this instanceof Equivalence) || (this instanceof Implication)) {
                int temporalOrder = ((Statement)this).getTemporalOrder();
                switch (temporalOrder) {
                    case TemporalRules.ORDER_FORWARD:
                    case TemporalRules.ORDER_CONCURRENT:
                    case TemporalRules.ORDER_BACKWARD:
                        containedTemporalRelations = 1;
                }                
            }
            
            for (final Term t : term)
                containedTemporalRelations += t.containedTemporalRelations();
        }
        return this.containedTemporalRelations;
    }
    
    

    /*
    @Override
    public boolean equals(final Object that) {
        return (that instanceof Term) && (compareTo((Term) that) == 0);
    }
    */

    
//    @Override
//    public boolean equals(final Object that) {
//        if (!(that instanceof CompoundTerm))
//            return false;
//        
//        final CompoundTerm t = (CompoundTerm)that;
//        return name().equals(t.name());
//        
//        /*if (hashCode() != t.hashCode())
//            return false;
//        
//        if (operator() != t.operator())
//            return false;
//        
//        if (size() != t.size())
//            return false;
//        
//        for (int i = 0; i < term.size(); i++) {
//            final Term c = term.get(i);
//            if (!c.equals(t.componentAt(i)))
//                return false;
//        }
//        
//        return true;*/
//        
//    }
//
//
//        
//
//    /**
//     * Orders among terms: variable < atomic < compound
//     *
//     * @param that The Term to be compared with the current Term
//\     * @return The order of the two terms
//     */
//    @Override
//    public int compareTo(final Term that) {
//        /*if (!(that instanceof CompoundTerm)) {
//            return getClass().getSimpleName().compareTo(that.getClass().getSimpleName());
//        }
//        */        
//        return -name.compareTo(that.name());
//            /*
//            if (size() == t.size()) {
//                int opDiff = this.operator().ordinal() - t.operator().ordinal(); //should be faster faster than Enum.compareTo                
//                if (opDiff != 0) {
//                    return opDiff;
//                }
//
//                for (int i = 0; i < term.length; i++) {
//                    final int diff = term[i].compareTo(t.term[i]);
//                    if (diff != 0) {
//                        return diff;
//                    }
//                }
//
//                return 0;
//            } else {
//                return size() - t.size();
//            }
//        } else {
//            return 1;
//            */
//    }



    /**
     * build a component list from terms
     * @return the component list
     */
    public static Term[] termArray(final Term... t) {
        return t;
    }
    public static List<Term> termList(final Term... t) {
        return Arrays.asList((Term[])t);
    }
    
    

    /* ----- utilities for oldName ----- */
    /**
     * default method to make the oldName of the current term from existing
     * fields
     *
     * @return the oldName of the term
     */
    protected CharSequence makeName() {
        return makeCompoundName(operator(), term);
    }

    /**
     * default method to make the oldName of a compound term from given fields
     *
     * @param op the term operator
     * @param arg the list of term
     * @return the oldName of the term
     */
    protected static String makeCompoundName(final NativeOperator op, final Term... arg) {
        final int sizeEstimate = 12 * arg.length;
        
        final StringBuilder n = new StringBuilder(sizeEstimate)
            .append(COMPOUND_TERM_OPENER.ch).append(op.toString());
            
        for (final Term t : arg) {
            if (t == null) {
                throw new RuntimeException("Term is null: " + Arrays.toString(arg));
            }
            n.append(Symbols.ARGUMENT_SEPARATOR).append(t.name());
        }
        
        n.append(COMPOUND_TERM_CLOSER.ch);
                
        return n.toString();
    }
    

 

    /* ----- utilities for other fields ----- */
    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */    
    @Override public short getComplexity() {
        return complexity;
    }

    /**
     * isConstant means if the term contains free variable, which implies it can name a Concept
     * True if:
     *   has zero variables, or
     *   uses several instances of the same variable
     * False if it uses one instance of a variable ("free" like a "free radical" in chemistry).
     * Therefore it may be considered Constant, yet actually contain variables.
     * 
     * @return if the term is a constant
     */
    @Override
    public boolean isConstant() {
        return true;
    }

    /**
     * Check if the order of the term matters
     * <p>
     * commutative CompoundTerms: Sets, Intersections Commutative Statements:
     * Similarity, Equivalence (except the one with a temporal order)
     * Commutative CompoundStatements: Disjunction, Conjunction (except the one
     * with a temporal order)
     *
     * @return The default value is false
     */
    public boolean isCommutative() {
        return false;
    }

    /* ----- extend Collection methods to component list ----- */
    /**
     * get the number of term
     *
     * @return the size of the component list
     */
    final public int size() {
        return term.length;
    }


    /** Gives a set of all contained term, recursively */
    public Set<Term> getContainedTerms() {
        Set<Term> s = new HashSet(getComplexity());
        for (Term t : term) {
            s.add(t);
            if (t instanceof CompoundTerm)
                s.addAll( ((CompoundTerm)t).getContainedTerms() );
        }
        return s;
    }

    /**
     * Clone the component list
     *
     * @return The cloned component list
     */
    public Term[] cloneTerms(final Term... additional) {
        return cloneTermsAppend(term, additional);
    }
    

    /**
     * Cloned array of Terms, except for one or more Terms.
     * @param toRemove
     * @return the cloned array with the missing terms removed, OR null if no terms were actually removed when requireModification=true
     */
    public Term[] cloneTermsExcept(final boolean requireModification, final Term[] toRemove) {
        //TODO if deep, this wastes created clones that are then removed.  correct this inefficiency?
        
        List<Term> l = getTermList();
        boolean removed = false;
                
        for (final Term t : toRemove) {
            if (l.remove(t))
                removed = true;
        }
        if ((!removed) && (requireModification))
            return null;
                
        return l.toArray(new Term[l.size()]);
    }
    

    
    /**
     * Deep clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static Term[] cloneTermsAppend(final Term[] original, final Term[] additional) {
        if (original == null) {
            return null;
        }        

        int L = original.length + additional.length;
        if (L == 0)
            return original;
        
        //TODO apply preventUnnecessaryDeepCopy to more cases
        
        final Term[] arr = new Term[L];
        
        int i;
        int j = 0;
        Term[] srcArray = original;
        for (i = 0; i < L; i++) {            
            if (i == original.length) {
                srcArray = additional;
                j = 0;
            }
            
            arr[i] = srcArray[j++];
        }

        return arr;
        
    }

    public List<Term> getTermList() {        
        ArrayList l = new ArrayList(term.length);
        addTermsTo(l);
        return l;
    }
  
    /** forced deep clone of terms */
    public ArrayList<Term> cloneTermsListDeep() {
        ArrayList<Term> l = new ArrayList(term.length);
        for (final Term t : term)
            l.add(t.clone());
        return l;        
    }

    

    
    /*static void shuffle(final Term[] list, final Random randomNumber) {
        if (list.length < 2)  {
            return;
        }
        
        
        int n = list.length;
        for (int i = 0; i < n; i++) {
            // between i and n-1
            int r = i + (randomNumber.nextInt() % (n-i));
            Term tmp = list[i];    // swap
            list[i] = list[r];
            list[r] = tmp;
        }
    }*/
    
        static void shuffle(final Term[] ar,final Random randomNumber)
        {
            if (ar.length < 2)  {
                return;
            }

          for (int i = ar.length - 1; i > 0; i--)
          {
            int index = randomNumber.nextInt(i + 1);
            // Simple swap
            Term a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
          }
        }
    
    

    /**
     * Check whether the compound contains a certain component
     * Also matches variables, ex: (&&,<a --> b>,<b --> c>) also contains <a --> #1>
     * @param t The component to be checked
     * @return Whether the component is in the compound
     */
    @Override
    public boolean containsTerm(final Term t) {        
        return Terms.contains(term, t);
    }

    /**
     * Recursively check if a compound contains a term
     *
     * @param target The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public boolean containsTermRecursively(final Term target) {
        for (final Term term : term) {
            if (term.containsTermRecursively(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the compound contains all term of another term, or
 that term as a whole
     *
     * @param t The other term
     * @return Whether the term are all in the compound
     */
    public boolean containsAllTermsOf(final Term t) {
        if (getClass() == t.getClass()) { //(t instanceof CompoundTerm) {
            return Terms.containsAll(term, ((CompoundTerm) t).term );
        } else {
            return Terms.contains(term, t);
        }
    }

//    /**
//     * Try to add a component into a compound
//     *
//     * @param t1 The compound
//     * @param t2 The component
//     * @param memory Reference to the memory
//     * @return The new compound
//     */
//    public static Term addComponents(final CompoundTerm t1, final Term t2, final Memory memory) {
//        if (t2 == null)
//            return t1;
//        
//        boolean success;
//        Term[] terms;
//        if (t2 instanceof CompoundTerm) {
//            terms = t1.cloneTerms(((CompoundTerm) t2).term);
//        } else {
//            terms = t1.cloneTerms(t2);
//        }
//        return Memory.make(t1, terms, memory);
//    }



    /**
     * Try to replace a component in a compound at a given index by another one
     *
     * @param compound The compound
     * @param index The location of replacement
     * @param t The new component
     * @param memory Reference to the memory
     * @return The new compound
     */
    public Term setComponent(final int index, final Term t, final Memory memory) {
        List<Term> list = getTermList();//Deep();
        list.remove(index);
        if (t != null) {
            if (getClass() != t.getClass()) {
                list.add(index, t);
            } else {
                //final List<Term> list2 = ((CompoundTerm) t).cloneTermsList();
                Term[] tt = ((CompoundTerm)t).term;
                for (int i = 0; i < tt.length; i++) {
                    list.add(index + i, tt[i]);
                }
            }
        }
        return memory.term(this, list);
    }

    /* ----- variable-related utilities ----- */
    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    @Override
    public boolean containVar() {
        return hasVar;
    }

    
    /** caches a static copy of commonly uesd index variables of each variable type */
    public static final int maxCachedVariableIndex = 32;
    public static final Variable[][] varCache = (Variable[][]) Array.newInstance(Variable.class, 3, maxCachedVariableIndex);
    
    public static Variable getIndexVariable(final char type, final int i) {
        int typeI;
        switch (type) {
            case '#': typeI = 0; break;
            case '$': typeI = 1; break;
            case '?': typeI = 2; break;
            default: throw new RuntimeException("Invalid variable type: " + type + ", index " + i);
        }
        
        if (i < maxCachedVariableIndex) {
            Variable existing = varCache[typeI][i];
            if (existing == null)
                existing = varCache[typeI][i] = new Variable(type + String.valueOf(i));
            return existing;
        }
        else
            return new Variable(type + String.valueOf(i));
    }



    
    /**
     * Recursively rename the variables in the compound
     *
     * @param map The substitution established so far
     * @return an array of terms, normalized; may return the original Term[] array if nothing changed,
     * otherwise a clone of the array will be returned
     */
    public static Term[] normalizeVariableNames(String prefix, final Term[] s, final HashMap<Variable, Variable> map) {
        
        boolean renamed = false;
        Term[] t = s.clone();
        char c = 'a';
        for (int i = 0; i < t.length; i++) {
            final Term term = t[i];

            if (term instanceof Variable) {

                Variable termV = (Variable)term;
                Variable var;

                if (term.name().length() == 1) { // anonymous variable from input
                    //var = getIndexVariable(termV.getType(), map.size()+1);
                    var = new Variable(termV.getType() + prefix + (map.size() + 1));
                } else {
                    var = map.get(termV);
                    if (var == null) {
                        //var = getIndexVariable(termV.getType(), map.size() + 1);
                        var = new Variable(termV.getType() + prefix + (map.size() + 1));
                    }
                }
                if (!termV.equals(var)) {
                    t[i] = var;
                    renamed = true;
                }

                map.put(termV, var);

            } else if (term instanceof CompoundTerm) {
                CompoundTerm ct = (CompoundTerm)term;
                if (ct.containVar()) {
                    Term[] d = normalizeVariableNames(prefix + Character.toString(c),  ct.term, map);
                    if (d!=ct.term) {                        
                        t[i] = ct.clone( d );
                        renamed = true;
                    }
                }
            }        
            c++;
        }
            
        if (renamed)
            return t;
        else 
            return s;
    }

    /** NOT TESTED YET */
    public boolean containsAnyTermsOf(final Collection<Term> c) {
        return Terms.containsAny(term, c);
    }
    
    /**
     * Recursively apply a substitute to the current CompoundTerm
     *
     * @param subs
     */
    public CompoundTerm applySubstitute(final Map<Term, Term> subs) {   
        if ((subs == null) || (subs.isEmpty())) {            
            return this;
        }
        
        //if (!containsAnyTermsOf(subs.keySet()))
            //return this;
        Term[] tt = new Term[term.length];
        boolean modified = false;
        
        for (int i = 0; i < tt.length; i++) {
            Term t1 = tt[i] = term[i];            
            
            if (subs.containsKey(t1)) {
                Term t2 = subs.get(t1);                            
                while (subs.containsKey(t2)) {
                    t2 = subs.get(t2);
                }
                //prevents infinite recursion
                if (!t2.containsTerm(t1)) {
                    tt[i] = t2.clone();
                    modified = true;
                }
            } else if (t1 instanceof CompoundTerm) {
                /*if (InferenceTracer.guardStack(50, "applySubstitute", this, t1, subs)) {
                    System.err.println(i + " "  + this + " " + t1 + " " + subs);
                    //new Exception().printStackTrace();;
                    //System.exit(1);
                }*/
                tt[i] = ((CompoundTerm) t1).applySubstitute(subs);
                if (!tt[i].equals(term[i]))
                    modified = true;
            }            
        }
        if (!modified)
            return this;
        
        if (this.isCommutative()) {         
            Arrays.sort(tt);
        }
        
        return this.clone(tt);
    }

    /* ----- link CompoundTerm and its term ----- */
    /**
     * Build TermLink templates to constant term and subcomponents
     * <p>
     * The compound type determines the link type; the component type determines
     * whether to build the link.
     *
     * @return A list of TermLink templates
     */
    public ArrayList<TermLink> prepareComponentLinks() {
        //complexity seems like an upper bound for the resulting number of componentLinks. 
        //so use it as an initial size for the array list
        final ArrayList<TermLink> componentLinks = new ArrayList<>( getComplexity() );              
        return Terms.prepareComponentLinks(componentLinks, this);
    }

    final public void addTermsTo(final Collection<Term> c) {
        for (final Term t : term)
            c.add(t);
    }

    public final TreeSet<Term> getTermTreeSet() {
        TreeSet<Term> set = new TreeSet<>();
        addTermsTo(set);
        return set;        
    }


}
