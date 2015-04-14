package nars.operate.mental;

import nars.*;
import nars.budget.Budget;
import nars.io.Symbols;
import nars.nal.*;
import nars.nal.stamp.Stamp;
import nars.nal.nal1.Inheritance;
import nars.nal.nal4.Product;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Interval;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;
import nars.operate.AbstractOperator;

import java.util.Arrays;

/**
 *
 * https://www.youtube.com/watch?v=ia4wMU-vfrw
 *
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 */
public class InternalExperience extends AbstractOperator {
        
    public static final float MINIMUM_BUDGET_SUMMARY_TO_CREATE=0.92f;
    
    //internal experience has less durability?
    public static final float INTERNAL_EXPERIENCE_PROBABILITY=0.01f;
    
    //less probable form
    public static final float INTERNAL_EXPERIENCE_RARE_PROBABILITY = 
            INTERNAL_EXPERIENCE_PROBABILITY/4f;




    //internal experience has less durability?
    public static final float INTERNAL_EXPERIENCE_DURABILITY_MUL=0.5f;
    //internal experience has less priority?
    public static final float INTERNAL_EXPERIENCE_PRIORITY_MUL=0.5f;
    
    //dont use internal experience for want and believe if this setting is true
    public static final boolean AllowWantBelieve=true;

    /*public boolean isAllowWantBelieve() {
        return AllowWantBelieve;
    }
    public void setAllowWantBelieve(boolean val) {
        AllowWantBelieve=val;
    }*/

    private Memory memory;
    private Operator believe;
    private Operator want;
    private Operator wonder;
    private Operator evaluate;


    /** whether it is full internal experience, or minimal (false) */
    public boolean isFull() {
        return false;
    }


    @Override
    public Class[] getEvents() {
        if (isFull()) {
            return new Class[] { Events.TaskImmediateProcessed.class, Events.BeliefReason.class };
        }
        else {
            return new Class[] { Events.TaskImmediateProcessed.class };
        }
    }

    @Override
    public void onEnabled(NAR n) {

        this.memory = n.memory;
        this.believe = memory.operator("^believe");
        this.want = memory.operator("^want");
        this.wonder = memory.operator("^wonder");
        this.evaluate = memory.operator("^evaluate");
    }

    @Override
    public void onDisabled(NAR n) {

    }

    public Term toTerm(final Sentence s, final Memory mem) {
        Operator opTerm;
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
        
        Term[] arg = new Term[ s.truth==null ? 1 : 2 ];
        arg[0]=s.getTerm();
        if (s.truth != null) {
            arg[1] = s.truth.toWordTerm();            
        }
        
        Operation operation = Operation.make(opTerm, arg);
        if (operation == null) {
            throw new RuntimeException("Unable to create Inheritance: " + opTerm + ", " + Arrays.toString(arg));
        }
        return operation;
    }


    @Override
    public void event(Class event, Object[] a) {


        if (event==Events.TaskImmediateProcessed.class) {

            Task task = (Task)a[0];
            NAL nal = (NAL)a[1];

            if (Memory.randomNumber.nextDouble()>INTERNAL_EXPERIENCE_PROBABILITY) return;

            if(task.budget.summary() < MINIMUM_BUDGET_SUMMARY_TO_CREATE) {
                return;
            }

            Term content = task.getTerm();

            if (content instanceof Operation) return;   // to prevent infinite recursions


            Sentence sentence = task.sentence;

            float conf = Global.DEFAULT_JUDGMENT_CONFIDENCE;


            Budget newbudget = Budget.budgetIfAboveThreshold(
                    task.getPriority() * INTERNAL_EXPERIENCE_PRIORITY_MUL, //Parameters.DEFAULT_JUDGMENT_CONFIDENCE*INTERNAL_EXPERIENCE_PRIORITY_MUL
                    task.getDurability() * INTERNAL_EXPERIENCE_DURABILITY_MUL, //Parameters.DEFAULT_JUDGMENT_PRIORITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
                    task.getQuality());
            if (newbudget == null) return;


            Term ret = toTerm(sentence, memory);
            if(ret==null) return;





            if (newbudget.aboveThreshold()) {

                NAL.StampBuilder stamp = nal.newStamp(task.sentence, memory.time());

                Sentence j = new Sentence(ret, Symbols.JUDGMENT, new TruthValue(1.0f, conf), stamp);

                Task newTask = new Task(j, newbudget, /*isFull() ? null : */task);

                memory.taskAdd(newTask, "Internally remembered experienced");
            }

        }
        else if (event == Events.BeliefReason.class) {
            //belief, beliefTerm, taskTerm, nal
            Sentence belief = (Sentence)a[0];
            Term taskTerm = (Term)a[1];
            NAL nal = (NAL)a[2];
            beliefReason(belief, belief.getTerm(), taskTerm, nal);
        }
    }

    final static String[] nonInnateBeliefOperators = new String[] {
        "^remind","^doubt","^consider","^evaluate","hestitate","^wonder","^belief","^want"
    }; 
    
    /** used in full internal experience mode only */
    protected void beliefReason(Sentence belief, Term beliefTerm, Term taskTerm, NAL nal) {
        
        Memory memory = nal.memory;
    
        if (Memory.randomNumber.nextDouble() < INTERNAL_EXPERIENCE_RARE_PROBABILITY ) {
            
            //the operators which dont have a innate belief
            //also get a chance to reveal its effects to the system this way
            Operator op=memory.operator(nonInnateBeliefOperators[Memory.randomNumber.nextInt(nonInnateBeliefOperators.length)]);
            
            Product prod=new Product(belief.term);
            
            if(op!=null) {
                
                Term new_term=Inheritance.make(prod, op);
                Sentence sentence = new Sentence(
                    new_term, Symbols.GOAL,
                    new TruthValue(1, Global.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                    new Stamp(memory, Tense.Present));
                
                float quality = BudgetFunctions.truthToQuality(sentence.truth);
                Budget budget = new Budget(
                    Global.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL,
                    Global.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
                    quality);

                Task newTask = new Task(sentence, budget);       
                nal.deriveTask(newTask, false, false, null, false);
            }
        }

        if (beliefTerm instanceof Implication && Memory.randomNumber.nextDouble()<=INTERNAL_EXPERIENCE_PROBABILITY) {
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
                    Operator op=memory.operator("^anticipate");
                    if (op == null)
                        throw new RuntimeException(this + " requires ^anticipate operate");
                    
                    Product args=new Product(imp.getPredicate());
                    Term new_term=Operation.make(args,op);

                    Sentence sentence = new Sentence(
                        new_term, Symbols.GOAL,
                        new TruthValue(1, Global.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                        new Stamp(memory, Tense.Present));

                    float quality = BudgetFunctions.truthToQuality(sentence.truth);
                    Budget budget = new Budget(
                        Global.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL,
                        Global.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL,
                        quality);

                    Task newTask = new Task(sentence, budget);       
                    nal.deriveTask(newTask, false, false, null, false);
                }
            }
        }
    }



    public static enum InternalExperienceMode {
        None, Minimal, Full
    }
}
