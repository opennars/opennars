package nars.operator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.Task;
import nars.io.Symbols;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Term;
import nars.language.Variable;


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
        int numArgs = args.length;
        if (args[args.length-1].equals(Term.SELF))
            numArgs--;
        
        if (numArgs < 2) {
            throw new RuntimeException("Requires at least 2 arguments");
        }
        if (!(args[numArgs-1] instanceof Variable)) {
            //TODO report error
            throw new RuntimeException("Last argument must be a Variable");
        }
        
        Term[] x = new Term[numArgs-1];
        System.arraycopy(args, 0, x, 0, numArgs-1);
        
        Term y;
        //try {
            y = function(m, x);
            if (y == null) {
                return null;
            }
            //m.emit(SynchronousFunctionOperator.class, Arrays.toString(x) + " | " + y);
        /*}
        catch (Exception e) {
            throw e;
        }*/
        
        
        Term parameterTerm = x.length == 1 ? x[0] : new Product(x);
        
        Inheritance operatorInheritance = 
                Operation.make(new Product(new Term[]{parameterTerm, y}), this);
        
        //wraps the result in getRange() inheritance:
        //Inheritance resultInheritance = Inheritance.make(operatorInheritance, getRange());
        //m.emit(Task.class, operatorInheritance);
        
        return Lists.newArrayList( 
                m.newTask(operatorInheritance, Symbols.JUDGMENT_MARK, 
                        1f, 0.99f, 
                        Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                        Parameters.DEFAULT_JUDGMENT_DURABILITY, operation.getTask()
        ));    
    }

}
