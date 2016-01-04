package nars.nal.nal8.operator;

import com.google.common.collect.Lists;
import nars.NAR;
import nars.Symbols;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Execution;
import nars.nal.nal8.Operator;
import nars.task.MutableTask;
import nars.task.Task;
import nars.term.Term;
import nars.term.atom.Atom;
import nars.term.compile.TermBuilder;
import nars.term.compound.Compound;
import nars.truth.DefaultTruth;
import nars.truth.Truth;
import nars.util.Texts;

import java.util.List;


/** 
 * Superclass of functions that execute synchronously (blocking, in thread) and take
 * N input Global and one variable argument (as the final argument), generating a new task
 * with the result of the function substituted in the variable's place.
 */
public abstract class TermFunction<O> extends SyncOperator {

    private final float feedbackPriorityMultiplier = 1.0f;
    private final float feedbackDurabilityMultiplier = 1.0f;

    protected TermFunction() {
    }

    protected TermFunction(Term name) {
        super(name);
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

    public static boolean isPunctuation(char c) {
        switch (c) {
            case Symbols.JUDGMENT:
            case Symbols.GOAL:
            case Symbols.QUEST:
            case Symbols.QUESTION:
                return true;
        }
        return false;
    }

    /** y = function(x) 
     * @return y, or null if unsuccessful
     * @param x
     * @param i
     */
    public abstract O function(Compound x, TermBuilder i);


    protected List<Task> result(NAR nar, Task opTask, Term y/*, Term[] x0, Term lastTerm*/) {

        Compound operation = opTask.term();

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


        //final int numArgs = x0.length;

        Term inh = Operator.result(operation, y);
        if ((!(inh instanceof Compound))) {
            //TODO wrap a non-Compound result as some kind of statement
            return null;
        }

        //Implication.make(operation, actual_part, TemporalRules.ORDER_FORWARD);

        return Lists.newArrayList(
                Operator.feedback(
                    new MutableTask(inh).
                        judgment().
                        truth(getResultFrequency(), getResultConfidence()).
                        tense(getResultTense(), nar.memory), opTask, feedbackPriorityMultiplier, feedbackDurabilityMultiplier)
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
        return 1.0f;
    }


    /** default confidence applied to result tasks */
    public float getResultConfidence() {
        return 0.99f;
    }



//    protected ArrayList<Task> result2(Operation operation, Term y, Term[] x0, Term lastTerm) {
//        //since Peis approach needs it to directly generate op(...,$output) =|> <$output <-> result>,
//        //which wont happen on temporal induction with dependent variable for good rule,
//        //because in general the two dependent variables of event1 and event2
//        //can not be assumed to be related, but here we have to assume
//        //it if we don't want to use the "resultof"-relation.
//
//
//        Compound actual = Implication.make(
//                operation.cloneWithArguments(x0, var),
//                Similarity.make(var, y),
//                TemporalRules.ORDER_FORWARD);
//
//        if (actual == null) return null;
//
//        Compound actual_dep_part = lastTerm!=null ? Similarity.make(lastTerm, y) : null;
//
//
//        float confidence = operation.getTask().getConfidence();
//        //TODO add a delay discount/projection for executions that happen further away from creation time
//
//        return Lists.newArrayList(
//
//                TaskSeed.make(nar.memory, actual).judgment()
//                        .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
//                        .truth(getResultFrequency(), getResultConfidence())
//                        .parent(operation.getTask())
//                        .tense(getResultTense()),
//
//                actual_dep_part != null ?
//                        TaskSeed.make(nar.memory, actual_dep_part).judgment()
//                                .budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY)
//                                .truth(1f, confidence)
//                                .present()
//                                .parent(operation.getTask()) : null
//
//        );
//
//    }

//    @Override
//    protected void noticeExecuted(Task<Operation> operation) {
//        //no notice
//    }




    @Override
    public void execute(Execution e) {

        Task opTask = e.task;
        Compound operation = opTask.term();

        //Term opTerm = Compounds.operatorTerm(operation);
        //Term[] x = Compounds.args(operation).terms();

        //Memory memory = nar.memory;

//        int numInputs = x.length;
//        if (x[numInputs - 1].equals(memory.self()))
//            numInputs--;
//
//        Term lastTerm = null;
//        if (x[numInputs - 1] instanceof Variable) {
//            lastTerm = x[numInputs-1];
//            numInputs--;
//        }

        //Term[] x0 = operation.getArgumentTerms(false, memory);


        Object y = function(Operator.opArgs(operation), e.nar.index());
        if (y == null) {
            return;
        }

        if (y instanceof Boolean) {
            boolean by = (Boolean)y;
            y = new DefaultTruth(by ? 1 : 0, 0.99f);
        }
        if (y instanceof Truth) {
            //this will get the original input operation term, not after it has been inlined.

            e.nar.input(
                new MutableTask(operation).judgment()
                        .truth((Truth) y).eternal()
            );

            return;
        }


        if (y instanceof Number) {
            y = (Atom.the((Number)y));
        }

        if (y instanceof Task) {
            e.feedback( Lists.newArrayList((Task)y) );
            return;
        }
        if (y instanceof Term) {
            e.feedback( result(e.nar, opTask, (Term) y/*, x, lastTerm*/) );
            return;
        }


        String ys = y.toString();


        //1. try to parse as task
        char mustBePuncToBeTask = ys.charAt(ys.length()-1); //early prevention from invoking parser
        if (isPunctuation(mustBePuncToBeTask) || mustBePuncToBeTask == ':' /* tense ending character */) {
            try {
                Task t = e.nar.task(ys);
                if (t != null) {
                    e.feedback( t );
                    return;
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        //2. try to parse as term

        Term t = Atom.the(ys, true);

        if (t != null) {
            e.feedback( result(e.nar, opTask, t/*, x, lastTerm*/) );
        }

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
