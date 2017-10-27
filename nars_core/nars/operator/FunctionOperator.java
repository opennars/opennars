package nars.operator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import nars.storage.Memory;
import nars.config.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.BudgetFunctions.truthToQuality;
import nars.io.Symbols;
import nars.language.CompoundTerm;
import nars.language.ImageExt;
import nars.language.Inheritance;
import nars.language.Product;
import nars.language.Term;
import nars.language.Variable;
//import nars.operator.misc.Javascript;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class FunctionOperator extends Operator {

    
    protected FunctionOperator(String name) {
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
        int numArgs = args.length -1;
        
        if (numArgs < 1) {
            throw new RuntimeException("Requires at least 1 arguments");
        }
        
        if (numArgs < 2 /*&& !(this instanceof Javascript)*/) {
            throw new RuntimeException("Requires at least 2 arguments");
        }
        
        //last argument a variable?
        Term lastTerm = args[numArgs];
        boolean variable = lastTerm instanceof Variable;
        
        if(!variable /*&& !(this instanceof Javascript)*/) { 
            throw new RuntimeException("output can not be specified");
        }
        
        
        
        int numParam = numArgs-1;
        
        /*if(this instanceof Javascript && !variable) {
            numParam++;
        }*/
        
        Term[] x = new Term[numParam];
        System.arraycopy(args, 1, x, 0, numParam);
        
        Term y;
        //try {
            y = function(m, x);
            if (y == null) {
                return null;
            }
            /*if(!variable && this instanceof Javascript) {
                return null;
            }*/
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
                        numArgs, y, m), m); 

        float confidence = Parameters.DEFAULT_JUDGMENT_CONFIDENCE;
        if (variable) {
            Sentence s = new Sentence(operation, 
                                      Symbols.JUDGMENT_MARK,
                                      new TruthValue(1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE),
                                      new Stamp(m));
            return Lists.newArrayList( 
                    new Task(s, 
                            new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                                            Parameters.DEFAULT_FEEDBACK_DURABILITY,
                                            truthToQuality(s.getTruth())), 
                            true));
        }
        else {
            
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
