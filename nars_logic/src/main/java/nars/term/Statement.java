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
package nars.term;

import nars.Global;
import nars.Op;
import nars.Symbols;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Instance;
import nars.nal.nal2.InstanceProperty;
import nars.nal.nal2.Property;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Image;
import nars.nal.nal5.Equivalence;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.util.utf8.ByteBuf;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import static nars.Op.STATEMENT_CLOSER;
import static nars.Op.STATEMENT_OPENER;

/**
 * A statement or relation is a compound term, consisting of a subject, a predicate, and a
 * relation symbol in between. It can be of either first-order or higher-order.
 */
public abstract class Statement<A extends Term, B extends Term> extends Compound2<A, B> {

    /**
     * Constructor with partial values, called by make
     * Subclass constructors should call init after any initialization
     *
     * @param arg The component list of the term
     */
    protected Statement(final A subj, final B pred) {
        super(subj, pred);
    }
    /*protected Statement(final Term... args) {
        super(args);
    }*/

    /*protected Statement(final Term... twoTermsPlease) {
        this(twoTermsPlease[0], twoTermsPlease[1]);
    }*/
    @Deprecated
    protected Statement() {
        this(null, null);
    }


    @Override
    protected void init(Term... t) {
        if (Global.DEBUG) {
            if (t.length != 2)
                throw new RuntimeException("Requires 2 terms: " + Arrays.toString(t));
            if (t[0] == null)
                throw new RuntimeException("Null subject: " + this);
            if (t[1] == null)
                throw new RuntimeException("Null predicate: " + this);

            if (isCommutative()) {
                if (t[0].compareTo(t[1]) > 0) {
                    throw new RuntimeException("Commutative term requires natural order of subject,predicate: " + Arrays.toString(t));
                }
            }
        }
        super.init(t);
    }


    /**
     * Make a Statement from String, called by StringParser
     *
     * @param o         The relation String
     * @param subject   The first component
     * @param predicate The second component
     * @return The Statement built
     */
    final public static Statement make(final Op o, final Term subject, final Term predicate, boolean customOrder, int order) {

        switch (o) {
            case INHERITANCE:
                return Inheritance.make(subject, predicate);
            case SIMILARITY:
                return Similarity.make(subject, predicate);
            case INSTANCE:
                return Instance.make(subject, predicate);
            case PROPERTY:
                return Property.make(subject, predicate);
            case INSTANCE_PROPERTY:
                return InstanceProperty.make(subject, predicate);
            case IMPLICATION:
                return Implication.make(subject, predicate);
            case IMPLICATION_AFTER:
                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_FORWARD);
            case IMPLICATION_BEFORE:
                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_BACKWARD);
            case IMPLICATION_WHEN:
                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE:
                return Equivalence.make(subject, predicate);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_FORWARD);
            case EQUIVALENCE_WHEN:
                return Equivalence.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_CONCURRENT);
        }

        return null;
    }

    /**
     * Make a Statement from given term, called by the rules
     *
     * @param order     The temporal order of the statement
     * @param subj      The first component
     * @param pred      The second component
     * @param statement A sample statement providing the class type
     * @param memory    Reference to the memory
     * @return The Statement built
     */
    final public static Statement make(final Statement statement, final Term subj, final Term pred, int order) {
        return make(statement.operator(), subj, pred, order);
    }

    /** op needs to be a Statement operator */
    final public static Statement make(final Op o, final Term subject, final Term predicate, int order) {
        return make(o, subject, predicate, true, order);
    }

//    /**
//     * Override the default in making the nameStr of the current term from
//     * existing fields
//     *
//     * @return the nameStr of the term
//     */
//    @Override
//    protected CharSequence makeName() {
//        throw new RuntimeException("should not be called");
//        //return makeStatementName(getSubject(), operator(), getPredicate());
//    }
//
//    @Override
//    protected byte[] makeKey() {
//        throw new RuntimeException("should not be called");
//        //return makeStatementKey(getSubject(), operator(), getPredicate());
//    }


