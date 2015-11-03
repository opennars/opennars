package nars.op.mental;

import com.google.common.util.concurrent.AtomicDouble;
import nars.*;
import nars.budget.Budget;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Temporal;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.premise.Premise;
import nars.process.NAL;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Compound;
import nars.term.Term;
import nars.truth.Truth;

import java.util.Arrays;
import java.util.Random;

/**
 * https://www.youtube.com/watch?v=ia4wMU-vfrw
 * <p>
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 */
public class InternalExperience {


    public static float MINIMUM_BUDGET_SUMMARY_TO_CREATE = 0.5f; //0.92
    public static float MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE = 0.92f;

    //internal experience has less durability?
    public static final float INTERNAL_EXPERIENCE_PROBABILITY = 0.01f;

    //less probable form
    public static final float INTERNAL_EXPERIENCE_RARE_PROBABILITY =
            INTERNAL_EXPERIENCE_PROBABILITY / 4.0f;


    //internal experience has less durability?
    public static float INTERNAL_EXPERIENCE_DURABILITY_MUL = 0.1f; //0.1
    //internal experience has less priority?
    public static float INTERNAL_EXPERIENCE_PRIORITY_MUL = 0.1f; //0.1

    //dont use internal experience for want and believe if this setting is true
    public static final boolean enableWantBelieve = true; //wut, semantic issue ^^

    //
    public static boolean OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY = false; //https://groups.google.com/forum/#!topic/open-nars/DVE5FJd7FaM

    @Deprecated
    public static boolean enabled = true;


    /** minimum expectation necessary to create a concept
     *  original value: 0.66
     * */
    public AtomicDouble conceptCreationExpectation = new AtomicDouble(0.66);

    public boolean isAllowNewStrategy() {
        return !OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY;
    }

    public void setAllowNewStrategy(boolean val) {
        OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY = !val;
    }

//    public boolean isEnableWantBelieve() {
//        return enableWantBelieve;
//    }
//    public void setEnableWantBelieve(boolean val) {
//        enableWantBelieve =val;
//    }

    public double getMinCreationBudgetSummary() {
        return MINIMUM_BUDGET_SUMMARY_TO_CREATE;
    }

    public void setMinCreationBudgetSummary(double val) {
        MINIMUM_BUDGET_SUMMARY_TO_CREATE = (float) val;
    }

    public double getMinCreationBudgetSummaryWonderEvaluate() {
        return MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE;
    }

    public void setMinCreationBudgetSummaryWonderEvaluate(double val) {
        MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE = (float) val;
    }

    //public static boolean enabled=true;


    public final static Operator believe = Operator.the("believe");
    public final static Operator want = Operator.the("want");
    public final static Operator wonder = Operator.the("wonder");
    public final static Operator evaluate = Operator.the("evaluate");
    public final static Operator anticipate = Operator.the("anticipate");


    /**
     * whether it is full internal experience, or minimal (false)
     */
    public boolean isFull() {
        return false;
    }


    public InternalExperience(NAR n) {
        super();

        n.memory.eventTaskProcess.on(tp -> {

            final Task task = tp.getTask();

            //old strategy always, new strategy only for QUESTION and QUEST:
            ///final char punc = task.getPunctuation();

            if (OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY ||
                    (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && task.isQuestOrQuestion())) {
                experienceFromTaskInternal(tp, task, isFull());
            }
            //we also need Mr task process to be able to have the task process, this is a hack..

        });
        n.memory.eventConceptProcess.on(p -> {
            final Task belief = p.getBelief();
            if (belief == null) return;

            final Task task = p.getTask();
            final Random r = p.getRandom();

            if (r.nextFloat() < INTERNAL_EXPERIENCE_RARE_PROBABILITY) {
                nonInnate(belief, task, p, randomNonInnate(r) );
            }

            final Term beliefTerm = belief.getTerm();

            if (beliefTerm instanceof Implication && r.nextFloat() <= INTERNAL_EXPERIENCE_PROBABILITY) {
                internalizeImplication(task, p, (Implication) beliefTerm);
            }
        });
    }

