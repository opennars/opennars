package nars.logic.nal8;

import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.entity.*;
import nars.logic.nal2.Similarity;
import nars.logic.nal5.Implication;
import nars.logic.nal7.TemporalRules;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input parameters and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class SynchronousTermFunction extends Operator implements TermEval {

    static final Variable inVar=new Variable("$x");
    static final Variable var=new Variable("$y");

    protected SynchronousTermFunction(String name) {
        super(name);
    }
//    protected SynchronousFunctionOperator(String name, boolean requireVariable) {
//
//    }


    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    @Override abstract public Term function(Memory memory, Term[] x);

    /** the term that the output will inherit from; analogous to the 'Range' of a function in mathematical terminology */
    protected Term getRange() {
        return null;
    }
        
    protected int getMinArity() {
        return 0;
    }
    //abstract protected int getMaxArity();

    protected static Term evaluate(final Memory m, final Term x) {
        if (x instanceof Operation) {
            final Operation o = (Operation)x;
            final Operator op = o.getOperator();
            if (op instanceof TermEval) {
                CompoundTerm[] ee = ((SynchronousTermFunction) op).executeTerm(o);
                if (ee!=null)
                    return ee[0];
            }
        }

        if (x instanceof CompoundTerm) {
            CompoundTerm ct = (CompoundTerm)x;
            Term[] r = new Term[ct.size()];
            boolean modified = false;
            int j = 0;
            for (final Term w : ct.term) {
                Term v = evaluate(m, w);
                if ((v!=null) && (v!=w)) {
                    r[j] = v;
                    modified = true;
                }
                else {
                    r[j] = w;
                }
                j++;
            }
            if (modified)
                return ct.clone(r);
        }

        return x; //return as-is
    }

    protected static CompoundTerm[] executeTerm(Operation operation) {
        SynchronousTermFunction op = (SynchronousTermFunction) operation.getOperator();

        Term[] args = operation.getArgumentTerms();

        //TODO make memory access optional by constructor argument
        //TODO allow access to NAR instance?
        int numArgs = args.length;
        if (args[args.length - 1].equals(op.getMemory().getSelf()))
            numArgs--;

        if (numArgs < (op.getMinArity())) {
            throw new RuntimeException("Requires at least 2 arguments");
        }

        //last argument a variable?
        Term lastTerm = args[numArgs-1];
        boolean resultVariable = lastTerm instanceof Variable;



        final int numParam = numArgs+(resultVariable ? 0 : 1);
        Term[] x = new Term[numParam];

        if (!resultVariable) {
            //evaluate parameters
            for (int i = 0; i < numParam-1; i++)
                x[i] = evaluate(op.getMemory(), args[i]);
            lastTerm = x[numParam-1] = inVar;
            resultVariable = true;
            numArgs++;
        }
        else {
            //use the terms as-is
            System.arraycopy(args, 0, x, 0, numParam);
        }


        Term y = op.function(op.getMemory(), x);
        if (y == null) {
            return null;
        }


        //since Peis approach needs it to directly generate op(...,$output) =|> <$output <-> result>,
        //which wont happen on temporal induction with dependent variable for good reason,
        //because in general the two dependent variables of event1 and event2
        //can not be assumed to be related, but here we have to assume
        //it if we don't want to use the "resultof"-relation.

        Term actual_part = Similarity.make(var, y);
        Operation opart =(Operation) operation.setComponent(0,
                ((CompoundTerm)operation.getSubject()).setComponent(
                        numArgs-1, var));


        CompoundTerm actual_dep_part = resultVariable ? Similarity.make(lastTerm, y) : null;


        CompoundTerm actual = Sentence.termOrNull(Implication.make(opart, actual_part, TemporalRules.ORDER_FORWARD));
        return new CompoundTerm[] { actual, actual_dep_part };
    }

    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory m) {



        CompoundTerm[] e = executeTerm(operation);
        CompoundTerm actual = e[0];
        CompoundTerm actual_dep_part = e[1];
        if (actual == null) return null;


        float confidence = 0.99f;

        return newArrayList(

                m.newTask(actual).judgment()
                        .budget(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY)
                        .truth(1f, confidence)
                        .present()
                        .parent(operation.getTask())
                        .get(),

                actual_dep_part!=null?
                        m.newTask(actual_dep_part).judgment()
                        .budget(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY)
                        .truth(1f, confidence)
                        .present()
                        .parent(operation.getTask())
                        .get() : null

        );
    }

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
