package nars.nal.nal8;

import com.google.common.collect.Lists;
import nars.Global;
import nars.io.Symbols;
import nars.nal.Task;
import nars.nal.nal2.Similarity;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.term.Variable;

import java.util.ArrayList;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class TermFunction<O> extends Operator  {

    static final Variable var=new Variable("$y");

    protected TermFunction(String name) {
        super(name);
    }



    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    abstract public O function(Term[] x);


    protected ArrayList<Task> result(Operation operation, Term y, Term[] x0, Term lastTerm) {
        //since Peis approach needs it to directly generate op(...,$output) =|> <$output <-> result>,
        //which wont happen on temporal induction with dependent variable for good rule,
        //because in general the two dependent variables of event1 and event2
        //can not be assumed to be related, but here we have to assume
        //it if we don't want to use the "resultof"-relation.


        Compound actual = Implication.make(
                operation.cloneWithArguments(x0, var),
                Similarity.make(var, y),
                TemporalRules.ORDER_FORWARD);

        if (actual == null) return null;

        Compound actual_dep_part = lastTerm!=null ? Similarity.make(lastTerm, y) : null;


        float confidence = operation.getTask().sentence.truth.getConfidence();
        //TODO add a delay discount/projection for executions that happen further away from creation time

        return Lists.newArrayList(

                nar.memory.newTask(actual).judgment()
                        .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                        .truth(1f, confidence)
                        .present()
                        .parent(operation.getTask())
                        .get(),

                actual_dep_part != null ?
                        nar.memory.newTask(actual_dep_part).judgment()
                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                                .truth(1f, confidence)
                                .present()
                                .parent(operation.getTask())
                                .get() : null

        );

    }

    @Override
    protected ArrayList<Task> execute(final Operation operation, final Term[] args) {

        TermFunction op = (TermFunction) operation.getOperator();

        Term[] rawArgs = operation.getArguments().term;

        int numInputs = rawArgs.length;
        if (rawArgs[numInputs - 1].equals(op.getMemory().getSelf()))
            numInputs--;

        Term lastTerm = null;
        if (rawArgs[numInputs - 1] instanceof Variable) {
            lastTerm = rawArgs[numInputs-1];
            numInputs--;
        }

        Term[] x0 = operation.getArgumentTerms(false);
        Term[] x = operation.getArgumentTerms(true);

        final Object y = function(x);

        if (y == null) return null;

        if (y instanceof Task) {
            return Lists.newArrayList((Task)y);
        }
        else if (y instanceof Term) {
            return result(operation, (Term) y, x0, lastTerm);
        }

        String ys = y.toString();
        char ysz = ys.charAt(ys.length() - 1);

        //1. try to parse as task
        try {
            Task t = nar.task(ys);
            if (t != null)
                return Lists.newArrayList(t);
        } catch (Throwable t) {
        }

        //2. try to parse as term
        Term t = nar.term(ys);
        if (t!=null)
            return result(operation, t, x0, lastTerm);

        throw new RuntimeException(this + " return value invalid: " + y);
    }

    /** the term that the output will inherit from; analogous to the 'Range' of a function in mathematical terminology */
    //protected Term getRange() {        return null;    }

    //protected int getMinArity() {        return 0;    }
    //abstract protected int getMaxArity();


    /** (can be overridden in subclasses) the extent to which it is truth 
     * that the 2 given terms are equal.  in other words, a distance metric
     */
    public float equals(Term a, Term b) {
        //default: Term equality
        return a.equals(b) ? 1.0f : 0.0f;
    }
}



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
