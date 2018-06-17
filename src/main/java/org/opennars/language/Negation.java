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
 * A negation of a statement.
 */
public class Negation extends CompoundTerm {



    /** avoid using this externally, because double-negatives can be unwrapped to the 
     * original term using Negation.make */
    protected Negation(final Term t) {
        super(new Term[] { t });
        
        init(term);
    }

    @Override
    protected CharSequence makeName() {
        return makeCompoundName(NativeOperator.NEGATION, term[0]);
    }
    
    /**
     * Clone an object
     *
     * @return A new object
     */
    @Override
    public Negation clone() {
        return new Negation(term[0]);
    }

    @Override
    public Term clone(final Term[] replaced) {
        if (replaced.length!=1)
            return null;
        return make(replaced[0]);
    }
    
    

    /**
     * Try to make a Negation of one component. Called by the inference rules.
     *
     * @param t The component
     * @return A compound generated or a term it reduced to
     */
    public static Term make(final Term t) {
        if (t instanceof Negation) {
            // (--,(--,P)) = P
            return ((Negation) t).term[0];
        }         
        return new Negation(t);
    }

    /**
     * Try to make a new Negation. Called by StringParser.
     *
     * @return the Term generated from the arguments
     * @param argument The list of term
     */
    public static Term make(final Term[] argument) {
        if (argument.length != 1)
            return null;        
        return make(argument[0]);
    }

    /**
     * Get the operator of the term.
     *
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.NEGATION;
    }

    
    public static boolean areMutuallyInverse(final Term tc, final Term ptc) {
        //doesnt seem necessary to check both, one seems sufficient.
        //incurs cost of creating a Negation and its id
        return (ptc.equals(Negation.make(tc)) /* || tc.equals(Negation.make(ptc))*/ );        
    }

}
