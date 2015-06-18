package nars.op.mental;

import nars.*;
import nars.event.NARReaction;
import nars.nal.*;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.AbstractInterval;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal8.Operation;
import nars.nal.process.TaskProcess;
import nars.nal.term.Atom;
import nars.nal.term.Compound;
import nars.nal.term.Term;

import java.util.Arrays;
import java.util.Random;

/**
 *
 * https://www.youtube.com/watch?v=ia4wMU-vfrw
 *
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 */
public class InternalExperience extends NARReaction {
        
    
    
    public static float MINIMUM_BUDGET_SUMMARY_TO_CREATE=0.5f; //0.92
    public static float MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE=0.92f;
    
    //internal experience has less durability?
    public static final float INTERNAL_EXPERIENCE_PROBABILITY=0.05f;
    
    //less probable form
    public static final float INTERNAL_EXPERIENCE_RARE_PROBABILITY = 
            INTERNAL_EXPERIENCE_PROBABILITY/ 4.0f;




//internal experience has less durability?
    public static float INTERNAL_EXPERIENCE_DURABILITY_MUL=0.1f; //0.1 
    //internal experience has less priority?
    public static float INTERNAL_EXPERIENCE_PRIORITY_MUL=0.1f; //0.1

    //dont use internal experience for want and believe if this setting is true
    public static final boolean enableWantBelieve =true; //wut, semantic issue ^^
    
    //
    public static boolean OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY=false; //https://groups.google.com/forum/#!topic/open-nars/DVE5FJd7FaM

    @Deprecated public static boolean enabled = true;


    public boolean isAllowNewStrategy() {
        return !OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY;
    }
    public void setAllowNewStrategy(boolean val) {
        OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY=!val;
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
        MINIMUM_BUDGET_SUMMARY_TO_CREATE=(float) val;
    }
    
    public double getMinCreationBudgetSummaryWonderEvaluate() {
        return MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE;
    }
    public void setMinCreationBudgetSummaryWonderEvaluate(double val) {
        MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE=(float) val;
    }
    
    //public static boolean enabled=true;

    private Memory memory;
    public final static Atom believe = Atom.the("believe");
    public final static Atom want = Atom.the("want");;
    public final static Atom wonder = Atom.the("wonder");;
    public final static Atom evaluate = Atom.the("evaluate");;
    public final static Atom anticipate = Atom.the("anticipate");




    /** whether it is full internal experience, or minimal (false) */
    public boolean isFull() {
        return false;
    }


    protected InternalExperience(NAR n, Class... events) {
        super(n, events);
        this.memory = n.memory;
    }
    public InternalExperience(NAR n) {
        this(n, TaskProcess.class);
    }

    public static Operation toTerm(final Sentence s, final NAL mem) {
        return toTerm(s, mem, enableWantBelieve);
    }

    public static Operation toTerm(final Sentence s, final NAL nal, boolean enableWantBelieve) {
        Atom opTerm;
        switch (s.punctuation) {
            case Symbols.JUDGMENT:
                if(!enableWantBelieve)
                    return null;
                opTerm = believe;
                break;
            case Symbols.GOAL:
                if(!enableWantBelieve)
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
        
        Term[] arg = new Term[ 1 + (s.truth==null ? 1 : 2) ];
        arg[0]=s.getTerm();
        int k = 1;

        if (s.truth != null) {
            final float t = nal.memory.param.conceptCreationExpectation.floatValue();
            arg[k++] = s.truth.toWordTerm(t);
        }
        arg[k] = nal.self();
        
        Operation operation = Operation.make(opTerm, Product.make(arg));
        if (operation == null) {
            throw new RuntimeException("Unable to create Inheritance: " + opTerm + ", " + Arrays.toString(arg));
        }
        return operation;
    }


    @Override
    public void event(Class event, Object[] a) {


        if (event==TaskProcess.class) {

            Task task = (Task)a[0];  
            TaskProcess tp = (TaskProcess)a[1];

            //old strategy always, new strategy only for QUESTION and QUEST:
            char punc = task.sentence.punctuation;
            if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY || (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (punc == Symbols.QUESTION || punc == Symbols.QUEST))) {
                experienceFromTaskInternal(tp, task, isFull());
            }
        }
        else if (event == Events.BeliefReason.class) {
            //belief, beliefTerm, taskTerm, nal
            Sentence belief = (Sentence)a[0];
            Task task = (Task)a[1];
            NAL nal = (NAL)a[2];
            beliefReason(belief, task, nal);
        }
    }

//    public Task experienceFromBelief(NAL nal, Budget b, Sentence belief) {
//        return experienceFromTask(nal,
//                new Task(belief.clone(), b, null),
//                false);
//    }
    
