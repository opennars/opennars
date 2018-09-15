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

import com.google.common.collect.ObjectArrays;
import org.opennars.io.Symbols.NativeOperator;
import org.opennars.main.MiscFlags;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

/**
 * type==Extension: A compound term whose extension is the intersection of the extensions of its term as defined in the NARS-theory
 * type==Intension: A compound term whose intension is the intersection of the extensions of its term as defined in the NARS-theory
 *
 * @author Patrick Hammer
 * @author Robert Wünsche
 */
public class Intersection extends CompoundTerm {
    public final EnumType type;

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private Intersection(final Term[] arg, final EnumType type) {
        super( arg );
        this.type = type;

        if (MiscFlags.DEBUG) { Terms.verifySortedAndUnique(arg, false); }

        init(arg);
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a Conjunction
     */
    @Override
    public Intersection clone() {
        return new Intersection(term, type);
    }

    @Override
    public Term clone(final Term[] replaced) {
        if(replaced == null) {
            return null;
        }
        return make(replaced, type);
    }


    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param term1 The first component
     * @param term2 The second component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term term1, final Term term2, EnumType setType) {
        Class setTypeClass = setType == EnumType.EXTENSION ? SetExt.class : SetInt.class;
        Class opositeSetTypeClass = setType == EnumType.INTENSION ? SetInt.class : SetExt.class;

        if ((term1.getClass().equals(opositeSetTypeClass)) && (term2.getClass().equals(opositeSetTypeClass))) {
            // set union
            final Term[] both = ObjectArrays.concat(
                ((CompoundTerm) term1).term,
                ((CompoundTerm) term2).term, Term.class);

            return (Term)invokeMake(opositeSetTypeClass, both);
        }
        else if ((term1.getClass().equals(setTypeClass)) && (term2.getClass().equals(setTypeClass))) {
            // set intersection
            final NavigableSet<Term> set = Term.toSortedSet(((CompoundTerm) term1).term);

            set.retainAll(((CompoundTerm) term2).asTermList());

            //technically this can be used directly if it can be converted to array
            //but wait until we can verify that NavigableSet.toarray does it or write a helper function like existed previously
            return (Term)invokeMake(setTypeClass, set.toArray(new Term[0]));
        }

        final List<Term> se = new ArrayList<>();
        if (term1 instanceof Intersection && (((Intersection)term1).type == setType)) {
            ((CompoundTerm) term1).addTermsTo(se);
            if (term2 instanceof Intersection && (((Intersection)term2).type == setType)) {
                // (&,(&,P,Q),(&,R,S)) = (&,P,Q,R,S)
                ((CompoundTerm) term2).addTermsTo(se);
            }
            else {
                // (&,(&,P,Q),R) = (&,P,Q,R)
                se.add(term2);
            }
        } else if (term2 instanceof Intersection && (((Intersection)term2).type == setType)) {
            // (&,R,(&,P,Q)) = (&,P,Q,R)
            ((CompoundTerm) term2).addTermsTo(se);
            se.add(term1);
        } else {
            se.add(term1);
            se.add(term2);
        }
        return make(se.toArray(new Term[0]), setType);
    }

    private static Object invokeMake(Class invokedClass, Term[] both) {
        try {
            Method m = invokedClass.getMethod("make", Term[].class);
            return m.invoke(null, both);
        } catch (NoSuchMethodException e) {
            // TODO< refactor so we throw exception >
            //e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            // TODO< refactor so we throw exception >
            //e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
            // TODO< refactor so we throw exception >
            //e.printStackTrace();
            return null;
        }
    }


    public static Term make(Term[] t, final EnumType type) {
        t = Term.toSortedSetArray(t);
        switch (t.length) {
            case 0: return null;
            case 1: return t[0];
            default:
                return new Intersection(t, type);
        }
    }


    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return type == EnumType.EXTENSION ? NativeOperator.INTERSECTION_EXT : NativeOperator.INTERSECTION_INT;
    }

    /**
     * Check if the compound is commutative.
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }

    public enum EnumType {
        EXTENSION,
        INTENSION
    }
}

