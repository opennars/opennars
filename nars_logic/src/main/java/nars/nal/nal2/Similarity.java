/*
 * Similarity.java
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
package nars.nal.nal2;

import nars.Global;
import nars.nal.NALOperator;
import nars.nal.term.Statement;
import nars.nal.term.Term;

/**
 * A Statement about a Similarity relation.
 */
public class Similarity extends Statement {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected Similarity(final Term subj, final Term pred) {
        super(subj, pred);
        init(term);
    }
    

    /**
     * Clone an object
     * @return A new object, to be casted into a Similarity
     */
    @Override
    public Similarity clone() {
        return new Similarity(getSubject(), getPredicate());
    }
    
    @Override public Similarity clone(Term[] replaced) {
        if (replaced.length!=2)
            return null;
        return make(replaced[0], replaced[1]);
    }

    /** alternate version of make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    public static Term makeTerm(final Term subject, final Term predicate) {
        if (subject.equals(predicate))
            return subject;                
        return make(subject, predicate);        
    }    
    
    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @param subject The first component
     * @param predicate The second component
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Similarity make(final Term subject, final Term predicate) {

        if (invalidStatement(subject, predicate)) {
            return null;
        }

        int compare = subject.compareTo(predicate);
        if (compare > 0)
                return new Similarity(predicate, subject);
        else if (compare < 0)
                return new Similarity(subject, predicate);
        else {
                throw new RuntimeException("subject and predicate are equal according to compareTo: " + subject + " , " + predicate);
        }

    }

    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public NALOperator operator() {
        return NALOperator.SIMILARITY;
    }

    /**
     * Check if the compound is commutative.
     * @return true for commutative
     */
    @Override
    public final boolean isCommutative() {
        return true;
    }
}
