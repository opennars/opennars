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
import java.util.Collection;
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
import static nars.language.CompoundTerm.make;
import static nars.language.CompoundTerm.makeCompoundName;
import nars.storage.Memory;


public abstract class CompoundTerm extends Term {


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



    /**
     * Abstract method to get the operator of the compound
     */
    public abstract NativeOperator operator();

    /**
     * Abstract clone method
     *
     * @return A clone of the compound term
     */
    @Override
    public abstract Object clone();

    
    
    /* ----- object builders, called from subclasses ----- */
    /**
     * Constructor called from subclasses constructors to clone the fields
     *
     * @param name Name
     * @param components Component list
     * @param isConstant Whether the term refers to a concept
     * @param complexity Complexity of the compound term
     */
    protected CompoundTerm(final String name, final Term[] components, final boolean isConstant, final short complexity) {
        super(name);
        this.term = components; //ensureValidComponents(term);
        this.isConstant = isConstant;
        this.complexity = complexity;
    }

    /**
     * High-performance constructor that avoids recalculating some Term metadata when created.
     * Similar to other constructors, except it does not invoke super(name) to avoid recomputing hashcode 
     * and containsVar.  Instead, all necessary values are provided directly from the callee.
     * This should perform better than the other constructor that invokes super constructor; this does not.
     */
    protected CompoundTerm(final String name, final Term[] components, final boolean isConstant, final boolean containsVar, final short complexity) {
        this.name = name;
        this.term = components; //ensureValidComponents(term);
        this.hasVar = containsVar;
        this.isConstant = isConstant;
        this.complexity = complexity;
    }
    


