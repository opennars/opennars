package nars.op.mental;

import nars.*;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.event.NARReaction;
import nars.nal.*;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Interval;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.stamp.Stamp;
import nars.nal.term.Atom;
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
        
    public static final float MINIMUM_BUDGET_SUMMARY_TO_CREATE=0.5f;
    
    public static float MINIMUM_BUDGET_SUMMARY_TO_CREATE=0.92f; //0.92
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
    public static final boolean AllowWantBelieve=true; //wut, semantic issue ^^
    
    //
    public static boolean OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY=false; //https://groups.google.com/forum/#!topic/open-nars/DVE5FJd7FaM
    
    public boolean isAllowNewStrategy() {
        return !OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY;
    }
    public void setAllowNewStrategy(boolean val) {
        OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY=!val;
    }

    public boolean isAllowWantBelieve() {
        return AllowWantBelieve;
    }
    public void setAllowWantBelieve(boolean val) {
        AllowWantBelieve=val;
    }
    
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
    
    public static boolean enabled=true;

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


    public Term toTerm(final Sentence s, final Memory mem) {
        Atom opTerm;
        switch (s.punctuation) {
            case Symbols.JUDGMENT:
                if(!AllowWantBelieve)
                    return null;
                opTerm = believe;
                break;
            case Symbols.GOAL:
                if(!AllowWantBelieve)
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
            arg[k++] = s.truth.toWordTerm();
        }
        arg[k] = memory.self();
        
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
            
            //old strategy always, new strategy only for QUESTION and QUEST:
            if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY || (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation == Symbols.QUESTION_MARK || task.sentence.punctuation == Symbols.QUEST_MARK))) {
                InternalExperienceFromTaskInternal(memory,task,isFull());
            }
        }
        else if (event == Events.BeliefReason.class) {
            //belief, beliefTerm, taskTerm, nal
            Sentence belief = (Sentence)a[0];
            Term beliefTerm = (Term)a[1];
            Term taskTerm = (Term)a[2];
            NAL nal = (NAL)a[3];
            beliefReason(belief, beliefTerm, taskTerm, nal);
        }
    }

     public static void InternalExperienceFromBelief(Memory memory, Task task, Sentence belief) {
        Task T=new Task(belief.clone(),task.budget.clone(),null);
        InternalExperienceFromTask(memory,T,false);
    }
    
    public static void InternalExperienceFromTask(Memory memory, Task task, boolean full) {
        if(!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
            InternalExperienceFromTaskInternal(memory,task,full);
        }
    }

    public static boolean InternalExperienceFromTaskInternal(Memory memory, Task task, boolean full) {
        if(!enabled) {
            return false;
        }
        
       // if(OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY ||
       //         (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation==Symbols.QUESTION_MARK || task.sentence.punctuation==Symbols.QUEST_MARK))) {
        {
            if(task.sentence.punctuation == Symbols.QUESTION_MARK || task.sentence.punctuation == Symbols.QUEST_MARK) {
                if(task.budget.summary()<MINIMUM_BUDGET_SUMMARY_TO_CREATE_WONDER_EVALUATE) {
                    return false;
                }
            }
            else
            if(task.budget.summary()<MINIMUM_BUDGET_SUMMARY_TO_CREATE) {
                return false;
            }
        }
        
        Term content=task.getTerm();
        // to prevent infinite recursions
        if (content instanceof Operation/* ||  Memory.randomNumber.nextDouble()>Global.INTERNAL_EXPERIENCE_PROBABILITY*/) {
            return true;
        }
        Sentence sentence = task.sentence;
        TruthValue truth = new TruthValue(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
        Stamp stamp = task.sentence.stamp.clone();
        stamp.setOccurrenceTime(memory.time());
        Term ret=toTerm(sentence, memory);
        if (ret==null) {
            return true;
        }
        Sentence j = new Sentence(ret, Symbols.JUDGMENT_MARK, truth, stamp);
        BudgetValue newbudget=new BudgetValue(
                Global.DEFAULT_JUDGMENT_CONFIDENCE*INTERNAL_EXPERIENCE_PRIORITY_MUL,
                Global.DEFAULT_JUDGMENT_PRIORITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
                BudgetFunctions.truthToQuality(truth));
        
        if(!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
            newbudget.setPriority(task.getPriority()*INTERNAL_EXPERIENCE_PRIORITY_MUL);
            newbudget.setDurability(task.getDurability()*INTERNAL_EXPERIENCE_DURABILITY_MUL);
        }
        
        Task newTask = new Task(j, (BudgetValue) newbudget,
                full ? null : task);
        memory.addNewTask(newTask, "Remembered Action (Internal Experience)");
        return false;
    }


    final static String[] nonInnateBeliefOperators = new String[] {
        "remind","doubt","consider","evaluate","hestitate","wonder","belief","want"
    }; 
    
    /** used in full internal experience mode only */
    protected void beliefReason(Sentence belief, Term beliefTerm, Term taskTerm, NAL nal) {
        
        Memory memory = nal.memory;
        Random r = memory.random;
    
        if (r.nextFloat() < INTERNAL_EXPERIENCE_RARE_PROBABILITY ) {
            
            //the operators which dont have a innate belief
            //also get a chance to reveal its effects to the system this way
            Atom op = memory.the(nonInnateBeliefOperators[r.nextInt(nonInnateBeliefOperators.length)]);
            if(op!=null) {
                Product prod=Product.make(belief.term);

                Term new_term=Inheritance.make(prod, op);
                Sentence sentence = new Sentence(
                    new_term, Symbols.GOAL,
                    new DefaultTruth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                    new Stamp(memory, Tense.Present));
                
                float quality = BudgetFunctions.truthToQuality(sentence.truth);
                Budget budget = new Budget(
                    Global.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL,
                    Global.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
                    quality);

                Task newTask = new Task(sentence, budget);       
                nal.deriveTask(newTask, false, false, null, null, false);
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
                        if(!(conj.term[i] instanceof Interval)) {
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


                    Product args = Product.make(imp.getPredicate());
                    Term new_term=Operation.make(anticipate, args);

                    Sentence sentence = new Sentence(
                        new_term, Symbols.GOAL,
                        new DefaultTruth(1, Global.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                        new Stamp(memory, Tense.Present));

                    float quality = BudgetFunctions.truthToQuality(sentence.truth);
                    Budget budget = new Budget(
                        Global.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL,
                        Global.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
                        quality);

                    Task newTask = new Task(sentence, budget);       
                    nal.deriveTask(newTask, false, false, null, null, false);
                }
            }
        }
    }



    public static enum InternalExperienceMode {
        None, Minimal, Full
    }


    //TODO
    public static void InternalExperienceFromBelief(Memory memory, Task task, Sentence belief) {
        Task T=new Task(belief.clone(),new Budget(task),null);
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
//        //         (!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY && (task.sentence.punctuation==Symbols.QUESTION_MARK || task.sentence.punctuation==Symbols.QUEST_MARK))) {
//        {
//            if(task.sentence.punctuation == Symbols.QUESTION_MARK || task.sentence.punctuation == Symbols.QUEST_MARK) {
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
//        TruthValue truth = new TruthValue(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE);
//        Stamp stamp = task.sentence.stamp.clone();
//        stamp.setOccurrenceTime(memory.time());
//        Term ret=toTerm(sentence, memory);
//        if (ret==null) {
//            return true;
//        }
//        Sentence j = new Sentence(ret, Symbols.JUDGMENT_MARK, truth, stamp);
//        BudgetValue newbudget=new BudgetValue(
//                Global.DEFAULT_JUDGMENT_CONFIDENCE*INTERNAL_EXPERIENCE_PRIORITY_MUL,
//                Global.DEFAULT_JUDGMENT_PRIORITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
//                BudgetFunctions.truthToQuality(truth));
//
//        if(!OLD_BELIEVE_WANT_EVALUATE_WONDER_STRATEGY) {
//            newbudget.setPriority(task.getPriority()*INTERNAL_EXPERIENCE_PRIORITY_MUL);
//            newbudget.setDurability(task.getDurability()*INTERNAL_EXPERIENCE_DURABILITY_MUL);
//        }
//
//        Task newTask = new Task(j, (BudgetValue) newbudget,
//                full ? null : task);
//        memory.addNewTask(newTask, "Remembered Action (Internal Experience)");
//        return false;
//    }
}
