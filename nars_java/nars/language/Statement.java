/*
 * Statement.java
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
import java.util.Map;
import java.util.TreeMap;

import nars.io.Symbols;
import nars.io.Symbols.Relation;
import nars.storage.Memory;

/**
 * A statement is a compound term, consisting of a subject, a predicate, and a
 * relation symbol in between. It can be of either first-order or higher-order.
 */
public abstract class Statement extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     *
     * @param arg The component list of the term
     */
    protected Statement(final ArrayList<Term> arg) {
        super(arg);
    }

    /**
     * Default constructor
     */
    protected Statement() {
    }

    /**
     * Constructor with full values, called by clone
     *
     * @param n The nameStr of the term
     * @param cs Component list
     * @param con Constant indicator
     * @param i Syntactic complexity of the compound
     */
    protected Statement(final String n, final ArrayList<Term> cs, final boolean con, final short i) {
        super(n, cs, con, i);
    }

    /**
     * Make a Statement from String, called by StringParser
     *
     * @param relation The relation String
     * @param subject The first component
     * @param predicate The second component
     * @param memory Reference to the memory
     * @return The Statement built
     */
    public static Statement make(final Relation relation, final Term subject, final Term predicate, final Memory memory) {
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        if (relation == Relation.INHERITANCE) {
            return Inheritance.make(subject, predicate, memory);
        }
        if (relation == Relation.SIMILARITY) {
            return Similarity.make(subject, predicate, memory);
        }
        if (relation == Relation.INSTANCE) {
            return Instance.make(subject, predicate, memory);
        }
        if (relation == Relation.PROPERTY) {
            return Property.make(subject, predicate, memory);
        }
        if (relation == Relation.INSTANCE_PROPERTY) {
            return InstanceProperty.make(subject, predicate, memory);
        }
        if (relation == Relation.IMPLICATION) {
            return Implication.make(subject, predicate, memory);
        }
        if (relation == Relation.IMPLICATION_AFTER) {
            return Implication.make(subject, predicate, CompoundTerm.ORDER_FORWARD, memory);
        }
        if (relation == Relation.IMPLICATION_BEFORE) {
            return Implication.make(subject, predicate, CompoundTerm.ORDER_BACKWARD, memory);
        }
        if (relation == Relation.IMPLICATION_WHEN) {
            return Implication.make(subject, predicate, CompoundTerm.ORDER_CONCURRENT, memory);
        }
        if (relation == Relation.EQUIVALENCE) {
            return Equivalence.make(subject, predicate, memory);
        }
        if (relation == Relation.EQUIVALENCE_AFTER) {
            return Equivalence.make(subject, predicate, CompoundTerm.ORDER_FORWARD, memory);
        }
        if (relation == Relation.EQUIVALENCE_WHEN) {
            return Equivalence.make(subject, predicate, CompoundTerm.ORDER_CONCURRENT, memory);
        }
        return null;
    }

    /**
     * Make a Statement from given components, called by the rules
     *
     * @return The Statement built
     * @param subj The first component
     * @param pred The second component
     * @param statement A sample statement providing the class type
     * @param memory Reference to the memory
     */
    public static Statement make(final Statement statement, final Term subj, final Term pred, final Memory memory) {
        if (statement instanceof Inheritance) {
            return Inheritance.make(subj, pred, memory);
        }
        if (statement instanceof Similarity) {
            return Similarity.make(subj, pred, memory);
        }
        if (statement instanceof Implication) {
            return Implication.make(subj, pred, memory);
        }
        if (statement instanceof Equivalence) {
            return Equivalence.make(subj, pred, memory);
        }
        return null;
    }

    /**
     * Make a symmetric Statement from given components and temporal
     * information, called by the rules
     *
     * @param statement A sample asymmetric statement providing the class type
     * @param subj The first component
     * @param pred The second component
     * @param memory Reference to the memory
     * @return The Statement built
     */
    public static Statement makeSym(final Statement statement, final Term subj, final Term pred, final Memory memory) {
        if (statement instanceof Inheritance) {
            return Similarity.make(subj, pred, memory);
        }
        if (statement instanceof Implication) {
            return Equivalence.make(subj, pred, memory);
        }
        return null;
    }

    
    /**
     * Check Statement relation symbol, called in StringPaser
     *
     * @param s0 The String to be checked
     * @return if the given String is a relation symbol
     */
    public static boolean isRelation(final String s0) {
        final String s = s0.trim();
        if (s.length() != 3) {
            return false;
        }

        return Symbols.getRelation(s)!=null;
    }
    
    public static Relation getRelation(final String s) {
        return Symbols.getRelation(s);
    }

    /**
     * Override the default in making the nameStr of the current term from
     * existing fields
     *
     * @return the nameStr of the term
     */
    @Override
    protected String makeName() {
        return makeStatementName(getSubject(), operator(), getPredicate());
    }

    /**
     * Default method to make the nameStr of an image term from given fields
     *
     * @param subject The first component
     * @param predicate The second component
     * @param relation The relation operator
     * @return The nameStr of the term
     */
    protected static String makeStatementName(final Term subject, final String relation, final Term predicate) {
        final String subjectName = subject.getName();
        final String predicateName = predicate.getName();
        int length = subjectName.length() + predicateName.length() + relation.length() + 4;
        
        final StringBuilder nameStr = new StringBuilder(length);
        nameStr.append(Symbols.STATEMENT_OPENER);
        nameStr.append(subjectName);
        nameStr.append(' ').append(relation).append(' ');
        nameStr.append(predicateName);
        nameStr.append(Symbols.STATEMENT_CLOSER);
        return nameStr.toString();
    }

    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     * @param subject The first component
     * @param predicate The second component
     * @return Whether The Statement is invalid
     */
    public static boolean invalidStatement(final Term subject, final Term predicate) {
        if (subject.equals(predicate)) {
            return true;
        }
        if (invalidReflexive(subject, predicate)) {
            return true;
        }
        if (invalidReflexive(predicate, subject)) {
            return true;
        }
        if ((subject instanceof Statement) && (predicate instanceof Statement)) {
            final Statement s1 = (Statement) subject;
            final Statement s2 = (Statement) predicate;
            final Term t11 = s1.getSubject();
            final Term t12 = s1.getPredicate();
            final Term t21 = s2.getSubject();
            final Term t22 = s2.getPredicate();
            if (t11.equals(t22) && t12.equals(t21)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if one term is identical to or included in another one, except in a
     * reflexive relation
     * <p>
     * @param t1 The first term
     * @param t2 The second term
     * @return Whether they cannot be related in a statement
     */
    private static boolean invalidReflexive(final Term t1, final Term t2) {
        if (!(t1 instanceof CompoundTerm)) {
            return false;
        }
        final CompoundTerm com = (CompoundTerm) t1;
        if ((com instanceof ImageExt) || (com instanceof ImageInt)) {
            return false;
        }
        return com.containComponent(t2);
    }

    
    public static boolean invalidPair(final String s1, final String s2) {
        if (Variable.containVarIndep(s1) && !Variable.containVarIndep(s2)) {
            return true;
        } else if (!Variable.containVarIndep(s1) && Variable.containVarIndep(s2)) {
            return true;
        }
        return false;
    }

    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     * Minimum requirement: the two terms cannot be the same, or containing each
     * other as component
     *
     * @return Whether The Statement is invalid
     */
    public boolean invalid() {
        return invalidStatement(getSubject(), getPredicate());
    }
    
 
    /**
     * Return the first component of the statement
     *
     * @return The first component
     */
    public Term getSubject() {
        return components.get(0);
    }

    /**
     * Return the second component of the statement
     *
     * @return The second component
     */
    public Term getPredicate() {
        return components.get(1);
    }
}
