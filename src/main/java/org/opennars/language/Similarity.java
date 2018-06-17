/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.language;

import org.opennars.io.Symbols.NativeOperator;

/**
 * A Statement about a Similarity relation.
 */
public class Similarity extends Statement {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    public Similarity(final Term[] arg) {
        super(arg);
        
        init(arg);
    }

    public Similarity(final Term subj, final Term pred) {
        this(new Term[] { subj, pred} );
    }
    

    /**
     * Clone an object
     * @return A new object, to be casted into a Similarity
     */
    @Override
    public Similarity clone() {
        return new Similarity(term);
    }
    
    @Override public Similarity clone(final Term[] replaced) {
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
     * Try to make a new compound from two term. Called by the inference rules.
     * @param subject The first component
     * @param predicate The second component
     * @return A compound generated or null
     */
    public static Similarity make(final Term subject, final Term predicate) {

        if (invalidStatement(subject, predicate)) {
            return null;
        }
        if (subject.compareTo(predicate) > 0) {
            return make(predicate, subject);
        }        
        
        return new Similarity(subject, predicate);
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.SIMILARITY;
    }

    /**
     * Check if the compound is commutative.
     * @return true for commutative
     */
    @Override
    public boolean isCommutative() {
        return true;
    }
}
