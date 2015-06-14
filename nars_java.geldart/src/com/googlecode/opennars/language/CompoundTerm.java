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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.opennars.language;

import java.util.*;

import com.googlecode.opennars.entity.TermLink;
import com.googlecode.opennars.main.Memory;
import com.googlecode.opennars.parser.Symbols;

/**
 * A Term with internal structure
 * <p>
 * Formed by a term operator with one or more component Terms.
 * <p>
 * This abstract class contains default methods for all CompoundTerms.
 */
public abstract class CompoundTerm extends Term {
    
    /**
     * list of (direct) components
     */
    protected ArrayList<Term> components;
    
    /**
     * list of open variables in the compound
     */
    protected ArrayList<Variable> openVariables;    // always necessary?
    
    /**
     * list of closed variables in the compound
     */
    protected ArrayList<Variable> closedVariables;    // remove???
    
    /**
     * syntactic complexity of the compound, which is the sum of those of its components plus 1
     */
    protected short complexity;
    
    
    // abstract methods
    public abstract Object clone();
    public abstract String operator();
    
    /* ----- object builders, called from subclasses ----- */
    
    /**
     * default constructor
     */
    public CompoundTerm() {}
    
    /**
     * constructor called from subclasses constructors to clone the fields
     */
    protected CompoundTerm(String n, ArrayList<Term> cs, ArrayList<Variable> open, ArrayList<Variable> closed, short i) {
        name = n;
        components = cs;
        openVariables = open;
        closedVariables = closed;
        complexity = i;
    }
    
    /**
     * constructor called from subclasses constructors to initialize the fields
     */
    protected CompoundTerm(String n, ArrayList<Term> arg) {
        components = arg;
        calcComplexity();
        markVariables();    // set in components, not in this
        name = makeName();
    }
    
    /**
     * the complexity of the term is the sum of those of the components plus 1
     */
    private void calcComplexity() {
        Term t;
        complexity = 1;
        for (int i = 0; i < components.size(); i++) {
            t = components.get(i);
            complexity += t.getComplexity();
        }
    }
    
    /**
     * check CompoundTerm operator symbol
     * @return if the given String is an operator symbol
     * @param s The String to be checked
     */
    public static boolean isOperator(String s, Memory memory) {
        if (s.length() == 1)
            return (s.equals(Symbols.INTERSECTION_EXT_OPERATOR) ||
                    s.equals(Symbols.INTERSECTION_INT_OPERATOR) ||
                    s.equals(Symbols.DIFFERENCE_EXT_OPERATOR) ||
                    s.equals(Symbols.DIFFERENCE_INT_OPERATOR) ||
                    s.equals(Symbols.PRODUCT_OPERATOR) ||
                    s.equals(Symbols.IMAGE_EXT_OPERATOR) ||
                    s.equals(Symbols.IMAGE_INT_OPERATOR));
        if (s.length() == 2)
            return (s.equals(Symbols.NEGATION_OPERATOR) ||
                    s.equals(Symbols.DISJUNCTION_OPERATOR) ||
                    s.equals(Symbols.CONJUNCTION_OPERATOR) ||
                    s.equals(Symbols.SEQUENCE_OPERATOR) ||
                    s.equals(Symbols.PARALLEL_OPERATOR) ||
                    s.equals(Symbols.PAST_OPERATOR) ||
                    s.equals(Symbols.PRESENT_OPERATOR) ||
                    s.equals(Symbols.FUTURE_OPERATOR));
        return isBuiltInOperator(s, memory); // s.length() > 2
    }
        
