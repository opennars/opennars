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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import nars.entity.TermLink;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static nars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;
import static nars.language.CompoundTerm.makeCompoundName;
import nars.storage.Memory;


public abstract class CompoundTerm extends Term {
    
    @Deprecated private static final boolean preventUnnecessaryDeepCopy = true; //temporary, disables eliminating deep copy
    //one reason why deepcopy is necessary is to eliminate cyclic clones 
    

    /**
     * list of (direct) term
     */
    public final Term[] term;
    
    /**
     * syntactic complexity of the compound, the sum of those of its term
 plus 1
     */
    public final short complexity;
    
    /** Whether the term names a concept */
    private boolean isConstant;
    
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
        this.name = name;
        this.term = components; //ensureValidComponents(term);
        this.hasVar = Variables.containVar(name());
        this.isConstant = isConstant;
        this.complexity = complexity;
    }

    /**
     * High-performance constructor that avoids recalculating some Term metadata when created.
     * Similar to other constructors, except it does not invoke super(name) to avoid recomputing hashcode 
     * and containsVar.  Instead, all necessary values are provided directly from the callee.
     * This should perform better than the other constructor that invokes super constructor; this does not.
     */
    protected CompoundTerm(final CharSequence name, final Term[] components, final boolean isConstant, final boolean containsVar, final short complexity) {
        this.name = name;
        this.term = components; //ensureValidComponents(term);
        this.hasVar = containsVar;
        this.isConstant = isConstant;
        this.complexity = complexity;
    }
    
    
    /**
     * Constructor called from subclasses constructors to initialize the fields
     *
     * @param name Name of the compound
     * @param components Component list
     */
    protected CompoundTerm(final CharSequence name, final Term[] components) {
        this.term = components; //ensureValidComponents(term);
        this.complexity = calcComplexity();
        setName(name);
        this.isConstant = !hasVar;
    }



