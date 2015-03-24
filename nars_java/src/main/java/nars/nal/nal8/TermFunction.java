package nars.nal.nal8;

import nars.Memory;
import nars.Global;
import nars.nal.entity.*;
import nars.nal.nal2.Similarity;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class TermFunction extends Operator implements TermEval {

    static final Variable var=new Variable("$y");

    protected TermFunction(String name) {
        super(name);
    }



    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    @Override abstract public Term function(Term[] x);




    protected static Compound[] executeTerm(Operation operation) {
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

        Term y = op.function(x);
        if (y == null) {
            return null;
        }

        //since Peis approach needs it to directly generate op(...,$output) =|> <$output <-> result>,
        //which wont happen on temporal induction with dependent variable for good rule,
        //because in general the two dependent variables of event1 and event2
        //can not be assumed to be related, but here we have to assume
        //it if we don't want to use the "resultof"-relation.


        return new Compound[] {

                Implication.make(
                            operation.cloneWithArguments(x0, var),
                            Similarity.make(var, y),
                        TemporalRules.ORDER_FORWARD),

                lastTerm!=null ? Similarity.make(lastTerm, y) : null
        };
    }

    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory m) {



        Compound[] e = executeTerm(operation);
        Compound actual = e[0];
        Compound actual_dep_part = e[1];
        if (actual == null) return null;


        float confidence = operation.getTask().sentence.truth.getConfidence();
        //TODO add a delay discount/projection for executions that happen further away from creation time

        return newArrayList(

                m.newTask(actual).judgment()
                        .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                        .truth(1f, confidence)
                        .present()
                        .parent(operation.getTask())
                        .get(),

                actual_dep_part!=null?
                        m.newTask(actual_dep_part).judgment()
                        .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                        .truth(1f, confidence)
                        .present()
                        .parent(operation.getTask())
                        .get() : null

        );
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
