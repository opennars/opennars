package nars.operator;

import java.util.ArrayList;
import java.util.Arrays;
import nars.core.Parameters;
import nars.entity.Task;
import nars.io.Symbols;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Term;
import nars.language.Variable;
import nars.operator.software.Javascript;
import nars.storage.Memory;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class SynchronousFunctionOperator extends Operator {

    protected SynchronousFunctionOperator(String name) {
        super(name);
    }

    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    abstract protected Term function(Memory memory, Term[] x);
    
    /** the term that the output will inherit from; analogous to the 'Range' of a function in mathematical terminology */
    abstract protected Term getRange();
        
    //abstract protected int getMinArity();
    //abstract protected int getMaxArity();
    
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory m) {
        //TODO make memory access optional by constructor argument
        //TODO allow access to NAR instance?
        if (args.length < 2) {
            return null;
        }
        if (!(args[args.length-1] instanceof Variable)) {
            //TODO report error
            return null;
        }
        
        Term[] x = new Term[args.length-1];
        System.arraycopy(args, 0, x, 0, args.length-1);
        
        Term y = function(m, x);
        if (y == null) {
            return null;
        }
        
        m.output(Javascript.class, Arrays.toString(x) + " | " + y);
        
        Term parameterTerm = x.length == 1 ? x[0] : Product.make(x, m);
        
        Inheritance operatorInheritance = 
                Operation.make(
                    Product.make(new Term[] { 
                        parameterTerm, y
                    },
                m), this, m);
        
        Inheritance resultInheritance = Inheritance.make(operatorInheritance, getRange(), m);
        m.output(Task.class, resultInheritance);
        ArrayList<Task> results = new ArrayList<>(1);
        results.add(m.newTask(resultInheritance, Symbols.JUDGMENT_MARK, 1f, 0.99f, Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, operation.getTask()));
        return results;
    }

}
