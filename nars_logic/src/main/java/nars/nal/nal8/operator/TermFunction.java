package nars.nal.nal8.operator;

import com.google.common.collect.Lists;
import nars.Global;
import nars.Memory;
import nars.Symbols;
import nars.io.Texts;
import nars.nal.nal1.Inheritance;
import nars.nal.nal2.Similarity;
import nars.nal.nal4.Product;
import nars.nal.nal5.Implication;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.task.Task;
import nars.task.TaskSeed;
import nars.term.Atom;
import nars.term.Compound;
import nars.term.Term;
import nars.term.Variable;
import nars.truth.DefaultTruth;
import nars.truth.Truth;

import java.util.ArrayList;
import java.util.List;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input Global and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class TermFunction<O> extends SynchOperator {

    static final Variable var=new Variable("$y");


    protected TermFunction() {
        super();
    }

    protected TermFunction(String name) {
        super(name);
    }

    public static int integer(Term x, int defaultValue)  {
        try {
            return integer(x);
        }
        catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static int integer(Term x) throws NumberFormatException {
        return Texts.i(Atom.unquote(x));
    }

    /** y = function(x) 
     * @return y, or null if unsuccessful
     */
    abstract public O function(Term... x);


    protected ArrayList<Task> result(Operation operation, Term y, Term[] x0, Term lastTerm) {


        //Variable var=new Variable("$1");
        //  Term actual_part = Similarity.make(var, y);
        //  Variable vardep=new Variable("#1");
        //Term actual_dep_part = Similarity.make(vardep, y);
//        operation=(Operation) operation.setComponent(0,
//                ((Compound)operation.getSubject()).setComponent(
//                        numArgs, y));

        //Examples:
        //      <3 --> (/,^add,1,2,_,SELF)>.
        //      <2 --> (/,^count,{a,b},_,SELF)>. :|: %1.00;0.99%
        //transform to image for perception variable introduction rule (is more efficient representation

        Product originalArgs =  operation.getTask().getTerm().arg();
        //final int numArgs = x0.length;

        Inheritance inh = Operation.result(operation.getPredicate(), originalArgs, y);

        //Implication.make(operation, actual_part, TemporalRules.ORDER_FORWARD);

        float confidence = 0.99f;
        return Lists.newArrayList(
                TaskSeed.make(nar.memory, inh).
                        truth(getResultFrequency(), getResultConfidence()).
                        budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY).
                        judgment().
                        tense(getResultTense())
            );

            /*float equal = equals(lastTerm, y);
            ArrayList<Task> rt = Lists.newArrayList(
                    m.newTask(actual, Symbols.JUDGMENT_MARK,
                            1.0f, confidence,
                            Global.DEFAULT_JUDGMENT_PRIORITY,
                            Global.DEFAULT_JUDGMENT_DURABILITY,
                            operation.getTask()));

            if (equal < 1.0f) {
                rt.add(m.newTask(operation, Symbols.JUDGMENT_MARK,
                            equal, confidence,
                            Global.DEFAULT_JUDGMENT_PRIORITY,
                            Global.DEFAULT_JUDGMENT_DURABILITY,
                            operation.getTask()));
            }
            return rt;
            */




    }

    /** default tense applied to result tasks */
    public Tense getResultTense() {
        return Tense.Present;
    }

    /** default confidence applied to result tasks */
    public float getResultFrequency() {
        return 1f;
    }


    /** default confidence applied to result tasks */
    public float getResultConfidence() {
        return 0.99f;
    }



    protected ArrayList<Task> result2(Operation operation, Term y, Term[] x0, Term lastTerm) {
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


        float confidence = operation.getTask().getConfidence();
        //TODO add a delay discount/projection for executions that happen further away from creation time

        return Lists.newArrayList(

                TaskSeed.make(nar.memory, actual).judgment()
                        .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                        .truth(getResultFrequency(), getResultConfidence())
                        .parent(operation.getTask())
                        .tense(getResultTense()),

                actual_dep_part != null ?
                        TaskSeed.make(nar.memory, actual_dep_part).judgment()
                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
                                .truth(1f, confidence)
                                .present()
                                .parent(operation.getTask()) : null

        );

    }

    @Override
    protected void noticeExecuted(Operation operation) {
        //no notice
    }

    @Override
    public List<Task> apply(final Operation operation) {

        final Memory memory = operation.getMemory();

        Term[] rawArgs = operation.args();

        int numInputs = rawArgs.length;
        if (rawArgs[numInputs - 1].equals(memory.self()))
            numInputs--;

        Term lastTerm = null;
        if (rawArgs[numInputs - 1] instanceof Variable) {
            lastTerm = rawArgs[numInputs-1];
            numInputs--;
        }

        //Term[] x0 = operation.getArgumentTerms(false, memory);
        Term[] x = operation.arg(memory);

        Object y = function(x);

        if (y == null) {
            return null;
        }

        if (y instanceof Boolean) {
            boolean by = (Boolean)y;
            y = new DefaultTruth(by ? 1 : 0, 0.99f);
        }
        if (y instanceof Truth) {
            //this will get the original input operation term, not after it has been inlined.
            Compound inputTerm = operation.getTask().getTerm();

            Task b = TaskSeed.make(memory, inputTerm).judgment().eternal().truth((Truth) y);

            nar.input(b);

            return null;
        }


        if (y instanceof Number) {
            y = (Atom.the((Number)y));
        }

        if (y instanceof Task) {
            return Lists.newArrayList((Task)y);
        }
        else if (y instanceof Term) {
            return result(operation, (Term) y, x, lastTerm);
        }


        String ys = y.toString();


        //1. try to parse as task
        char mustBePuncToBeTask = ys.charAt(ys.length()-1); //early prevention from invoking parser
        if (Symbols.isPunctuation(mustBePuncToBeTask) || mustBePuncToBeTask == ':' /* tense ending character */) {
            try {
                Task t = nar.task(ys);
                if (t != null)
                    return Lists.newArrayList(t);
            } catch (Throwable t) {
            }
        }

        //2. try to parse as term

        Term t = nar.term(ys);
        if (t != null)
            return result(operation, t, x, lastTerm);


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
                            Global.DEFAULT_JUDGMENT_PRIORITY,
                            Global.DEFAULT_JUDGMENT_DURABILITY,
                            operation.getTask()));

            if (equal < 1.0f) {
                rt.add(m.newTaskAt(operation, Symbols.JUDGMENT,
                            equal, confidence,
                            Global.DEFAULT_JUDGMENT_PRIORITY,
                            Global.DEFAULT_JUDGMENT_DURABILITY,
                            operation.getTask()));
            }
            return rt;
            */

//   return null;

//}