    public static Term make(String op, ArrayList arg, Memory memory) {
        if (op.length() == 1) {
            if (op.equals(Symbols.INTERSECTION_EXT_OPERATOR))
                return IntersectionExt.make(arg, memory);
            if (op.equals(Symbols.INTERSECTION_INT_OPERATOR))
                return IntersectionInt.make(arg, memory);
            if (op.equals(Symbols.DIFFERENCE_EXT_OPERATOR))
                return DifferenceExt.make(arg, memory);
            if (op.equals(Symbols.DIFFERENCE_INT_OPERATOR))
                return DifferenceInt.make(arg, memory);
            if (op.equals(Symbols.PRODUCT_OPERATOR))
                return Product.make(arg, memory);
            if (op.equals(Symbols.IMAGE_EXT_OPERATOR))
                return ImageExt.make(arg, memory);
            if (op.equals(Symbols.IMAGE_INT_OPERATOR))
                return ImageInt.make(arg, memory);
        }
        if (op.length() == 2) {
            if (op.equals(Symbols.NEGATION_OPERATOR))
                return Negation.make(arg, memory);
            if (op.equals(Symbols.DISJUNCTION_OPERATOR))
                return Disjunction.make(arg, memory);
            if (op.equals(Symbols.CONJUNCTION_OPERATOR))
                return Conjunction.make(arg, memory);
            if (op.equals(Symbols.SEQUENCE_OPERATOR))
                return ConjunctionSequence.make(arg, memory);
            if (op.equals(Symbols.PARALLEL_OPERATOR))
                return ConjunctionSequence.make(arg, memory);
            if (op.equals(Symbols.FUTURE_OPERATOR))
                return TenseFuture.make(arg, memory);
            if (op.equals(Symbols.PRESENT_OPERATOR))
                return TensePresent.make(arg, memory);
            if (op.equals(Symbols.PAST_OPERATOR))
                return TensePast.make(arg, memory);
        }
        if (isBuiltInOperator(op, memory)) {
            // t = Operator.make(op, arg);
            Term sub = Product.make(arg, memory);
            Term pre = memory.nameToOperator(op);
            return Inheritance.make(sub, pre, memory);
        }
        return null;
    }

    /**
     * check built-in operator name
     * @return if the given String is an operator name
     * @param s The String to be checked
     */
    private static boolean isBuiltInOperator(String s, Memory memory) {
        return (s.charAt(0) == Symbols.OPERATOR_TAG) && (memory.nameToOperator(s) != null);
    }
    
    public static Term make(CompoundTerm compound, ArrayList<Term> components, Memory memory) {
        if (compound instanceof SetExt)
            return SetExt.make(components, memory);
        if (compound instanceof SetInt)
            return SetInt.make(components, memory);
        if (compound instanceof IntersectionExt)
            return IntersectionExt.make(components, memory);
        if (compound instanceof IntersectionInt)
            return IntersectionInt.make(components, memory);
        if (compound instanceof DifferenceExt)
            return DifferenceExt.make(components, memory);
        if (compound instanceof DifferenceInt)
            return DifferenceInt.make(components, memory);
        if (compound instanceof Product)
            return Product.make(components, memory);
        if (compound instanceof ImageExt)
            return ImageExt.make(components, ((ImageExt) compound).getRelationIndex(), memory);
        if (compound instanceof ImageInt)
            return ImageInt.make(components, ((ImageInt) compound).getRelationIndex(), memory);
        if (compound instanceof Disjunction)
            return Disjunction.make(components, memory);
        if (compound instanceof ConjunctionSequence)
            return ConjunctionSequence.make(components, memory);
        if (compound instanceof ConjunctionParallel)
            return ConjunctionParallel.make(components, memory);
        if (compound instanceof Conjunction)
            return Conjunction.make(components, memory);
        return null;
    }
    
    /* ----- utilities for name ----- */
    
    /**
     * default method to make the name of the current term from existing fields
     * @return the name of the term
     */
    protected String makeName() {
        return makeCompoundName(operator(), components);
    }
    
    /**
     * default method to make the name of a compound term from given fields
     * @param op the term operator
     * @param arg the list of components
     * @return the name of the term
     */
    protected static String makeCompoundName(String op, ArrayList<Term> arg) {
        StringBuffer name = new StringBuffer();
        name.append(Symbols.COMPOUND_TERM_OPENER);      // also show closed variables???
        name.append(op);
        for (int i = 0; i < arg.size(); i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            name.append(arg.get(i).getName());
        }
        name.append(Symbols.COMPOUND_TERM_CLOSER);
        return name.toString();
    }
    
    /**
     * make the name of an ExtensionSet or IntensionSet
     * @param opener the set opener
     * @param closer the set closer
     * @param arg the list of components
     * @return the name of the term
     */
    protected static String makeSetName(char opener, ArrayList<Term> arg, char closer) {
        StringBuffer name = new StringBuffer();
        name.append(opener);
        name.append(arg.get(0).toString());
        for (int i = 1; i < arg.size(); i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            name.append(arg.get(i).getName());
        }
        name.append(closer);
        return name.toString();
    }
    
