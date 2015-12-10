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

import nars.Op;
import nars.Symbols;
import nars.nal.nal4.Image;
import nars.term.compound.Compound;
import nars.term.variable.Variable;
import nars.util.utf8.ByteBuf;

import java.io.IOException;

import static nars.Symbols.STATEMENT_CLOSER;
import static nars.Symbols.STATEMENT_OPENER;

/**
 * A statement or relation is a compound term, consisting of a subject, a predicate, and a
 * relation symbol in between. It can be of either first-order or higher-order.
 */
@Deprecated
public interface Statement {


    static void append(Compound c, Appendable w, boolean pretty) {

        Term a = subj(c);
        Term b = pred(c);

        try {
            w.append(STATEMENT_OPENER);
            a.append(w, pretty);

            if (pretty) w.append(' ');

            c.op().append(w);

            if (pretty) w.append(' ');

            b.append(w, pretty);

            w.append(STATEMENT_CLOSER);

        } catch (IOException e) {
            e.printStackTrace();

        }

    }


//    /**
//     * Make a Statement from String, called by StringParser
//     *
//     * @param o         The relation String
//     * @param subject   The first component
//     * @param predicate The second component
//     * @return The Statement built
//     */
//    final public static Statement make(final Op o, final Term subject, final Term predicate, boolean customOrder, int order) {
//
//        switch (o) {
//            case INHERITANCE:
//                return Inheritance.make(subject, predicate);
//            case SIMILARITY:
//                return Similarity.make(subject, predicate);
//            case INSTANCE:
//                return Instance.make(subject, predicate);
//            case PROPERTY:
//                return Property.make(subject, predicate);
//            case INSTANCE_PROPERTY:
//                return InstanceProperty.make(subject, predicate);
//            case IMPLICATION:
//                return Implication.make(subject, predicate);
//            case IMPLICATION_AFTER:
//                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_FORWARD);
//            case IMPLICATION_BEFORE:
//                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_BACKWARD);
//            case IMPLICATION_WHEN:
//                return Implication.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_CONCURRENT);
//            case EQUIVALENCE:
//                return Equivalence.make(subject, predicate);
//            case EQUIVALENCE_AFTER:
//                return Equivalence.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_FORWARD);
//            case EQUIVALENCE_WHEN:
//                return Equivalence.make(subject, predicate, customOrder ? order : TemporalRules.ORDER_CONCURRENT);
//        }
//
//        return null;
//    }

//    /**
//     * Make a Statement from given term, called by the rules
//     *
//     * @param order     The temporal order of the statement
//     * @param subj      The first component
//     * @param pred      The second component
//     * @param statement A sample statement providing the class type
//     * @param memory    Reference to the memory
//     * @return The Statement built
//     */
//    final public static Statement make(final Statement statement, final Term subj, final Term pred, int order) {
//        return make(statement.op(), subj, pred, order);
//    }
//
//    /** op needs to be a Statement operator */
//    final public static Statement make(final Op o, final Term subject, final Term predicate, int order) {
//        return make(o, subject, predicate, true, order);
//    }

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

//    @Deprecated
//    final protected static CharSequence makeStatementName(final Term subject, final Op relation, final Term predicate) {
//        throw new RuntimeException("Not necessary, utf8 keys should be used instead");
////        final CharSequence subjectName = subject.toString();
////        final CharSequence predicateName = predicate.toString();
////        int length = subjectName.length() + predicateName.length() + relation.toString().length() + 4;
////
////        StringBuilder cb = new StringBuilder(length);
////
////        cb.append(STATEMENT_OPENER.ch);
////
////        //Texts.append(cb, subjectName);
////        cb.append(subjectName);
////
////        cb.append(' ').append(relation).append(' ');
////        //cb.append(relation);
////
////        //Texts.append(cb, predicateName);
////        cb.append(predicateName);
////
////        cb.append(STATEMENT_CLOSER.ch);
////
////        return cb.toString();
//    }



    static byte[] bytes(Op op, Term subject, Term predicate) {
        byte[] subjBytes = subject.bytes();
        byte[] predBytes = predicate.bytes();
        byte[] relationBytes = op.bytes;

        ByteBuf b = ByteBuf.create(
                subjBytes.length + predBytes.length + relationBytes.length +
                        1 + 1 //separator and end closers
        );

        return b.add(relationBytes)
                .add(subjBytes)
                .add((byte) Symbols.ARGUMENT_SEPARATOR)
                .add(predBytes)
                .add((byte) STATEMENT_CLOSER).toBytes();
    }



    static void append(Appendable w, Op op, Term subject, Term predicate, boolean pretty) throws IOException {

        w.append(STATEMENT_OPENER);

        subject.append(w, pretty);

        if (pretty) w.append(' ');

        op.append(w);

        if (pretty) w.append(' ');

        predicate.append(w, pretty);

        w.append(STATEMENT_CLOSER);
    }


    static boolean invalidStatement(Compound s) {
        return invalidStatement(s.term(0), s.term(1));
    }


    /**
     * Check the validity of a potential Statement. [To be refined]
     * <p>
     *
     * @param subject   The first component
     * @param predicate The second component
     * @return Whether The Statement is invalid
     */
    static boolean invalidStatement(Term subject, Term predicate) {
        if (subject == null || predicate == null)
            return true;

        if (subject.equals(predicate))
            return true;

        //TODO combine these mirrored invalidReflexive calls into one combined, unredundant operation
        if (invalidReflexive(subject, predicate))
            return true;

        if (invalidReflexive(predicate, subject))
            return true;


        if ((Statement.is(subject)) && (Statement.is(predicate))) {
            Compound s1 = (Compound) subject;
            Compound s2 = (Compound) predicate;

            Term t11 = Statement.subj(s1);
            Term t22 = Statement.pred(s2);
            if (!t11.equals(t22))
                return false;

            Term t12 = Statement.pred(s1);
            Term t21 = Statement.subj(s2);
            if (t12.equals(t21))
                return true;

        }
        return false;
    }



    static boolean is(Term t) {
        return t.op().isStatement();
    }

    static Term subj(Term t) {
        return ((Compound)t).term(0);
    }
    static Term pred(Term t) {
        return ((Compound)t).term(1);
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
    static boolean invalidReflexive(Term t1, Term t2) {
        if (!(t1 instanceof Compound)) {
            return false;
        }
        if ((t1 instanceof Image /*Ext) || (t1 instanceof ImageInt*/)) {
            return false;
        }
        Compound ct1 = (Compound) t1;
        return ct1.containsTerm(t2);
    }


//    public static boolean invalidPair(final Term s1, final Term s2) {
//        boolean s1Indep = s1.hasVarIndep();
//        boolean s2Indep = s2.hasVarIndep();
//        //return (s1Indep && !s2Indep || !s1Indep && s2Indep);
//        return s1Indep ^ s2Indep;
//    }

    static boolean subjectOrPredicateIsIndependentVar(Compound t) {
        if (!t.hasVarIndep()) return false;

        Term subj = t.term(0);
        if ((subj instanceof Variable) && (subj.hasVarIndep()))
            return true;

        Term pred = t.term(1);
        return (pred instanceof Variable) && (pred.hasVarIndep());

    }

}