    /**
     * Constructor called from subclasses constructors to initialize the fields
     *
     * @param components Component list
     */
    @Deprecated protected CompoundTerm(final Term[] components) {
        this.term = components; //ensureValidComponents(term);
        this.complexity = calcComplexity();
        setName(makeName());        
        this.isConstant = !hasVar;
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

    @Override
    protected final boolean setName(String name) {
        if (super.setName(name)) {
            this.hasVar = Variable.containVar(getName());
            return true;
        }
        return false;
    }
    
    
    
    /**
     * Constructor called from subclasses constructors to initialize the fields
     *
     * @param name Name of the compound
     * @param components Component list
     */
    protected CompoundTerm(final String name, final Term[] components) {
        this.term = components; //ensureValidComponents(term);
        this.complexity = calcComplexity();
        setName(name);
        this.isConstant = !hasVar;
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
 
    /*
    @Override
    public boolean equals(final Object that) {
        return (that instanceof Term) && (compareTo((Term) that) == 0);
    }
    */
    
    //slightly faster version of equals() that works like compare but only needs to return true/false
    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        
        if (!(that instanceof CompoundTerm))
            return false;
        
        final CompoundTerm t = (CompoundTerm)that;
        
        if (hashCode() != t.hashCode())
            return false;
        
        /*if (operator() != t.operator())
            return false;
        
        if (size() != t.size())
            return false;
        
        for (int i = 0; i < term.size(); i++) {
            final Term c = term.get(i);
            if (!c.equals(t.componentAt(i)))
                return false;
        }
        
        return true;*/
        
        return getName().equals(t.getName());
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

    /* static methods making new compounds, which may return null */
    /**
     * Try to make a compound term from a template and a list of term
     *
     * @param compound The template
     * @param components The term
     * @param memory Reference to the memory
     * @return A compound term or null
     */
    public static Term make(final CompoundTerm compound, final Term[] components, final Memory memory) {
                
        if (compound instanceof ImageExt) {
            return ImageExt.make(components, ((ImageExt) compound).getRelationIndex(), memory);
        } else if (compound instanceof ImageInt) {
            return ImageInt.make(components, ((ImageInt) compound).getRelationIndex(), memory);
        } else {
            return make(compound.operator(), components, memory);
        }
    }
    
    public static Term make(final CompoundTerm compound, Collection<Term> components, final Memory memory) { 
        Term[] c = components.toArray(new Term[components.size()]);
        return make(compound, c, memory);
    }

    /**
     * Try to make a compound term from an operator and a list of term
     * <p>
     * Called from StringParser
     *
     * @param op Term operator
     * @param arg Component list
     * @param memory Reference to the memory
     * @return A compound term or null
     */    
    public static Term make(final NativeOperator op, final Term[] a, final Memory memory) {
        
        
        switch (op) {
            case SET_EXT_OPENER: 
                return SetExt.make(termList(a), memory);
            case SET_INT_OPENER: 
                return SetInt.make(termList(a), memory);
            case INTERSECTION_EXT: 
                return IntersectionExt.make(termList(a), memory);
            case INTERSECTION_INT:
                return IntersectionInt.make(termList(a), memory);
            case DIFFERENCE_EXT:
                return DifferenceExt.make(a, memory);
            case DIFFERENCE_INT:
                return DifferenceInt.make(a, memory);
            case INHERITANCE:
                return Inheritance.make(a[0], a[1], memory);
            case PRODUCT:
                return Product.make(a, memory);
            case IMAGE_EXT:
                return ImageExt.make(a, memory);
            case IMAGE_INT:
                return ImageInt.make(a, memory);                    
            case NEGATION:
                return Negation.make(a, memory);            
            case DISJUNCTION:
                return Disjunction.make(termList(a), memory);            
            case CONJUNCTION:
                return Conjunction.make(a, memory);
            case SEQUENCE:
                return Conjunction.make(a, TemporalRules.ORDER_FORWARD, memory);
            case PARALLEL:
                return Conjunction.make(a, TemporalRules.ORDER_CONCURRENT, memory);
            case IMPLICATION:
                return Implication.make(a[0], a[1], memory);
            case EQUIVALENCE:
                return Equivalence.make(a[0], a[1], memory);            
        }
 
        throw new RuntimeException("Unknown Term operator: " + op + " (" + op.name() + ")");
    }    


    /**
     * build a component list from terms
     * @return the component list
     */
    protected static Term[] termArray(final Term... t) {
        return t;
    }
    protected static List<Term> termList(final Term... t) {
        return Arrays.asList(t);
    }
    
    

    /* ----- utilities for oldName ----- */
    /**
     * default method to make the oldName of the current term from existing
     * fields
     *
     * @return the oldName of the term
     */
    protected String makeName() {
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
        
        final StringBuilder nameBuilder = new StringBuilder(sizeEstimate)
            .append(COMPOUND_TERM_OPENER.ch).append(op.toString());
            
        for (final Term t : arg) {
            nameBuilder.append(Symbols.ARGUMENT_SEPARATOR);
            if (t instanceof CompoundTerm) {
                t.setName(((CompoundTerm) t).makeName());
            }
            nameBuilder.append(t.getName());
        }
        nameBuilder.append(COMPOUND_TERM_CLOSER.ch);
                
        return nameBuilder.toString();
    }

    /**
     * make the oldName of an ExtensionSet or IntensionSet
     *
     * @param opener the set opener
     * @param closer the set closer
     * @param arg the list of term
     * @return the oldName of the term
     */
    protected static String makeSetName(final char opener, final Term[] arg, final char closer) {
        final int sizeEstimate = 12 * arg.length + 2;
        
        StringBuilder name = new StringBuilder(sizeEstimate)
            .append(opener);

        if (arg.length == 0) { 
            //is empty arg valid?            
            //throw new RuntimeException("Empty arg list for makeSetName");            
        }
        else {
        
            name.append(arg[0].getName());

            for (int i = 1; i < arg.length; i++) {
                name.append(Symbols.ARGUMENT_SEPARATOR);
                name.append(arg[i].getName());
            }
        }
        
        name.append(closer);
        return name.toString();
    }

    /**
     * default method to make the oldName of an image term from given fields
     *
     * @param op the term operator
     * @param arg the list of term
     * @param relationIndex the location of the place holder
     * @return the oldName of the term
     */
    protected static String makeImageName(final NativeOperator op, final Term[] arg, final int relationIndex) {
        final int sizeEstimate = 12 * arg.length + 2;
        
        StringBuilder name = new StringBuilder(sizeEstimate)
            .append(COMPOUND_TERM_OPENER.ch)
            .append(op)
            .append(Symbols.ARGUMENT_SEPARATOR)
            .append(arg[relationIndex].getName());
        
        for (int i = 0; i < arg.length; i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            if (i == relationIndex) {
                name.append(Symbols.IMAGE_PLACE_HOLDER);
            } else {
                name.append(arg[i].getName());
            }
        }
        name.append(COMPOUND_TERM_CLOSER.ch);
        return name.toString();
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
        Set<Term> s = new HashSet();
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
    
    private Term[] cloneTermsExcept(boolean requireModification, final Term... toRemove) {
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
            if (t.isConstant())
                arr[i] = t;
            else
                arr[i] = ( deep ? (Term)t.clone() : t );

            //arr[i] = ( deep ? (Term)t.clone() : t );
        
        }
        for (int j = 0; j < additional.length; j++) {
            final Term t = additional[j];                    
            
            //experiental optimization
            if (t.isConstant())
                arr[i+j] = t;
            else            
                arr[i+j] = ( deep ? (Term)t.clone() : t );
            
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
            if (t.isConstant())
                l.add(t);
            else
                l.add( deep ? (Term)t.clone() : t );  
            
            //l.add( deep ? (Term)t.clone() : t );
        }
        return l;        
    }

    
    //TODO move this to a utility method
    public static <T> boolean contains(final T[] array, final T v) {
        /*if (v == null) {
            for (final T e : array)
                if (e == null)
                    return true;
        } else {*/
        
        for (final T e : array)
            if (v.equals(e))
                return true;

        return false;
    }

    
    static void shuffle(Term[] list, Random randomNumber) {
        int n = list.length;
        for (int i = 0; i < n; i++) {
            // between i and n-1
            int r = i + (int) (randomNumber.nextDouble() * (n-i));
            Term tmp = list[i];    // swap
            list[i] = list[r];
            list[r] = tmp;
        }
    }
    
    //TODO move this to a utility method
    public static <T> int indexOf(final T[] array, final T v) {
        /*if (v == null) {
            for (final T e : array)
                if (e == null)
                    return true;
        } else {*/
        
        int i = 0;
        for (final T e : array) {
            if (v.equals(e)) {
                return i;
            }
            i++;
        }

        return -1;
    }
    
    //TODO move this to a utility method
    public static <T> boolean containsAll(final T[] array, final T v) {
        for (final T e : array)
            if (!v.equals(e))
                return false;

        return true;
    }
    

    /**
     * Check whether the compound contains a certain component
     * Also matches variables, ex: (&&,<a --> b>,<b --> c>) also contains <a --> #1>
     * @param t The component to be checked
     * @return Whether the component is in the compound
     */
    @Override
    public boolean containsTerm(final Term t) {        
        return CompoundTerm.contains(term, t);
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
    public boolean containsAllTerms(final Term t) {
        if (t instanceof CompoundTerm) {
            return containsAll(term, ((CompoundTerm) t).term );
        } else {
            return contains(term, t);
        }
    }

    /**
     * Try to add a component into a compound
     *
     * @param t1 The compound
     * @param t2 The component
     * @param memory Reference to the memory
     * @return The new compound
     */
    public static Term addComponents(final CompoundTerm t1, final Term t2, final Memory memory) {
        if (t2 == null)
            return t1;
        
        boolean success;
        Term[] terms;
        if (t2 instanceof CompoundTerm) {
            terms = t1.cloneTerms(((CompoundTerm) t2).term);
        } else {
            terms = t1.cloneTerms(t2);
        }
        return make(t1, terms, memory);
    }


    /**
     * Try to remove a component from a compound
     *
     * @param t1 The compound
     * @param t2 The component
     * @param memory Reference to the memory
     * @return The new compound
     */
    public static Term reduceComponents(final CompoundTerm t1, final Term t2, final Memory memory) {
        final Term[] list;
        if (t1.getClass() == t2.getClass()) {
            //success = list.removeAll(((CompoundTerm) t2).term);
            list = t1.cloneTermsExcept(true, ((CompoundTerm) t2).term);
        } else {
            //success = list.remove(t2);
            list = t1.cloneTermsExcept(true, t2);
        }
        if (list!=null) {
            if (list.length > 1) {
                return make(t1, list, memory);
            }
            if (list.length == 1) {
                if ((t1 instanceof Conjunction) || (t1 instanceof Disjunction)
                        || (t1 instanceof IntersectionExt) || (t1 instanceof IntersectionInt)
                        || (t1 instanceof DifferenceExt) || (t1 instanceof DifferenceInt)) {
                    return list[0];
                }
            }
        }
        return null;
    }    

    /**
     * Try to replace a component in a compound at a given index by another one
     *
     * @param compound The compound
     * @param index The location of replacement
     * @param t The new component
     * @param memory Reference to the memory
     * @return The new compound
     */
    public static Term setComponent(final CompoundTerm compound, final int index, final Term t, final Memory memory) {
        List<Term> list = compound.cloneTermsList();
        list.remove(index);
        if (t != null) {
            if (compound.getClass() != t.getClass()) {
                list.add(index, t);
            } else {
                final List<Term> list2 = ((CompoundTerm) t).cloneTermsList();
                for (int i = 0; i < list2.size(); i++) {
                    list.add(index + i, list2.get(i));
                }
            }
        }
        return make(compound, list, memory);
    }

    /* ----- variable-related utilities ----- */
    /**
     * Whether this compound term contains any variable term
     *
     * @return Whether the name contains a variable
     */
    public boolean containVar() {
        return hasVar;
    }

    /**
     * Rename the variables in the compound, called from Sentence constructors
     */
    @Override
    public void renameVariables() {
        if (containVar()) {
            int existingComponents = term.length;
            renameVariables(new HashMap<Variable, Variable>());
        }
        isConstant = true;
        setName(makeName());
    }

    /**
     * Recursively rename the variables in the compound
     *
     * @param map The substitution established so far
     */
    private void renameVariables(final HashMap<Variable, Variable> map) {
        if (containVar()) {
            for (int i = 0; i < term.length; i++) {
                final Term term = this.term[i];
                if (term instanceof Variable) {
                    Variable var;                    
                    if (term.getName().length() == 1) { // anonymous variable from input
                        var = new Variable(term.getName().charAt(0) + String.valueOf(map.size() + 1));
                    } else {
                        var = map.get(term);
                        if (var == null) {
                            var = new Variable(term.getName().charAt(0) + String.valueOf(map.size() + 1));
                        }
                    }
                    if (!term.equals(var)) {
                        this.term[i] = var;
                    }
                    map.put((Variable) term, var);
                } else if (term instanceof CompoundTerm) {
                    CompoundTerm ct = (CompoundTerm)term;
                    ct.renameVariables(map);
                    ct.setName(ct.makeName());
                }
            }
        }
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
                term[i] = (Term) t2.clone();
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
    public List<TermLink> prepareComponentLinks() {
        //complexity seems like an upper bound for the resulting number of componentLinks. 
        //so use it as an initial size for the array list
        final List<TermLink> componentLinks = new ArrayList<>( getComplexity() );
        
        short type = (this instanceof Statement) ? TermLink.COMPOUND_STATEMENT : TermLink.COMPOUND;   // default
        prepareComponentLinks(componentLinks, type, this);
        return componentLinks;
    }

    /**
     * Collect TermLink templates into a list, go down one level except in
     * special cases
     * <p>
     *
     * @param componentLinks The list of TermLink templates built so far
     * @param type The type of TermLink to be built
     * @param term The CompoundTerm for which the links are built
     */
    private void prepareComponentLinks(final List<TermLink> componentLinks, final short type, final CompoundTerm t) {
        //Term t1, t2, t3 are term at different levels
        for (int i = 0; i < t.size(); i++) {     // first level term
           final Term t1 = t.term[i];
            if (t1.isConstant()) {
                componentLinks.add(new TermLink(type, t1, i));
            }
            if (((this instanceof Equivalence) || ((this instanceof Implication) && (i == 0))) && ((t1 instanceof Conjunction) || (t1 instanceof Negation))) {
                
                ((CompoundTerm) t1).prepareComponentLinks(componentLinks, TermLink.COMPOUND_CONDITION, (CompoundTerm) t1);

            } else if (t1 instanceof CompoundTerm) {
                for (int j = 0; j < ((CompoundTerm) t1).size(); j++) {  // second level term
                    final Term t2 = ((CompoundTerm) t1).term[j];
                    if (t2.isConstant()) {
                        if ((t1 instanceof Product) || (t1 instanceof ImageExt) || (t1 instanceof ImageInt)) {
                            if (type == TermLink.COMPOUND_CONDITION) {
                                componentLinks.add(new TermLink(TermLink.TRANSFORM, t2, 0, i, j));
                            } else {
                                componentLinks.add(new TermLink(TermLink.TRANSFORM, t2, i, j));
                            }
                        } else {
                            componentLinks.add(new TermLink(type, t2, i, j));
                        }
                    }
                    if ((t2 instanceof Product) || (t2 instanceof ImageExt) || (t2 instanceof ImageInt)) {
                        for (int k = 0; k < ((CompoundTerm) t2).size(); k++) {
                            final Term t3 = ((CompoundTerm) t2).term[k];
                            if (t3.isConstant()) {                           // third level
                                if (type == TermLink.COMPOUND_CONDITION) {
                                    componentLinks.add(new TermLink(TermLink.TRANSFORM, t3, 0, i, j, k));
                                } else {
                                    componentLinks.add(new TermLink(TermLink.TRANSFORM, t3, i, j, k));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    
    
    static boolean EqualSubjectPredicateInRespectToImageAndProduct(final Term a, final Term b) {
        if(a==null || b==null) {
            return false;
        }
        if(!(a instanceof Statement) && !(b instanceof Statement))
            return false;
        if(a.equals(b)) {
            return true;
        }
        Statement A=(Statement) a;
        Statement B=(Statement) b;
        if(A instanceof Similarity && B instanceof Similarity || A instanceof Inheritance && B instanceof Inheritance) {
            Term subjA = A.getSubject();
            Term predA = A.getPredicate();
            Term subjB = B.getSubject();
            Term predB = B.getPredicate();

            //ok we know they are not equal, its time to determine how image structure could make them equal:
            if(subjA instanceof Product) { //A is a product, so B must be a extensional image to make true
                if(predB instanceof ImageExt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    componentsA.add(predA);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)subjA).term));
                    Set<Term> componentsB=new HashSet();
                    componentsB.add(subjB);
                    componentsB.addAll(Arrays.asList(((CompoundTerm)predB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            
            if(subjB instanceof Product) { //B is a product, so A must be a extensional image to make true
                if(predA instanceof ImageExt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    componentsA.add(subjA);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)predA).term));
                    Set<Term> componentsB=new HashSet();
                    componentsB.add(predB);
                    componentsB.addAll(Arrays.asList(((CompoundTerm)subjB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            
            if(predA instanceof ImageExt) { //A is a extensional image, so B must be a extensional image to make true
                if(predB instanceof ImageExt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    Set<Term> componentsB=new HashSet();
                    componentsA.add(subjA);
                    componentsB.add(subjB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)predA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm)predB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            
            if(predA instanceof ImageExt) { //A is a extensional image, so B must be a extensional image to make true
                if(predB instanceof ImageExt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    Set<Term> componentsB=new HashSet();
                    componentsA.add(subjA);
                    componentsB.add(subjB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)predA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm)predB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            
            if(subjA instanceof ImageInt) { //A is a intensional image, so B must be a intensional image to make true
                if(subjB instanceof ImageInt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    Set<Term> componentsB=new HashSet();
                    componentsA.add(predA);
                    componentsB.add(predB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)subjA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm)subjB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            
            if(subjA instanceof ImageInt) { //A is a intensional image, so B must be a intensional image to make true
                if(subjB instanceof ImageInt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    Set<Term> componentsB=new HashSet();
                    componentsA.add(predA);
                    componentsB.add(predB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)subjA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm)subjB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            
            if(predA instanceof Product) { //A is a product, so B must be a intensional image to make true
                if(subjB instanceof ImageInt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    Set<Term> componentsB=new HashSet();
                    componentsA.add(subjA);
                    componentsB.add(predB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)predA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm)subjB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
            
            if(predB instanceof Product) { //A is a product, so B must be a intensional image to make true
                if(subjA instanceof ImageInt) {
                    //now the term of both statements need to be the same:
                    Set<Term> componentsA=new HashSet();
                    Set<Term> componentsB=new HashSet();
                    componentsA.add(predA);
                    componentsB.add(subjB);
                    componentsA.addAll(Arrays.asList(((CompoundTerm)subjA).term));
                    componentsB.addAll(Arrays.asList(((CompoundTerm)predB).term));
                    if(componentsA.containsAll(componentsB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean EqualSubTermsInRespectToImageAndProduct(final Term a, final Term b) {
        if(a==null || b==null) {
            return false;
        }
        if(!((a instanceof CompoundTerm) && (b instanceof CompoundTerm))) {
            return a.equals(b);
        }
        if(a instanceof Inheritance && b instanceof Inheritance) {
            return EqualSubjectPredicateInRespectToImageAndProduct(a,b);
        }
        if(a instanceof Similarity && b instanceof Similarity) {
            return EqualSubjectPredicateInRespectToImageAndProduct(a,b) || EqualSubjectPredicateInRespectToImageAndProduct(b,a);
        }
        Term[] A=((CompoundTerm) a).term;
        Term[] B=((CompoundTerm) b).term;
        if(A.length != B.length) {
            return false;
        }
        else {
            for(int i=0;i<A.length;i++) {
                Term x = A[i];
                Term y = B[i];
                if(!x.equals(y)) {
                    if(x instanceof Inheritance && y instanceof Inheritance) {
                        if(!EqualSubjectPredicateInRespectToImageAndProduct(x,y)) {
                            return false;
                        }
                        else {
                            continue;
                        }
                    }
                    if(x instanceof Similarity && y instanceof Similarity) {
                        if(!EqualSubjectPredicateInRespectToImageAndProduct(x,y) && !EqualSubjectPredicateInRespectToImageAndProduct(y,x)) {
                            return false;
                        }
                        else {
                            continue;
                        }
                    }
                    return false;
                }
            }
            return true;
        }
    }
    
    //3 helper functions for dedSecondLayerVariableUnification:
    public static Term unwrapNegation(Term T) { //negation is not counting as depth
        if(T!=null && T instanceof Negation)
            return ((CompoundTerm)T).term[0];
        return T;
    }
    
    public static Term reduceComponentOneLayer(CompoundTerm t1, Term t2, Memory memory) {
        Term[] list;
        if (t1.getClass() == t2.getClass()) {
            list = t1.cloneTermsExcept(true, ((CompoundTerm) t2).term);
        } else {
            list = t1.cloneTermsExcept(true, t2);
        }
        if (list!=null) {
            if (list.length > 1) {
                return make(t1, list, memory);
            }
            else if (list.length == 1) {
                if (t1 instanceof CompoundTerm) {
                    return list[0];
                }
            }
        }
        return t1;
    }
    public static CompoundTerm ReduceTillLayer2(CompoundTerm itself, Term replacement, Memory memory)
    {
       if(!(itself instanceof CompoundTerm)) {
           return null;
       }
       itself=(CompoundTerm) reduceComponentOneLayer(itself, replacement, memory);
       int j=0;
       for(Term t : itself.term) {
           Term t2 = unwrapNegation(t);
            if(!(t2 instanceof Implication) && !(t2 instanceof Equivalence) && !(t2 instanceof Conjunction) && !(t2 instanceof Disjunction)) {
                j++;
                continue;
            }
            Term ret2=reduceComponentOneLayer((CompoundTerm) t2,replacement,memory);
            CompoundTerm replaced=(CompoundTerm) CompoundTerm.setComponent((CompoundTerm) itself, j, ret2, memory);
            if(replaced!=null) {
                itself=replaced;
            }
           j++;
       }
       return itself;
    }

    
    /*
    @Deprecated public static Term make(final String op, final ArrayList<Term> arg, final Memory memory) {
        final int length = op.length();
        if (length == 1) {
            final char c = op.charAt(0);
            switch (c) {
                case Symbols.SET_EXT_OPENER: 
                    return SetExt.make(arg, memory);
                case Symbols.SET_INT_OPENER: 
                    return SetInt.make(arg, memory);
                case Symbols.INTERSECTION_EXT_OPERATORc: 
                    return IntersectionExt.make(arg, memory);
                case Symbols.INTERSECTION_INT_OPERATORc:
                    return IntersectionInt.make(arg, memory);
                case Symbols.DIFFERENCE_EXT_OPERATORc:
                    return DifferenceExt.make(arg, memory);
                case Symbols.DIFFERENCE_INT_OPERATORc:
                    return DifferenceInt.make(arg, memory);
                case Symbols.PRODUCT_OPERATORc:
                    return Product.make(arg, memory);
                case Symbols.IMAGE_EXT_OPERATORc:
                    return ImageExt.make(arg, memory);
                case Symbols.IMAGE_INT_OPERATORc:
                    return ImageInt.make(arg, memory);                    
            }            
        }
        else if (length == 2) {
            //since these symbols are the same character repeated, we only need to compare the first character
            final char c1 = op.charAt(0);
            final char c2 = op.charAt(1);
            if (c1 == c2) {
                switch (c1) {
                    case Symbols.NEGATION_OPERATORc:
                        return Negation.make(arg, memory);            
                    case Symbols.DISJUNCTION_OPERATORc:
                        return Disjunction.make(arg, memory);            
                    case Symbols.CONJUNCTION_OPERATORc:
                        return Conjunction.make(arg, memory);
                }            
            } else if (op.equals(Symbols.SEQUENCE_OPERATOR)) {
                return Conjunction.make(arg, TemporalRules.ORDER_FORWARD, memory);
            } else if (op.equals(Symbols.PARALLEL_OPERATOR)) {
                return Conjunction.make(arg, TemporalRules.ORDER_CONCURRENT, memory);
            }
        }
        throw new RuntimeException("Unknown Term operator: " + op);
    }
    */


}
