package nars.operator;

import com.google.common.collect.Lists;
import nars.core.Memory;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.logic.entity.*;
import nars.logic.nal2.Similarity;
import nars.logic.nal5.Implication;
import nars.logic.nal7.TemporalRules;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;

import java.util.ArrayList;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class SynchronousFunctionOperator extends Operator {


    static final Variable var=new Variable("$1");

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
        
        if (numArgs < 2) {
            throw new RuntimeException("Requires at least 2 arguments");
        }
        
        //last argument a variable?
        Term lastTerm = args[numArgs-1];
        final boolean variable = lastTerm instanceof Variable;
        
        if(!variable) {
            return null;
        }
        
        int numParam = numArgs-1;
        Term[] x = new Term[numParam];
        System.arraycopy(args, 0, x, 0, numParam);
        
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
          
        //since Peis approach needs it to directly generate op(...,$output) =|> <$output <-> result>,
        //which wont happen on temporal induction with dependent variable for good reason,
        //because in general the two dependent variables of event1 and event2
        //can not be assumed to be related, but here we have to assume
        //it if we don't want to use the "resultof"-relation.

        Term actual_part = Similarity.make(var, y);
        Operation opart =(Operation) operation.setComponent(0,
                ((CompoundTerm)operation.getSubject()).setComponent(
                        numArgs-1, var));
        
        CompoundTerm actual =
                Sentence.termOrNull(Implication.make(opart, actual_part, TemporalRules.ORDER_FORWARD));
        if (actual == null) return null;

        return Lists.newArrayList(
                m.newTask(actual, Symbols.JUDGMENT,
                        1f, 0.99f,
                        Parameters.DEFAULT_JUDGMENT_PRIORITY,
                        Parameters.DEFAULT_JUDGMENT_DURABILITY, operation.getTask()
                ));

        //if (variable) {
        //}
        //else {
            /*float equal = equals(lastTerm, y);
            
            
            
            float confidence = 0.99f;
            ArrayList<Task> rt = Lists.newArrayList( 
                    m.newTaskAt(actual, Symbols.JUDGMENT,
                            1.0f, confidence, 
                            Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                            Parameters.DEFAULT_JUDGMENT_DURABILITY, 
                            operation.getTask()));    
            
            if (equal < 1.0f) {
                rt.add(m.newTaskAt(operation, Symbols.JUDGMENT,
                            equal, confidence, 
                            Parameters.DEFAULT_JUDGMENT_PRIORITY, 
                            Parameters.DEFAULT_JUDGMENT_DURABILITY, 
                            operation.getTask()));
            }
            return rt;
            */
            
         //   return null;
            
        //}
    }

    /** (can be overridden in subclasses) the extent to which it is truth 
     * that the 2 given terms are equal.  in other words, a distance metric
     */
    public float equals(Term a, Term b) {
        //default: Term equality
        return a.equals(b) ? 1.0f : 0.0f;
    }
}