    public static Operation toTerm(final Sentence s, final NAL mem, float conceptCreationExpectation) {
        return toTerm(s, mem, conceptCreationExpectation, enableWantBelieve);
    }

    public static Operation toTerm(final Sentence s, final NAL nal, float conceptCreationExpectation, boolean enableWantBelieve) {
        Operator opTerm;
        switch (s.getPunctuation()) {
            case Symbols.JUDGMENT:
                if (!enableWantBelieve)
                    return null;
                opTerm = believe;
                break;
            case Symbols.GOAL:
                if (!enableWantBelieve)
                    return null;
                opTerm = want;
                break;
            case Symbols.QUESTION:
                opTerm = wonder;
                break;
            case Symbols.QUEST:
                opTerm = evaluate;
                break;
            default:
                return null;
        }

        final Truth tr = s.getTruth();
        Term[] arg = new Term[1 + (tr == null ? 1 : 2)];
        arg[0] = s.getTerm();
        int k = 1;

        if (tr != null) {
            arg[k++] = tr.toWordTerm(conceptCreationExpectation);
        }
        arg[k] = nal.self();

        Operation operation = $.opr(opTerm, arg);
        if (operation == null) {
            throw new RuntimeException("Unable to create Inheritance: " + opTerm + ", " + Arrays.toString(arg));
        }
        return operation;
    }



    public static Operator randomNonInnate(Random r) {
        return nonInnateBeliefOperators[r.nextInt(nonInnateBeliefOperators.length)];
    }


//    public Task experienceFromBelief(NAL nal, Budget b, Sentence belief) {
//        return experienceFromTask(nal,
//                new Task(belief.clone(), b, null),
//                false);
//    }

//    public Task experienceFromTask(NAL nal, Task task, boolean full) {
//        if (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
//            return experienceFromTaskInternal(nal, task, full);
//        }
//        return null;
//    }

    protected Task experienceFromTaskInternal(NAL nal, Task task, boolean full) {

        // if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY ||
        //         (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation==Symbols.QUESTION || task.sentence.punctuation==Symbols.QUEST))) {
        {
            //char punc = task.getPunctuation();
            final Budget b = task.getBudget();
            if (task.isQuestOrQuestion()) {
                if (b.summaryLessThan(MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE)) {
                    return null;
                }
            } else if (b.summaryLessThan(MINIMUM_BUDGET_SUMMARY_TO_CREATE)) {
                return null;
            }
        }

        Term content = task.getTerm();
        // to prevent infinite recursions
        if (content instanceof Operation/* ||  Memory.randomNumber.nextFloat()>Global.INTERNAL_EXPERIENCE_PROBABILITY*/) {
            return null;
        }

        Operation ret = toTerm(task, nal, conceptCreationExpectation.floatValue());
        if (ret == null) {
            return null;
        }

        float pri = Global.DEFAULT_JUDGMENT_PRIORITY * INTERNAL_EXPERIENCE_PRIORITY_MUL;
        float dur = Global.DEFAULT_JUDGMENT_DURABILITY * INTERNAL_EXPERIENCE_DURABILITY_MUL;
        if (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
            pri *= INTERNAL_EXPERIENCE_PRIORITY_MUL;
            dur *= INTERNAL_EXPERIENCE_DURABILITY_MUL;
        }

        Task<Compound<?>> t;
        nal.nar().input(t = nal.newTask(ret).judgment()
                        .parent(task).occurr(nal.time())
                        .truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                        .budget(pri, dur)
                        .reason("Remembered Action (Internal Experience)"));
        return t;
    }


    final static Operator[] nonInnateBeliefOperators = new Operator[]{
            Operator.the("remind"),
            Operator.the("doubt"),
            Operator.the("consider"),
            Operator.the("evaluate"),
            Operator.the("hestitate"),
            Operator.the("wonder"),
            Operator.the("belief"),
            Operator.the("want")
    };