    /**
     * default method to make the name of an image term from given fields
     * @param op the term operator
     * @param arg the list of components
     * @param relationIndex the location of the place holder
     * @return the name of the term
     */
    protected static String makeImageName(String op, ArrayList<Term> arg, int relationIndex) {
        StringBuffer name = new StringBuffer();
        name.append(Symbols.COMPOUND_TERM_OPENER);
        name.append(op);
        name.append(Symbols.ARGUMENT_SEPARATOR);
        name.append(arg.get(relationIndex).getName());
        for (int i = 0; i < arg.size(); i++) {
            name.append(Symbols.ARGUMENT_SEPARATOR);
            if (i == relationIndex)
                name.append(Symbols.IMAGE_PLACE_HOLDER);
            else
                name.append(arg.get(i).getName());
        }
        name.append(Symbols.COMPOUND_TERM_CLOSER);
        return name.toString();
    }
    
    /**
     * skip all variable names to produce stable sorting order among components, not for display
     * @return the constant part of the term name
     */
    public String getConstantName() {
        StringBuffer s = new StringBuffer();
        s.append(Symbols.COMPOUND_TERM_OPENER);
        s.append(operator());
        s.append(Symbols.ARGUMENT_SEPARATOR);
        for (int i = 0; i < components.size(); i++) {
            s.append(components.get(i).getConstantName());
            s.append(Symbols.ARGUMENT_SEPARATOR);
        }
        s.append(Symbols.COMPOUND_TERM_CLOSER);
        return s.toString();
    }
    
    /* ----- utilities for other fields ----- */
    
    /**
     * report the term's syntactic complexity
     * @return the comlexity value
     */
    public int getComplexity() {
        return complexity;
    }
    
    /**
     * check if the term contains free variable
     * @return if the term is a constant
     */
    public boolean isConstant() {
        return (openVariables == null);
    }
    
    /**
     * check if the order of the components matters
     * <p>
     * commutative CompoundTerms: Sets, Intersections;
     * communative Statements: Similarity, Equivalence;
     * communative CompoundStatements: Disjunction, Conjunction
     * @return if the term is a communitive
     */
    public boolean isCommutative() {
        return false;
    }
        
    /**
     * build a component list from two terms
     * @param t1 the first component
     * @param t2 the second component
     * @return the component list
     */
    protected static ArrayList<Term> argumentsToList(Term t1, Term t2) {
        ArrayList<Term> list = new ArrayList<Term>(2);
        list.add(t1);
        list.add(t2);
        return list;
    }
    
    /* ----- extend Collection methods to component list ----- */
    
    /**
     * get the number of components
     * @return the size of the component list
     */
    public int size() {
        return components.size();
    }
    
    /**
     * get a component by index
     * @param i index of the component
     * @return the component
     */
    public Term componentAt(int i) {
        return components.get(i);
    }
    
    public ArrayList<Term> getComponents() {
        return components;
    }
    
    public ArrayList<Term> cloneComponents() {
        return (ArrayList<Term>) cloneList(components);
    }
    
    /**
     * deep clone an array list
     * @return an identical and separate copy of the list
     */
    public static ArrayList cloneList(ArrayList original) {
        if (original == null)
            return null;
        ArrayList arr = new ArrayList(original.size());
        for (int i = 0; i < original.size(); i++)
            arr.add(((Term) original.get(i)).clone());
        return arr;
    }
    
    public boolean containComponent(Term t) {
        return components.contains(t);
    }
    
    public boolean containAllComponents(Term t) {
        if (getClass() == t.getClass())
            return components.containsAll(((CompoundTerm) t).components);
        else
            return components.contains(t);
    }
    
    /* ----- clone/modify methods ----- */
    
    public static Term reduceComponents(CompoundTerm t1, Term t2, Memory memory) {
        boolean done;
        ArrayList<Term> list = t1.cloneComponents();
        if (t1.getClass() == t2.getClass())
            done = list.removeAll(((CompoundTerm) t2).components);
        else
            done = list.remove(t2);
        return (done ? make(t1, list, memory) : null);
    }

