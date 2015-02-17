package nars.logic.nal8;

import nars.core.Memory;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.entity.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/** 
 * A function which takes inputs and generates one or more Task as a result
 */
public abstract class SynchronousSentenceFunction extends Operator {


    static final Variable var=new Variable("$1");

    protected SynchronousSentenceFunction(String name) {
        super(name);
    }


    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    abstract protected Collection<Sentence> function(Memory memory, Term[] x);
    


    @Override
    protected List<Task> execute(Operation operation, Term[] args, Memory m) {

        Collection<Sentence> y = function(m, args);
        if (y == null) {
                return null;
            }

        List<Task> result = new ArrayList(y.size());
        for (Sentence s : y) {
            result.add( m.newTask(s).parent( operation.getTask() ).get() );
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