    private void internalizeImplication(Task task, NAL nal, Implication beliefTerm) {
        Term taskTerm = task.getTerm();
        Implication imp = beliefTerm;
        if (imp.getTemporalOrder() == Temporal.ORDER_FORWARD) {
            //1. check if its (&/,term,+i1,...,+in) =/> anticipateTerm form:
            boolean valid = true;
            Term impsub = imp.getSubject();
            if (impsub instanceof Conjunction) {
                Conjunction conj = (Conjunction) impsub;
                if (!conj.term(0).equals(taskTerm)) {
                    valid = false; //the expected needed term is not included
                }
            } else {
                if (!impsub.equals(taskTerm)) {
                    valid = false;
                }
            }

            //TODO use interval?


            if (valid) {
                long interval = (impsub instanceof Interval ? ((Interval)impsub).duration() : 0);

                beliefReasonDerive(task,
                        $.opr(Product.only(imp.getPredicate()), anticipate),
                        nal, interval);
            }
        }
    }

    private void nonInnate(Sentence belief, Task task, NAL nal, Operator op) {
        //the operators which dont have a innate belief
        //also get a chance to reveal its effects to the system this way

            beliefReasonDerive(task,
                    $.opr(Product.only(belief.getTerm()), op),
                    nal, 0);
    }

    protected static void beliefReasonDerive(Task parent, Compound new_term, Premise p, long delay) {

        //TODO should this be a mew stamp or attached to parent.. originally it was a fresh new stamp from memory

        long now = p.time();

        p.nar().input(p.newTask(new_term).goal().truth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                        .budget(Global.DEFAULT_GOAL_PRIORITY * INTERNAL_EXPERIENCE_PRIORITY_MUL,
                                Global.DEFAULT_GOAL_DURABILITY * INTERNAL_EXPERIENCE_DURABILITY_MUL)
                        .parent(parent)
                        .time(now, now + delay));

    }


    /*public enum InternalExperienceMode {
        None, Minimal, Full
    }*/


    //TODO
    public static void experienceFromBelief(Memory memory, Task task, Sentence belief) {
        //Task T=new Task(belief.clone(),new Budget(task),null);
        ///InternalExperienceFromTask(memory,T,false);
    }

//    public static void InternalExperienceFromTask(Memory memory, Task task, boolean full) {
//        if(!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
//            InternalExperienceFromTaskInternal(memory,task,full);
//        }
//    }
//
//    public static boolean InternalExperienceFromTaskInternal(Memory memory, Task task, boolean full) {
//        if(!enabled) {
//            return false;
//        }
//
//        // if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY ||
//        //         (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation==Symbols.QUESTION || task.sentence.punctuation==Symbols.QUEST))) {
//        {
//            if(task.sentence.punctuation == Symbols.QUESTION || task.sentence.punctuation == Symbols.QUEST) {
//                if(task.budget.summary()<MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE) {
//                    return false;
//                }
//            }
//            else
//            if(task.budget.summary()<MINIMUM_BUDGET_SUMMARY_TO_CREATE) {
//                return false;
//            }
//        }
//
//        Term content=task.getTerm();
//        // to prevent infinite recursions
//        if (content instanceof Operation/* ||  Memory.randomNumber.nextDouble()>Global.INTERNAL_EXPERIENCE_PROBABILITY*/) {
//            return true;
//        }
//        Sentence sentence = task.sentence;
//        TruthValue truth = new DefaultTruth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
//        Stamp stamp = task.sentence.stamp.clone();
//        stamp.setOccurrenceTime(memory.time());
//        Term ret=toTerm(sentence, memory);
//        if (ret==null) {
//            return true;
//        }
//        Sentence j = new Sentence(ret, Symbols.JUDGMENT, truth, stamp);
//        Budget newbudget=new Budget(
//                Global.DEFAULT_JUDGMENT_CONFIDENCE*INTERNAL_EXPERIENCE_PRIORITY_MUL,
//                Global.DEFAULT_JUDGMENT_PRIORITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
//                BudgetFunctions.truthToQuality(truth));
//
//        if(!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
//            newbudget.setPriority(task.getPriority()*INTERNAL_EXPERIENCE_PRIORITY_MUL);
//            newbudget.setDurability(task.getDurability()*INTERNAL_EXPERIENCE_DURABILITY_MUL);
//        }
//
//        Task newTask = new Task(j, (Budget) newbudget,
//                full ? null : task);
//        memory.addNewTask(newTask, "Remembered Action (Internal Experience)");
//        return false;
//    }
}