    public static Term replaceComponent(CompoundTerm compound, int index, Term t, Memory memory) {
        ArrayList<Term> list = compound.cloneComponents();
        if (t == null)
            list.remove(index);
        else if (compound.getClass() != t.getClass())
            list.set(index, t);
        else {
            ArrayList<Term> list2 = ((CompoundTerm)t).cloneComponents();
            for (int i = 0; i < list2.size(); i++)
                list.add(index + i, list2.get(i));
        }
        if (compound.isCommutative())
            Collections.sort(list);
        return make(compound, list, memory);
    }

    public static Term replaceComponent(CompoundTerm compound, Term oldComponent, Term newComponent, Memory memory) {
        ArrayList<Term> list = compound.cloneComponents();
        int index = list.indexOf(oldComponent);
        list.remove(index);
        if (compound.getClass() != newComponent.getClass())
            list.add(index, newComponent);
        else {
            ArrayList<Term> list2 = ((CompoundTerm)newComponent).cloneComponents();
            for (int i = 0; i < list2.size(); i++)
                list.add(index + i, list2.get(i));
        }
        if (compound.isCommutative())
            Collections.sort(list);
        return make(compound, list, memory);
    }
   
    /* ----- variable-related utilities ----- */
    
    /**
     * get the OpenVariables list
     * @return the open variables list
     */
    public ArrayList<Variable> getOpenVariables() {
        return openVariables;
    }
    
    /**
     * register open and closed variables in a CompoundTerm
     * <p>
     * an open variable only appears in one components, while a closed variable appears in multiple components
     */
    private void markVariables() {  // not recursive
        openVariables = new ArrayList<Variable>();
        ArrayList<Variable> closedVariables = new ArrayList<Variable>();  // local variable
        ArrayList<Variable> list;
        for (Term term : components) {
            if ((term instanceof Variable) && (((Variable) term).getType() != Variable.VarType.ANONYMOUS)) {
                openVariables.add((Variable) term);
            } else if (term instanceof CompoundTerm) {
                list = ((CompoundTerm) term).getOpenVariables();
                if (list != null) {
                    for (Variable var : list) {
                        if (var.getType() == Variable.VarType.QUERY)
                            openVariables.add(var);
                        else {
                            int i = openVariables.indexOf(var);
                            if (i >= 0) {           // assume a (independent/dependent) variable appears exactly twice
                                var.setScope(this);
                                openVariables.get(i).setScope(this);
                                openVariables.remove(i);
                                closedVariables.add(var);
                            } else
                                openVariables.add(var);
                        }
                    }
                }
            }
        }
        if (openVariables.size() == 0) // {
            openVariables = null;
    }
    
    // only called from Sentence
    public void renameVariables() {
        renameVariables(new HashMap<String, String>());
    }
    
    /**
     * Recursively rename variables by their appearing order in the CompoundTerm
     * <p>
     * Since each occurance of a variable is processed exactly ones, there will be no confusion between new names and old names.
     * @param varMap the mapping built so far
     */
    protected void renameVariables(HashMap<String, String> varMap) {
        String oldName, newName;
        for (Term t : components) {
            if ((t instanceof Variable) && (((Variable) t).getType() != Variable.VarType.ANONYMOUS)) {
                oldName = ((Variable) t).getSimpleName();
                newName = varMap.get(oldName);
                if (newName == null) {
                    newName = makeVarName(varMap.size(), (Variable) t);
                    varMap.put(oldName, newName);
                }
                ((Variable) t).setName(newName);
//            } else if ((t instanceof CompoundTerm) && !(t.isConstant())) {
            } else if (t instanceof CompoundTerm) {
                ((CompoundTerm) t).renameVariables(varMap);
            }
        }
        name = makeName();     // type-specific
    }
    
    /**
     * sequentially generate new variable names
     * @param size the current size of the variable list
     * @param v the variable to be renamed
     * @return a new variable name
     */
    private String makeVarName(int size, Variable v) {
        StringBuffer s = new StringBuffer();
        Variable.VarType type = v.getType();
        if (type == Variable.VarType.QUERY)
            s.append(Symbols.QUERY_VARIABLE_TAG);
        else
            s.append(Symbols.VARIABLE_TAG);
        s.append(size + 1);
        return s.toString();
    }
    
