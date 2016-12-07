package nars.operator;

import com.google.common.collect.Lists;
import nars.config.Parameters;
import nars.entity.Task;
import nars.io.Symbols;
import nars.language.*;
import nars.operator.misc.Javascript;
import nars.storage.Memory;

import java.util.ArrayList;


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
    @Deprecated abstract protected Term getRange();
        
    //abstract protected int getMinArity();
    //abstract protected int getMaxArity();
    
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory m) {
        //TODO make memory access optional by constructor argument
        //TODO allow access to NAR instance?
        int numArgs = args.length;
        if (args[args.length-1].equals(Term.SELF))
            numArgs--;
        
        if (numArgs < 1) {
            throw new RuntimeException("Requires at least 1 arguments");
        }
        
        if (numArgs < 2 && !(this instanceof Javascript)) {
            throw new RuntimeException("Requires at least 2 arguments");
        }
        
        //last argument a variable?
        Term lastTerm = args[numArgs-1];
        boolean variable = lastTerm instanceof Variable;
        
        if(!variable && !(this instanceof Javascript)) { 
            throw new RuntimeException("output can not be specified");
        }
        
        
        
        int numParam = numArgs-1;
        
        if(this instanceof Javascript && !variable) {
            numParam++;
        }
        
        Term[] x = new Term[numParam];
        System.arraycopy(args, 0, x, 0, numParam);
        
        Term y;
        //try {
            y = function(m, x);
            if (y == null) {
                return null;
            }
            if(!variable && this instanceof Javascript) {
                return null;
            }
            //m.emit(SynchronousFunctionOperator.class, Arrays.toString(x) + " | " + y);
        /*}
        catch (Exception e) {
            throw e;
        }*/
          
        Variable var=new Variable("$1");
      //  Term actual_part = Similarity.make(var, y);
      //  Variable vardep=new Variable("#1");
        //Term actual_dep_part = Similarity.make(vardep, y);
        operation=(Operation) operation.setComponent(0, 
                ((CompoundTerm)operation.getSubject()).setComponent(
                        numArgs-1, y, m), m); 
        
        //<3 --> (/,^add,1,2,_,SELF)>.
        //transform to image for perception variable introduction rule (is more efficient representation
        ImageExt ing=(ImageExt) ImageExt.make((Product)operation.getSubject(),operation.getPredicate(), (short)(numArgs-1));
        Inheritance inh=Inheritance.make(y, ing);
        Term actual=inh; //Implication.make(operation, actual_part, TemporalRules.ORDER_FORWARD);

        float confidence = 0.99f;
        if (variable) {
            return Lists.newArrayList( 
                    m.newTask(actual, Symbols.JUDGMENT_MARK, 
                            1f, confidence, 
                            Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                            Parameters.DEFAULT_JUDGMENT_DURABILITY, operation.getTask()
            ));    
        }
        else {
            /*float equal = equals(lastTerm, y);
            ArrayList<Task> rt = Lists.newArrayList( 
                    m.newTask(actual, Symbols.JUDGMENT_MARK, 
                            1.0f, confidence, 
                            Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                            Parameters.DEFAULT_JUDGMENT_DURABILITY, 
                            operation.getTask()));    
            
            if (equal < 1.0f) {
                rt.add(m.newTask(operation, Symbols.JUDGMENT_MARK, 
                            equal, confidence, 
                            Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                            Parameters.DEFAULT_JUDGMENT_DURABILITY, 
                            operation.getTask()));
            }
            return rt;
            */
            
            return null;
            
        }
    }

    /** (can be overridden in subclasses) the extent to which it is truth 
     * that the 2 given terms are equal.  in other words, a distance metric
     */
    public float equals(Term a, Term b) {
        //default: Term equality
        return a.equals(b) ? 1.0f : 0.0f;
    }
}