// --Commented out by Inspection START (8/15/14 2:37 AM):
//    /**
//     * Constructor called from subclasses constructors to initialize the fields.
//     * Calculates name, hasVar, & complexity.
//     *
//     * @param components Component list
//     */
//    @Deprecated protected CompoundTerm(final Term[] components) {
//        this.term = components; //ensureValidComponents(term);
//        this.complexity = calcComplexity();
//        setName(makeName());
//        this.isConstant = !hasVar;
//    }
// --Commented out by Inspection STOP (8/15/14 2:37 AM)


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

    @Override
    protected final boolean setName(CharSequence name) {
        if (super.setName(name)) {
            this.hasVar = Variables.containVar(name());
            return true;
        }
        return false;
    }
    
    
    

    /**
     * The complexity of the term is the sum of those of the term plus 1
     */
    private short calcComplexity() {
        int c = 1;
        for (final Term t : term) {
            c += t.getComplexity();        
        }
        return (short)c;
    }
 
 
 @Override
    public boolean equals(final Object that) {       
        if (!(that instanceof CompoundTerm))
            return false;
        
        final CompoundTerm t = (CompoundTerm)that;
        return name().equals(t.name());
    }


        

    /**
     * Orders among terms: variable < atomic < compound
     *
     * @param that The Term to be compared with the current Term
\     * @return The order of the two terms
     */
    @Override
    public int compareTo(final Term that) {
        if (this == that) return 0;
        
        if (that instanceof CompoundTerm) {
            final CompoundTerm t = (CompoundTerm) that;
            if (size() == t.size()) {
                int opDiff = this.operator().ordinal() - t.operator().ordinal(); //should be faster faster than Enum.compareTo                
                if (opDiff != 0) {
                    return opDiff;
                }
                
                int tDiff = this.getTemporalOrder() - t.getTemporalOrder(); //should be faster faster than Enum.compareTo                
                if (tDiff != 0) {
                    return tDiff;
                }

                for (int i = 0; i < term.length; i++) {
                    final int diff = term[i].compareTo(t.term[i]);
                    if (diff != 0) {
                        return diff;
                    }
                }

                return 0;
            } else {
                return size() - t.size();
            }
        } else {
            return 1;
        }
    }

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
        return Arrays.asList(t);
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
    protected static String makeCompoundName(final NativeOperator op, final Term[] arg) {
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
     * check if the term contains free variable
     *
     * @return if the term is a constant
     */
    @Override
    public boolean isConstant() {
        return isConstant;
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
    public int size() {
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
    public Term[] cloneTerms(boolean deep, Term... additional) {
        return cloneTermsAppend(deep, term, additional);
    }
    
    public Term[] cloneTerms(Term... additional) {
        return cloneTerms(true, additional);
    }
    
    public Term[] cloneTermsExcept(boolean requireModification, final Term... toRemove) {
        return cloneTermsExcept(true, requireModification, toRemove);
    }

    /**
     * Cloned array of Terms, except for one or more Terms.
     * @param toRemove
     * @return the cloned array with the missing terms removed, OR null if no terms were actually removed when requireModification=true
     */
    private Term[] cloneTermsExcept(boolean deep, boolean requireModification, final Term... toRemove) {
        //TODO if deep, this wastes created clones that are then removed.  correct this inefficiency?
        
        List<Term> l = cloneTermsList(deep);
        boolean removed = false;
                
        for (final Term t : toRemove) {
            if (l.remove(t))
                removed = true;
        }
        if ((!removed) && (requireModification))
            return null;
                
        return l.toArray(new Term[l.size()]);
    }
    

    public static Term[] cloneTermsAppend(final Term[] original, Term... additional) {
        return cloneTermsAppend(true, original, additional);
    }
    
    /**
     * Deep clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static Term[] cloneTermsAppend(boolean deep, final Term[] original, Term... additional) {
        if (original == null) {
            return null;
        }        

        final Term[] arr = new Term[original.length + additional.length];
        
        int i;
        for (i = 0; i < original.length; i++) {            
            final Term t = original[i];      
            
            //experiental optimization
            //if (allowNonDeepCopy && t.isConstant() && !t.containVar())
            if (preventUnnecessaryDeepCopy && t.getClass() == Term.class)
                arr[i] = t;
            else
                arr[i] = ( deep ? t.clone() : t );

            //arr[i] = ( deep ? (Term)t.clone() : t );
        
        }
        for (int j = 0; j < additional.length; j++) {
            final Term t = additional[j];                    
            
            //experiental optimization
            if (preventUnnecessaryDeepCopy && t.getClass() == Term.class)
                arr[i+j] = t;
            else            
                arr[i+j] = ( deep ? t.clone() : t );
            
            //arr[i+j] = ( deep ? (Term)t.clone() : t );
        }
        return arr;
        
    }
    
    public ArrayList<Term> cloneTermsList() {
        return cloneTermsList(true);
    }

    public ArrayList<Term> cloneTermsList(boolean deep) {
        ArrayList<Term> l = new ArrayList(term.length);
        for (final Term t : term) {
             //experiental optimization
            if (preventUnnecessaryDeepCopy && t.getClass() == Term.class)
                l.add(t);
            else
                l.add( deep ? t.clone() : t );  
            
            //l.add( deep ? (Term)t.clone() : t );
        }
        return l;        
    }
  
    /** forced deep clone of terms */
    public ArrayList<Term> cloneTermsListDeep() {
        ArrayList<Term> l = new ArrayList(term.length);
        for (final Term t : term)
            l.add(t.clone());
        return l;        
    }

    

    
    static void shuffle(final Term[] list, final Random randomNumber) {
        if (list.length < 2)  {
            return;
        }
        
        int n = list.length;
        for (int i = 0; i < n; i++) {
            // between i and n-1
            int r = i + (int) (randomNumber.nextDouble() * (n-i));
            Term tmp = list[i];    // swap
            list[i] = list[r];
            list[r] = tmp;
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
        List<Term> list = cloneTermsListDeep();
        list.remove(index);
        if (t != null) {
            if (getClass() != t.getClass()) {
                list.add(index, t);
            } else {
                final List<Term> list2 = ((CompoundTerm) t).cloneTermsList();
                for (int i = 0; i < list2.size(); i++) {
                    list.add(index + i, list2.get(i));
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

    /**
     * Rename the variables in the compound, called from Sentence constructors
     */
    @Override
    public void renameVariables() {
        if (containVar()) {
            //int existingComponents = term.length;
            boolean b = renameVariables(new HashMap<>());
            if (b) {
                setName(makeName());                
            }
        }
        isConstant = true;        
    }

    /**
     * Recursively rename the variables in the compound
     *
     * @param map The substitution established so far
     */
    private boolean renameVariables(final HashMap<Variable, Variable> map) {
        if (containVar()) {
            boolean renamed = false;
            for (int i = 0; i < term.length; i++) {
                final Term term = this.term[i];
                if (term instanceof Variable) {
                    Variable var;                    
                    if (term.name().length() == 1) { // anonymous variable from input
                        var = new Variable(term.name().charAt(0) + String.valueOf(map.size() + 1));
                    } else {
                        var = map.get(term);
                        if (var == null) {
                            var = new Variable(term.name().charAt(0) + String.valueOf(map.size() + 1));
                        }
                    }
                    if (!term.equals(var)) {
                        this.term[i] = var;
                        renamed = true;
                    }
                    map.put((Variable) term, var);
                    
                } else if (term instanceof CompoundTerm) {
                    CompoundTerm ct = (CompoundTerm)term;
                    boolean r = ct.renameVariables(map);
                    if (r) {
                        ct.setName(ct.makeName());
                        renamed = true;
                    }
                }                
            }
            return renamed;
        }
        return false;
    }

    /**
     * Recursively apply a substitute to the current CompoundTerm
     *
     * @param subs
     */
    public void applySubstitute(final HashMap<Term, Term> subs) {                
        for (int i = 0; i < term.length; i++) {
            Term t1 = term[i];
            if (subs.containsKey(t1)) {
                Term t2 = subs.get(t1);                            
                while (subs.containsKey(t2)) {
                    t2 = subs.get(t2);
                }
                //prevents infinite recursion
                if (!t2.containsTerm(t1))
                    term[i] = t2.clone();
            } else if (t1 instanceof CompoundTerm) {
                ((CompoundTerm) t1).applySubstitute(subs);
            }            
        }
        if (this.isCommutative()) {         
            Arrays.sort(term);
        }
        setName( makeName() );
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


}
