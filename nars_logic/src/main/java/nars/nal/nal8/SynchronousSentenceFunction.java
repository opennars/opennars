package nars.nal.nal8;

import nars.Memory;
import nars.nal.Sentence;
import nars.nal.Task;
import nars.nal.term.Term;
import nars.nal.term.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/** 
 * A function which takes inputs and generates one or more Task as a result
 */
public abstract class SynchronousSentenceFunction extends Operator {


    static final Variable var=new Variable("$1");

    protected SynchronousSentenceFunction(String name) {
        super();
    }


    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    abstract protected Collection<Sentence> function(@Deprecated Memory memory, Term[] x);
    


    @Override
    protected List<Task> execute(Operation operation, Memory memory) {

        Collection<Sentence> y = function(memory, operation.arg().term);
        if (y == null) {
                return null;
            }

        List<Task> result = new ArrayList(y.size());
        for (Sentence s : y) {
            result.add( memory.newTask(s).parent( operation.getTask() ).get() );
        }

        return result;

    }

    /** (can be overridden in subclasses) the extent to which it is truth 
     * that the 2 given terms are equal.  in other words, a distance metric
     */
    public float equals(Term a, Term b) {
        //default: Term equality
        return a.equals(b) ? 1.0f : 0.0f;
    }
}