    public void substituteComponent(HashMap<String,Term> subs, boolean first) {
        Term t1, t2;
        String varName;
        for (int i = 0; i < size(); i++) {
            t1 = componentAt(i);
            if (t1 instanceof Variable) {                  // simple Variable
                varName = ((Variable) t1).getVarName(first);  // name of the variable used in the mapping
                t2 = subs.get(varName);                       // new Term if exist
                if (t2 != null) {
                    components.set(i, t2);
                }
            } else if (t1 instanceof CompoundTerm) {              // compound
                ((CompoundTerm) t1).substituteComponent(subs, first);   // apply substitute to a clone
            }
        }
        markVariables();        // variable status may be changed, so need to re-do this
        name = makeName();
    }
    
    /* ----- link CompoundTerm and its components ----- */
    
    /**
     * build CompositionalLinks to constant components and subcomponents
     * <p>
     * The compound type determines the link type; the component type determines whether to build the link.
     */
    public ArrayList<TermLink> prepareComponentLinks(Memory memory) {
        ArrayList<TermLink> componentLinks = new ArrayList<TermLink>();
        short type = (this instanceof Statement) ? TermLink.COMPOUND_STATEMENT : TermLink.COMPOUND;   // default
        prepareComponentLinks(componentLinks, type, this, memory);
        return componentLinks;
    }
    
    protected void prepareComponentLinks(ArrayList<TermLink> componentLinks, short type, CompoundTerm term, Memory memory) {
        Term t1, t2, t3;
        for (int i = 0; i < term.size(); i++) {     // first level components
            t1 = term.componentAt(i);
            if (t1.isConstant())                                            // first level
                componentLinks.add(new TermLink(t1, type, i, memory));
            if ((this instanceof Implication) && (i == 0) && (t1 instanceof Conjunction)) {
                ((CompoundTerm) t1).prepareComponentLinks(componentLinks, TermLink.COMPOUND_CONDITION, (CompoundTerm) t1, memory);
            } else if (t1 instanceof CompoundTerm) {
                for (int j = 0; j < ((CompoundTerm) t1).size(); j++) {
                    t2 = ((CompoundTerm) t1).componentAt(j);
                    if (t2.isConstant()) {                                  // second level
                        if ((t1 instanceof Product) || (t1 instanceof ImageExt) || (t1 instanceof ImageInt))
                            componentLinks.add(new TermLink(t2, TermLink.TRANSFORM, i, j, memory));
                        else
                            componentLinks.add(new TermLink(t2, type, i, j, memory));
                    }
                    if ((t2 instanceof Product) || (t2 instanceof ImageExt) || (t2 instanceof ImageInt)) {
                        for (int k = 0; k < ((CompoundTerm) t2).size(); k++) {
                            t3 = ((CompoundTerm) t2).componentAt(k);
                            if (t3.isConstant()) {                           // third level
                                componentLinks.add(new TermLink(t3, TermLink.TRANSFORM, i, j, k, memory));
                            }
                        }
                    }
                }
            }
        }
    }
    
    
    /* ---------- temporal order among components ---------- */
    
    public enum TemporalOrder { BEFORE, WHEN, AFTER, NONE, UNSURE }
    
    public static TemporalOrder temporalInference(TemporalOrder t1, TemporalOrder t2) {
        if ((t1 == TemporalOrder.UNSURE) || (t2 == TemporalOrder.UNSURE))
            return TemporalOrder.UNSURE;
        if (t1 == TemporalOrder.NONE)
            return t2;
        if (t2 == TemporalOrder.NONE)
            return t1;
        if (t1 == TemporalOrder.WHEN)
            return t2;
        if (t2 == TemporalOrder.WHEN)
            return t1;
        if (t1 == t2)
            return t1;
        return TemporalOrder.UNSURE;
    }

    public static TemporalOrder temporalReverse(TemporalOrder t1) {
        if (t1 == TemporalOrder.BEFORE)
            return TemporalOrder.AFTER;
        if (t1 == TemporalOrder.AFTER)
            return TemporalOrder.BEFORE;
        return t1;
    } 
    
    public static TemporalOrder temporalInferenceWithFigure(TemporalOrder order1, TemporalOrder order2, int figure) {
        switch (figure) {
        case 11:
            return temporalInference(temporalReverse(order1), order2);
        case 12:
            return temporalInference(temporalReverse(order1), temporalReverse(order2));
        case 21:
            return temporalInference(order1, order2);
        case 22:
            return temporalInference(order1, temporalReverse(order2));
        default:
            return TemporalOrder.UNSURE;
        }
    }
}
