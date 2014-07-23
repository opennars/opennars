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

import java.util.*;

import nars.entity.*;
import nars.storage.*;
import nars.io.Symbols;
import nars.io.Symbols.Operator;
import nars.inference.TemporalRules;
import static nars.language.CompoundTerm.make;
import static nars.language.CompoundTerm.makeCompoundName;

/**
 * A CompoundTerm is a Term with internal (syntactic) structure
 * <p>
 * A CompoundTerm consists of a term operator with one or more component Terms.
 * <p>
 * This abstract class contains default methods for all CompoundTerms.
 */
public abstract class CompoundTerm extends Term {

    /**
     * list of (direct) components
     */
    public final ArrayList<Term> components;
    /**
     * syntactic complexity of the compound, the sum of those of its components
     * plus 1
     */
    protected short complexity;
    /**
     * Whether the term names a concept
     */
    protected boolean isConstant = true;

//    public static final int ORDER_FORWARD = 1;
//    public static final int ORDER_BACKWARD = -1;
//    public static final int ORDER_CONCURRENT = 0;
//    public static final int ORDER_NONE = Integer.MIN_VALUE;
    /* ----- abstract methods to be implemented in subclasses ----- */

    /**
     * Abstract method to get the operator of the compound
     */
    public abstract Operator operator();

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
    protected CompoundTerm(final String name, final ArrayList<Term> components, final boolean isConstant, final short complexity) {
        super(name);
        this.components = components;
        this.isConstant = isConstant;
        this.complexity = complexity;
    }

    /**
     * Default constructor
     */
    protected CompoundTerm() {
        this.components = null;
    }

    /**
     * Constructor called from subclasses constructors to initialize the fields
     *
     * @param components Component list
     */
    protected CompoundTerm(final ArrayList<Term> components) {
        this.components = components;
        calcComplexity();
        setName(makeName());
        isConstant = !Variable.containVar(name);
    }

    
    /**
     * Constructor called from subclasses constructors to initialize the fields
     *
     * @param name Name of the compound
     * @param components Component list
     */
    protected CompoundTerm(final String name, final ArrayList<Term> components) {
        this.components = components;
        calcComplexity();
        setName(name);
        isConstant = !Variable.containVar(name);
    }

    /**
     * Change the oldName of a CompoundTerm, called after variable substitution
     *
     * @param s The new oldName
     */
    @Override
    protected void setName(String s) {
        name = s;
    }

    /**
     * The complexity of the term is the sum of those of the components plus 1
     */
    private void calcComplexity() {
        complexity = 1;
        for (int i = 0; i < components.size(); i++) {
            complexity += components.get(i).getComplexity();
        }
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
        if (!(that instanceof CompoundTerm))
            return false;
        
        final CompoundTerm t = (CompoundTerm)that;    
                
        if (operator() != t.operator())
            return false;
        
        if (size() != t.size())
            return false;
        
        for (int i = 0; i < components.size(); i++) {
            final Term c = components.get(i);
            if (!c.equals(t.componentAt(i)))
                return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.components);
        hash = 43 * hash + operator().hashCode();
        return hash;
    }
        

