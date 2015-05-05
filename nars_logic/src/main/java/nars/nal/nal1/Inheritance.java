/*
 * Inheritance.java
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
package nars.nal.nal1;

import nars.Global;
import nars.nal.NALOperator;
import nars.nal.term.Compound;
import nars.nal.Statement;
import nars.nal.term.Term;
import nars.nal.nal4.Product;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;

/**
 * A Statement about an Inheritance relation.
 */
public class Inheritance extends Statement {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    protected Inheritance(final Term... arg) {
        super(arg);

        init(arg);
    }

    @Deprecated protected Inheritance() {
        super();
    }
    
    /*protected Inheritance(final Term subj, final Term pred) {
        this(subj, pred);
    }*/


    
    /**
     * Clone an object
     * @return A new object, to be casted into a SetExt
     */
    @Override public Inheritance clone() {
        return make(getSubject(), getPredicate());
    }

    @Override public Inheritance clone(Term[] t) {
        if (t.length!=2)
            return null;
            //throw new RuntimeException("Invalid terms for " + getClass().getSimpleName() + ": " + Arrays.toString(t));
                
        return make(t[0], t[1]);
    }

    /** alternate version of Inheritance.make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    public static Term makeTerm(final Term subject, final Term predicate) {            
        if (subject.equals(predicate))
            return subject;
        return make(subject, predicate);
    }

    /**
     * Try to make a new compound from two term. Called by the logic rules.
     * @param subject The first compoment
     * @param predicate The second compoment
     * @return A compound generated or null
     */
    public static Inheritance make(final Term subject, final Term predicate) {
                
        if (invalidStatement(subject, predicate)) {
            return null;
        }
        
        boolean subjectProduct = subject instanceof Product;
        boolean predicateOperator = predicate instanceof Operator;

        if (subjectProduct && predicateOperator) {
            return Operation.make((Operator) predicate, ((Compound) subject).term);
        } else {            
            return new Inheritance(subject, predicate);
        }
         
    }

    /**
     * Get the operate of the term.
     * @return the operate of the term
     */
    @Override
    public NALOperator operator() {
        return NALOperator.INHERITANCE;
    }

}

