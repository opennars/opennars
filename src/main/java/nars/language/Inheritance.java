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
package nars.language;

import java.util.Arrays;
import nars.main.Parameters;
import nars.io.Symbols.NativeOperator;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * A Statement about an Inheritance relation.
 */
public class Inheritance extends Statement {

    /**
     * Constructor with partial values, called by make
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected Inheritance(final Term[] arg) {
        super(arg);  
        
        init(arg);
    }
    
    protected Inheritance(final Term subj, final Term pred) {
        this(new Term[] { subj, pred} );
    }


    /**
     * Clone an object
     * @return A new object, to be casted into a SetExt
     */
    @Override public Inheritance clone() {
        return make(getSubject(), getPredicate());
    }

    @Override public Inheritance clone(Term[] t) {
        if (t.length!=2)
            throw new RuntimeException("Invalid terms for " + getClass().getSimpleName() + ": " + Arrays.toString(t));
                
        return make(t[0], t[1]);
    }

    /** alternate version of Inheritance.make that allows equivalent subject and predicate
     * to be reduced to the common term.      */
    public static Term makeTerm(final Term subject, final Term predicate) {            
        return make(subject, predicate);        
    }

    /**
     * Try to make a new compound from two term. Called by the inference rules.
     * @param subject The first compoment
     * @param predicate The second compoment
     * @param memory Reference to the memory
     * @return A compound generated or null
     */
    public static Inheritance make(final Term subject, final Term predicate) {
                
        if (subject==null || predicate==null || invalidStatement(subject, predicate)) {            
            return null;
        }
        
        boolean subjectProduct = subject instanceof Product;
        boolean predicateOperator = predicate instanceof Operator;
        
        if (Parameters.DEBUG) {
            if (!predicateOperator && predicate.toString().startsWith("^")) {
                throw new RuntimeException("operator term detected but is not an operator: " + predicate);
            }
        }
        
        if (subjectProduct && predicateOperator) {
            //name = Operation.makeName(predicate.name(), ((CompoundTerm) subject).term);
            return Operation.make((Operator)predicate, ((CompoundTerm)subject).term, true);
        } else {            
            return new Inheritance(subject, predicate);
        }
         
    }

    /**
     * Get the operator of the term.
     * @return the operator of the term
     */
    @Override
    public NativeOperator operator() {
        return NativeOperator.INHERITANCE;
    }

}