//    /**
//     * Default method to make the nameStr of an image term from given fields
//     *
//     * @param subject   The first component
//     * @param predicate The second component
//     * @param relation  The relation operate
//     * @return The nameStr of the term
//     */
//    @Deprecated final protected static CharSequence makeStatementNameSB(final Term subject, final NALOperator relation, final Term predicate) {
//        final CharSequence subjectName = subject.toString();
//        final CharSequence predicateName = predicate.toString();
//        int length = subjectName.length() + predicateName.length() + relation.toString().length() + 4;
//
//        StringBuilder sb = new StringBuilder(length)
//                .append(STATEMENT_OPENER.ch)
//                .append(subjectName)
//
//                .append(' ').append(relation).append(' ')
//                        //.append(relation)
//
//                .append(predicateName)
//                .append(STATEMENT_CLOSER.ch);
//
//        return sb.toString();
//    }

    @Deprecated
    final protected static CharSequence makeStatementName(final Term subject, final Op relation, final Term predicate) {
        throw new RuntimeException("Not necessary, utf8 keys should be used instead");
//        final CharSequence subjectName = subject.toString();
//        final CharSequence predicateName = predicate.toString();
//        int length = subjectName.length() + predicateName.length() + relation.toString().length() + 4;
//
//        StringBuilder cb = new StringBuilder(length);
//
//        cb.append(STATEMENT_OPENER.ch);
//
//        //Texts.append(cb, subjectName);
//        cb.append(subjectName);
//
//        cb.append(' ').append(relation).append(' ');
//        //cb.append(relation);
//
//        //Texts.append(cb, predicateName);
//        cb.append(predicateName);
//
//        cb.append(STATEMENT_CLOSER.ch);
//
//        return cb.toString();
    }

    @Override
    public byte[] init() {
        final byte[] subjBytes = getSubject().bytes();
        final byte[] predBytes = getPredicate().bytes();
        final byte[] relationBytes = operator().bytes;

        ByteBuf b = ByteBuf.create(
                subjBytes.length + predBytes.length + relationBytes.length +
                        + 1 + 1 //separator and end closers
        );

        return b.add(relationBytes)
                .add(subjBytes)
                .add((byte)Symbols.STAMP_SEPARATOR)
                .add(predBytes)
                .add((byte) STATEMENT_CLOSER.ch).toBytes();
    }


    @Override
    public void append(final Writer w, final boolean pretty) throws IOException {

        w.append(STATEMENT_OPENER.ch);

        getSubject().name().append(w, pretty);

        if (pretty) w.append(' ');

        operator().expand(w);

        if (pretty) w.append(' ');

        getPredicate().name().append(w, pretty);

        w.append(STATEMENT_CLOSER.ch);
    }


    final public static boolean invalidStatement(final Statement s) {
        return invalidStatement(s.getSubject(), s.getPredicate());
    }


    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     *
     * @param subject   The first component
     * @param predicate The second component
     * @return Whether The Statement is invalid
     */
    final public static boolean invalidStatement(final Term subject, final Term predicate) {
        if (subject == null || predicate == null) return true;

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
            final Term t22 = s2.getPredicate();
            if (!t11.equals(t22))
                return false;

            final Term t12 = s1.getPredicate();
            final Term t21 = s2.getSubject();
            if (t12.equals(t21))
                return true;


            /*if (t11.equals(t22) && t12.equals(t21))
                return true;
            */
        }
        return false;
    }

    /**
     * Check if one term is identical to or included in another one, except in a
     * reflexive relation
     * <p>
     *
     * @param t1 The first term
     * @param t2 The second term
     * @return Whether they cannot be related in a statement
     */
    private static boolean invalidReflexive(final Term t1, final Term t2) {
        if (!(t1 instanceof Compound)) {
            return false;
        }
        if ((t1 instanceof Image /*Ext) || (t1 instanceof ImageInt*/)) {
            return false;
        }
        final Compound ct1 = (Compound) t1;
        return ct1.containsTerm(t2);
    }


    public static boolean invalidPair(final Term s1, final Term s2) {
        boolean s1Indep = s1.hasVarIndep();
        boolean s2Indep = s2.hasVarIndep();
        //return (s1Indep && !s2Indep || !s1Indep && s2Indep);
        return s1Indep ^ s2Indep;
    }

    public boolean subjectOrPredicateIsIndependentVar() {
        if (!hasVarIndep()) return false;

        Term subj = getSubject();
        if ((subj instanceof Variable) && (subj.hasVarIndep()))
            return true;

        Term pred = getPredicate();
        return (pred instanceof Variable) && (pred.hasVarIndep());

    }


    /**
     * Return the first component of the statement
     *
     * @return The first component
     */
    public A getSubject() {
        return (A) term[0];
    }

    /**
     * Return the second component of the statement
     *
     * @return The second component
     */
    public B getPredicate() {
        return (B) term[1];
    }

    @Override
    public abstract Statement clone();

    public Term getSubject(boolean unwrapLen1SetExt, boolean unwrapLen1SetInt, boolean unwrapLen1Product) {
        return Compound.unwrap(getSubject(), unwrapLen1SetExt, unwrapLen1SetInt, unwrapLen1Product);
    }
    public Term getPredicate(boolean unwrapLen1SetExt, boolean unwrapLen1SetInt, boolean unwrapLen1Product) {
        return Compound.unwrap(getPredicate(), unwrapLen1SetExt, unwrapLen1SetInt, unwrapLen1Product);
    }
}


//    Should not be necessary since a Statement that would be invalid would never be created:
//    /**
//     * Check the validity of a potential Statement. [To be refined]
//     * <p>
//     * Minimum requirement: the two terms cannot be the same, or containing each
//     * other as component
//     *
//     * @return Whether The Statement is invalid
//     */
//    public boolean invalid() {
//        return invalidStatement(getSubject(), getPredicate());
//    }