    public Task experienceFromTask(NAL nal, Task task, boolean full) {
        if(!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
            return experienceFromTaskInternal(nal, task, full);
        }
        return null;
    }

    protected Task experienceFromTaskInternal(NAL nal, Task task, boolean full) {

       // if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY ||
       //         (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation==Symbols.QUESTION || task.sentence.punctuation==Symbols.QUEST))) {
        {
            char punc = task.sentence.punctuation;
            if(punc == Symbols.QUESTION || punc == Symbols.QUEST) {
                if(task.summary()<MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE) {
                    return null;
                }
            }
            else
            if(task.summaryLessThan(MINIMUM_BUDGET_SUMMARY_TO_CREATE)) {
                return null;
            }
        }
        
        Term content=task.getTerm();
        // to prevent infinite recursions
        if (content instanceof Operation/* ||  Memory.randomNumber.nextDouble()>Global.INTERNAL_EXPERIENCE_PROBABILITY*/) {
            return null;
        }
        Sentence sentence = task.sentence;
        Operation ret = toTerm(sentence, nal);
        if (ret==null) {
            return null;
        }

        float pri = Global.DEFAULT_JUDGMENT_CONFIDENCE * INTERNAL_EXPERIENCE_PRIORITY_MUL;
        float dur = Global.DEFAULT_JUDGMENT_PRIORITY * INTERNAL_EXPERIENCE_DURABILITY_MUL;
        if(!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
            pri*=INTERNAL_EXPERIENCE_PRIORITY_MUL;
            dur*=INTERNAL_EXPERIENCE_DURABILITY_MUL;
        }

        return nal.deriveSingle(
                nal.newTask(ret).judgment()
                        .parent(full ? null : task, nal.time())
                        .truth(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                        .budget(pri, dur)
                        .reason("Remembered Action (Internal Experience)")
        );
    }


    final static String[] nonInnateBeliefOperators = new String[] {
        "remind","doubt","consider","evaluate","hestitate","wonder","belief","want"
    }; 
    
    /** used in full internal experience mode only */
    protected void beliefReason(Sentence belief, Task task, NAL nal) {

        Term taskTerm = task.getTerm();
        Term beliefTerm = belief.getTerm();
        Memory memory = nal.memory;
        Random r = memory.random;

        if (r.nextFloat() < INTERNAL_EXPERIENCE_RARE_PROBABILITY ) {

            //the operators which dont have a innate belief
            //also get a chance to reveal its effects to the system this way
            Atom op = memory.the(nonInnateBeliefOperators[r.nextInt(nonInnateBeliefOperators.length)]);
            if(op!=null) {
                beliefReasonDerive(task,
                        Inheritance.make(Product.only(belief.term), op),
                nal);
            }
        }

        if (beliefTerm instanceof Implication && memory.random.nextFloat()<=INTERNAL_EXPERIENCE_PROBABILITY) {
            Implication imp=(Implication) beliefTerm;
            if(imp.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                //1. check if its (&/,term,+i1,...,+in) =/> anticipateTerm form:
                boolean valid=true;
                if(imp.getSubject() instanceof Conjunction) {
                    Conjunction conj=(Conjunction) imp.getSubject();
                    if(!conj.term[0].equals(taskTerm)) {
                        valid=false; //the expected needed term is not included
                    }
                    for(int i=1;i<conj.term.length;i++) {
                        if(!(conj.term[i] instanceof AbstractInterval)) {
                            valid=false;
                            break;
                        }
                    }
                } else {
                    if(!imp.getSubject().equals(taskTerm)) {
                        valid=false;
                    }
                }

                if(valid) {
                    beliefReasonDerive(task,
                            Operation.make(anticipate, Product.only(imp.getPredicate())),
                            nal);
                }
            }
        }
    }

    protected static Task beliefReasonDerive(Task parent, Compound new_term, NAL nal) {


        //TODO should this be a mew stamp or attached to parent.. originally it was a fresh new stamp from memory

        return nal.derive(nal.newTask(new_term).goal().truth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE)
                .budget(Global.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL,
                        Global.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL)
                .parent(parent).occurrNow()

        );

    }


    public static enum InternalExperienceMode {
        None, Minimal, Full
    }


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
