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
import org.opennars.main.MiscFlags;

import java.util.NavigableSet;
import java.util.TreeSet;

/**
 * A compound term whose extension is the difference of the intensions of its term
 */
public class DifferenceInt extends CompoundTerm {

    /**
     * Constructor with partial values, called by make
     * @param arg The component list of the term
     */
    private DifferenceInt(final Term[] arg) {
        super(arg);
        
        ensureValidDifferenceArguments(arg);
        
        init(arg);
    }

    public static void ensureValidDifferenceArguments(final Term[] arg) {
        if (arg.length!=2)
            throw new IllegalStateException("Requires 2 components");
        
        if (MiscFlags.DEBUG) {
            if (arg[0].equals(arg[1]))
                throw new IllegalStateException("Equal arguments invalid");
        }                
    }

    /**
     * Clone an object
     * @return A new object, to be casted into a DifferenceInt
     */
    @Override
    public DifferenceInt clone() {
        return new DifferenceInt(term);
    }
    
    @Override public Term clone(final Term[] replaced) {
        return make(replaced);
    }

    /**
     * Try to make a new DifferenceExt. Called by StringParser.
     * @return the Term generated from the arguments
     * @param arg The list of term
     */
    public static Term make(final Term[] arg) {
        if (arg.length == 1) { // special case from CompoundTerm.reduceComponent
            return arg[0];
        }
        if (arg.length != 2) {
            return null;
        }
        
        if ((arg[0] instanceof SetInt) && (arg[1] instanceof SetInt)) {
            //TODO maybe a faster way to calculate:
            final NavigableSet<Term> set = new TreeSet<>(((CompoundTerm) arg[0]).asTermList());
            set.removeAll(((CompoundTerm) arg[1]).asTermList());           // set difference
            return SetInt.make(set);
        }
                
        if (arg[0].equals(arg[1])) {
            return null;
        }
            
        return new DifferenceInt(arg);
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param t1 The first component
     * @param t2 The second component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term t1, final Term t2) {
        if (t1.equals(t2))
            return null;        

        return make(new Term[] { t1, t2 });
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.DIFFERENCE_INT;
    }
}


