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
package org.opennars.operator;

import org.opennars.entity.Task;
import org.opennars.io.Symbols;
import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_CLOSER;
import static org.opennars.io.Symbols.NativeOperator.COMPOUND_TERM_OPENER;
import org.opennars.language.Inheritance;
import org.opennars.language.Product;
import org.opennars.language.Term;

/**
 * An operation is interpreted as an Inheritance relation.
 */
public class Operation extends Inheritance {
    private Task task;
    public final static Term[] SELF_TERM_ARRAY = new Term[] { SELF };

    /**
     * Constructor with partial values, called by make
     *
     * @param n The name of the term
     * @param arg The component list of the term
     */
    protected Operation(Term argProduct, Term operator) {
        super(argProduct, operator);
    }
    
    protected Operation(Term[] t) {
        super(t);
    }
    
    /**
     * Clone an object
     *
     * @return A new object, to be casted into a SetExt
     */
    @Override
    public Operation clone() {        
        return new Operation(term);
    }
 
   
    /**
     * Try to make a new compound from two components. Called by the inference
     * rules.
     *
     * @param memory Reference to the memory
     * @param addSelf include SELF term at end of product terms
     * @return A compound generated or null
     */
    public static Operation make(final Operator oper, Term[] arg, boolean addSelf) {        

//        if (Variables.containVar(arg)) {
//            throw new RuntimeException("Operator contains variable: " + oper + " with arguments " + Arrays.toString(arg) );
//        }
        /*//new cleaner  policy: should be added by the example already
        if(addSelf && !Term.isSelf(arg[arg.length-1])) {
            Term[] arg2=new Term[arg.length+1];
            for(int i=0;i<arg.length;i++) {
                arg2[i]=arg[i];
            }
            arg2[arg.length] = Term.SELF;
            arg=arg2;
        }*/
        
        return new Operation( new Product(arg), oper  );        
    }

    public Operator getOperator() {
        return (Operator)getPredicate();
    }
    
    @Override
    protected CharSequence makeName() {
        if(getSubject() instanceof Product && getPredicate() instanceof Operator)
            return makeName(getPredicate().name(), ((Product)getSubject()).term);
        return makeStatementName(getSubject(), Symbols.NativeOperator.INHERITANCE, getPredicate());
    }

    
    public static CharSequence makeName(final CharSequence op, final Term[] arg) {
        final StringBuilder nameBuilder = new StringBuilder(16) //estimate
                .append(COMPOUND_TERM_OPENER.ch).append(op);
        
        int n=0;
        for (final Term t : arg) {
            /*if(n==arg.length-1) {
                break;
            }*/
            nameBuilder.append(Symbols.ARGUMENT_SEPARATOR);
            nameBuilder.append(t.name());
            n++;
        }
        
        nameBuilder.append(COMPOUND_TERM_CLOSER.ch);
        return nameBuilder.toString();
    }
    
    /** stores the currently executed task, which can be accessed by Operator execution */
    public void setTask(final Task task) {
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public Product getArguments() {
        return (Product)getSubject();
    }
    
}