    /**
     * Orders among terms: variable < atomic < compound
     *
     * @param that The Term to be compared with the current Term
     * @return The same as compareTo as defined on Strings
     * @return The order of the two terms
     */
    @Override
    public int compareTo(final Term that) {
        if (that instanceof CompoundTerm) {
            final CompoundTerm t = (CompoundTerm) that;
            if (size() == t.size()) {
                int opDiff = this.operator().ordinal() - t.operator().ordinal(); //should be faster faster than Enum.compareTo                
                if (opDiff != 0) {
                    return opDiff;
                }

                for (int i = 0; i < components.size(); i++) {
                    final Term c = components.get(i);
                    final int diff = c.compareTo(t.componentAt(i));
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
     * Try to make a compound term from a template and a list of components
     *
     * @param compound The template
     * @param components The components
     * @param memory Reference to the memory
     * @return A compound term or null
     */
    public static Term make(final CompoundTerm compound, final ArrayList<Term> components, final Memory memory) {
        if (compound instanceof ImageExt) {
            return ImageExt.make(components, ((ImageExt) compound).getRelationIndex(), memory);
        } else if (compound instanceof ImageInt) {
            return ImageInt.make(components, ((ImageInt) compound).getRelationIndex(), memory);
        } else {
            return make(compound.operator(), components, memory);
        }
    }

    /**
     * Try to make a compound term from an operator and a list of components
     * <p>
     * Called from StringParser
     *
     * @param op Term operator
     * @param arg Component list
     * @param memory Reference to the memory
     * @return A compound term or null
     */    
    public static Term make(final Operator op, final ArrayList<Term> arg, final Memory memory) {
        switch (op) {
            case SET_EXT_OPENER: 
                return SetExt.make(arg, memory);
            case SET_INT_OPENER: 
                return SetInt.make(arg, memory);
            case INTERSECTION_EXT: 
                return IntersectionExt.make(arg, memory);
            case INTERSECTION_INT:
                return IntersectionInt.make(arg, memory);
            case DIFFERENCE_EXT:
                return DifferenceExt.make(arg, memory);
            case DIFFERENCE_INT:
                return DifferenceInt.make(arg, memory);
            case PRODUCT:
                return Product.make(arg, memory);
            case IMAGE_EXT:
                return ImageExt.make(arg, memory);
            case IMAGE_INT:
                return ImageInt.make(arg, memory);                    
            case NEGATION:
                return Negation.make(arg, memory);            
            case DISJUNCTION:
                return Disjunction.make(arg, memory);            
            case CONJUNCTION:
                return Conjunction.make(arg, memory);
            case SEQUENCE:
                return Conjunction.make(arg, TemporalRules.ORDER_FORWARD, memory);
            case PARALLEL:
                return Conjunction.make(arg, TemporalRules.ORDER_CONCURRENT, memory);
            default:
                throw new RuntimeException("Unknown Term operator: " + op);
        }
    }    

    /**
     * Check CompoundTerm operator symbol
     *
     * @return if the given String is an operator symbol
     * @param s The String to be checked
     */
    public static boolean isOperator(final String op) {               
        final int length = op.length();
        if (length == 1) {
            final char c = op.charAt(0);
            switch (c) {
                case Symbols.SET_EXT_OPENER: 
                case Symbols.SET_INT_OPENER: 
                case Symbols.INTERSECTION_EXT_OPERATORc: 
                case Symbols.INTERSECTION_INT_OPERATORc:
                case Symbols.DIFFERENCE_EXT_OPERATORc:
                case Symbols.DIFFERENCE_INT_OPERATORc:
                case Symbols.PRODUCT_OPERATORc:
                case Symbols.IMAGE_EXT_OPERATORc:
                case Symbols.IMAGE_INT_OPERATORc:
                    return true;
            }            
        }
        else if (length == 2) {
            //since these symbols are the same character repeated, we only need to compare the first character
            final char c1 = op.charAt(0);
            final char c2 = op.charAt(1);
            if (c1 == c2) {
                switch (c1) {
                    case Symbols.NEGATION_OPERATORc:
                    case Symbols.DISJUNCTION_OPERATORc:
                    case Symbols.CONJUNCTION_OPERATORc:
                        return true;                        
                }            
            } else if ((op.equals(Symbols.SEQUENCE_OPERATOR)) || (op.equals(Symbols.PARALLEL_OPERATOR))) {
                return true;
            }
        }        
        
        return false;
    }

    /**
     * build a component list from two terms
     *
     * @param t1 the first component
     * @param t2 the second component
     * @return the component list
     */
    protected static ArrayList<Term> argumentsToList(final Term t1, final Term t2) {
        final ArrayList<Term> list = new ArrayList<>(2);
        list.add(t1);
        list.add(t2);
        return list;
    }

    /* ----- utilities for oldName ----- */
    /**
     * default method to make the oldName of the current term from existing
     * fields
     *
     * @return the oldName of the term
     */
    protected String makeName() {
        return makeCompoundName(operator(), components);
    }

    /**
     * default method to make the oldName of a compound term from given fields
     *
     * @param op the term operator
     * @param arg the list of components
     * @return the oldName of the term
     */
    protected static String makeCompoundName(final Operator op, final ArrayList<Term> arg) {
        final StringBuilder nameBuilder = new StringBuilder(16  /* estimate */)
            .append(Symbols.COMPOUND_TERM_OPENER).append(op.toString());
        for (final Term t : arg) {
            nameBuilder.append(Symbols.ARGUMENT_SEPARATOR);
            if (t instanceof CompoundTerm) {
                ((CompoundTerm) t).setName(((CompoundTerm) t).makeName());
            }
            nameBuilder.append(t.getName());
        }
        nameBuilder.append(Symbols.COMPOUND_TERM_CLOSER);
        return nameBuilder.toString();
    }

    /**
     * make the oldName of an ExtensionSet or IntensionSet
     *
     * @param opener the set opener
     * @param closer the set closer
     * @param arg the list of components
     * @return the oldName of the term
     */
    protected static String makeSetName(final char opener, final ArrayList<Term> arg, final char closer) {
        StringBuilder name = new StringBuilder(16 /* estimate */)
            .append(opener);

        if (arg.size() == 0) { 
            //is empty arg valid?            
            throw new RuntimeException("Empty arg list for makeSetName");
        }
        
        name.append(arg.get(0).getName());
        
        for (int i = 1; i < arg.size(); i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            name.append(arg.get(i).getName());
        }
        name.append(closer);
        return name.toString();
    }

    /**
     * default method to make the oldName of an image term from given fields
     *
     * @param op the term operator
     * @param arg the list of components
     * @param relationIndex the location of the place holder
     * @return the oldName of the term
     */
    protected static String makeImageName(final Operator op, final ArrayList<Term> arg, final int relationIndex) {
        StringBuilder name = new StringBuilder(16 /* estimate */)
        .append(Symbols.COMPOUND_TERM_OPENER)
        .append(op)
        .append(Symbols.ARGUMENT_SEPARATOR)
        .append(arg.get(relationIndex).getName());
        
        for (int i = 0; i < arg.size(); i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            if (i == relationIndex) {
                name.append(Symbols.IMAGE_PLACE_HOLDER);
            } else {
                name.append(arg.get(i).getName());
            }
        }
        name.append(Symbols.COMPOUND_TERM_CLOSER);
        return name.toString();
    }

    /* ----- utilities for other fields ----- */
    /**
     * report the term's syntactic complexity
     *
     * @return the complexity value
     */
    @Override
    public int getComplexity() {
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
     * Set the constant status
     *
     * @param isConstant
     */
    public void setConstant(final boolean isConstant) {
        this.isConstant = isConstant;
    }

    /**
     * Check if the order of the components matters
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
     * get the number of components
     *
     * @return the size of the component list
     */
    public int size() {
        return components.size();
    }

    /**
     * get a component by index
     *
     * @param i index of the component
     * @return the component
     */
    public Term componentAt(final int i) {
        return components.get(i);
    }

    /**
     * Get the component list
     *
     * @return The component list
     */
    public ArrayList<Term> getComponents() {
        return components;
    }
    
    /** Gives a set of all contained components, recursively */
    public Set<Term> getContainedTerms() {
        Set<Term> s = new HashSet();
        for (Term t : components) {
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
    public ArrayList<Term> cloneComponents() {
        return cloneList(components);
    }

    /**
     * Deep clone an array list of terms
     *
     * @param original The original component list
     * @return an identical and separate copy of the list
     */
    public static ArrayList<Term> cloneList(final ArrayList<Term> original) {
        if (original == null) {
            return null;
        }
        final int osize = original.size();
        
        final ArrayList<Term> arr = new ArrayList(osize);        
        for (int i = 0; i < osize; i++) {
            Term original1 = original.get(i);
            arr.add((Term) (original1.clone()));
        }
        return arr;
    }

    /**
     * Check whether the compound contains a certain component
     * Also matches variables, ex: (&&,<a --> b>,<b --> c>) also contains <a --> #1>
     * @param t The component to be checked
     * @return Whether the component is in the compound
     */
    @Override
    public boolean containComponent(final Term t) {
        return components.contains(t);
    }

    /**
     * Recursively check if a compound contains a term
     *
     * @param target The term to be searched
     * @return Whether the target is in the current term
     */
    @Override
    public boolean containTerm(final Term target) {
        for (final Term term : components) {
            if (term.containTerm(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the compound contains all components of another term, or
     * that term as a whole
     *
     * @param t The other term
     * @return Whether the components are all in the compound
     */
    public boolean containAllComponents(final Term t) {
        if (getClass() == t.getClass()) {
            return components.containsAll(((CompoundTerm) t).getComponents());
        } else {
            return components.contains(t);
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
        if (t2 == null) {
            return t1;
        }
        boolean success;
        final ArrayList<Term> list = t1.cloneComponents();
        if (t1.getClass() == t2.getClass()) {
            success = list.addAll(((CompoundTerm) t2).getComponents());
        } else {
            success = list.add(t2);
        }
        return (success ? make(t1, list, memory) : null);
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
        boolean success;
        final ArrayList<Term> list = t1.cloneComponents();
        if (t1.getClass() == t2.getClass()) {
            success = list.removeAll(((CompoundTerm) t2).getComponents());
        } else {
            success = list.remove(t2);
        }
        if (success) {
            if (list.size() > 1) {
                return make(t1, list, memory);
            }
            if (list.size() == 1) {
                if ((t1 instanceof Conjunction) || (t1 instanceof Disjunction)
                        || (t1 instanceof IntersectionExt) || (t1 instanceof IntersectionInt)
                        || (t1 instanceof DifferenceExt) || (t1 instanceof DifferenceInt)) {
                    return list.get(0);
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
        final ArrayList<Term> list = compound.cloneComponents();
        list.remove(index);
        if (t != null) {
            if (compound.getClass() != t.getClass()) {
                list.add(index, t);
            } else {
                final ArrayList<Term> list2 = ((CompoundTerm) t).cloneComponents();
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
        return Variable.containVar(name);
    }

    /**
     * Rename the variables in the compound, called from Sentence constructors
     */
    @Override
    public void renameVariables() {
        if (containVar()) {
            renameVariables(new HashMap<Variable, Variable>());
        }
        setConstant(true);
        setName(makeName());
    }

    /**
     * Recursively rename the variables in the compound
     *
     * @param map The substitution established so far
     */
    private void renameVariables(final HashMap<Variable, Variable> map) {
        if (containVar()) {
            for (int i = 0; i < components.size(); i++) {
                final Term term = components.get(i);
                if (term instanceof Variable) {
                    Variable var;                    
                    if (term.getName().length() == 1) { // anonymous variable from input
                        var = new Variable(term.getName().charAt(0) + String.valueOf(map.size() + 1));
                    } else {
                        var = (Variable) map.get((Variable) term);
                        if (var == null) {
                            var = new Variable(term.getName().charAt(0) + String.valueOf(map.size() + 1));
                        }
                    }
                    if (!term.equals(var)) {
                        components.set(i, var);
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
        int i = 0;
        for (final Term t1 : components) {
            if (subs.containsKey(t1)) {
                Term t2 = subs.get(t1);                            
                while (subs.containsKey(t2)) {
                    t2 = subs.get(t2);
                }
                components.set(i, (Term) t2.clone());
            } else if (t1 instanceof CompoundTerm) {
                ((CompoundTerm) t1).applySubstitute(subs);
            }
            i++;
        }
        if (this.isCommutative()) {         
            // re-order
            components.clear();
            components.addAll(new TreeSet<>(components));
        }
        name = makeName();
    }

    /* ----- link CompoundTerm and its components ----- */
    /**
     * Build TermLink templates to constant components and subcomponents
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
    private void prepareComponentLinks(final List<TermLink> componentLinks, final short type, final CompoundTerm term) {
        //Term t1, t2, t3 are components at different levels
        for (int i = 0; i < term.size(); i++) {     // first level components
           final Term t1 = term.componentAt(i);
            if (t1.isConstant()) {
                componentLinks.add(new TermLink(type, t1, i));
            }
            if (((this instanceof Equivalence) || ((this instanceof Implication) && (i == 0))) && ((t1 instanceof Conjunction) || (t1 instanceof Negation))) {
                
                ((CompoundTerm) t1).prepareComponentLinks(componentLinks, TermLink.COMPOUND_CONDITION, (CompoundTerm) t1);

            } else if (t1 instanceof CompoundTerm) {
                for (int j = 0; j < ((CompoundTerm) t1).size(); j++) {  // second level components
                    final Term t2 = ((CompoundTerm) t1).componentAt(j);
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
                            final Term t3 = ((CompoundTerm) t2).componentAt(k);
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
