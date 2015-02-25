package nars.plugin.mental;

import java.util.Arrays;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.Plugin;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.Interval;
import nars.language.Product;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * To rememberAction an internal action as an operation
 * <p>
 * called from Concept
 * @param task The task processed
 */
public class InternalExperience implements Plugin, EventObserver {
        
    public static float MINIMUM_BUDGET_SUMMARY_TO_CREATE=0.92f;
    
    //internal experience has less durability?
    public static final float INTERNAL_EXPERIENCE_PROBABILITY=0.0001f;
    
    //less probable form
    public static final float INTERNAL_EXPERIENCE_RARE_PROBABILITY = 
            INTERNAL_EXPERIENCE_PROBABILITY/4f;
    
    //internal experience has less durability?
    public static float INTERNAL_EXPERIENCE_DURABILITY_MUL=0.1f;
    //internal experience has less priority?
    public static float INTERNAL_EXPERIENCE_PRIORITY_MUL=0.1f;
    
    //dont use internal experience for want and believe if this setting is true
    public static boolean AllowWantBelieve=true;
    
    public boolean isAllowWantBelieve() {
        return AllowWantBelieve;
    }
    public void setAllowWantBelieve(boolean val) {
        AllowWantBelieve=val;
    }

    
    public double getMinimumCreationBudgetSummary() {
        return MINIMUM_BUDGET_SUMMARY_TO_CREATE;
    }
    public void setMinimumCreationBudgetSummary(double val) {
        MINIMUM_BUDGET_SUMMARY_TO_CREATE=(float) val;
    }
    
    private Memory memory;


    /** whether it is full internal experience, or minimal (false) */
    public boolean isFull() {
        return false;
    }
    
    @Override public boolean setEnabled(NAR n, boolean enabled) {        
        memory = n.memory;
        
        memory.event.set(this, enabled, Events.ConceptDirectProcessedTask.class);
        
        if (isFull())
            memory.event.set(this, enabled, Events.BeliefReason.class);
                
        return true;
    }
    
        public static Term toTerm(final Sentence s, final Memory mem) {
        String opName;
        switch (s.punctuation) {
            case Symbols.JUDGMENT_MARK:
                opName = "^believe";
                if(!AllowWantBelieve) {
                    return null;
                }
                break;
            case Symbols.GOAL_MARK:
                opName = "^want";
                if(!AllowWantBelieve) {
                    return null;
                }
                break;
            case Symbols.QUESTION_MARK:
                opName = "^wonder";
                break;
            case Symbols.QUEST_MARK:
                opName = "^evaluate";
                break;
            default:
                return null;
        }
        
        Term opTerm = mem.getOperator(opName);
        Term[] arg = new Term[ s.truth==null ? 1 : 2 ];
        arg[0]=s.getTerm();
        if (s.truth != null) {
            arg[1] = s.truth.toWordTerm();            
        }
        
        //Operation.make ?
        Term operation = Inheritance.make(new Product(arg), opTerm);
        if (operation == null) {
            throw new RuntimeException("Unable to create Inheritance: " + opTerm + ", " + Arrays.toString(arg));
        }
        return operation;
    }


    @Override
    public void event(Class event, Object[] a) {
        
        if (event==Events.ConceptDirectProcessedTask.class) {


            Task task = (Task)a[0];                

            if(task.budget.summary()<MINIMUM_BUDGET_SUMMARY_TO_CREATE) {
                return;
            }
            Term content = task.getTerm();

            // to prevent infinite recursions
            if (content instanceof Operation/* ||  Memory.randomNumber.nextDouble()>Parameters.INTERNAL_EXPERIENCE_PROBABILITY*/)
                return;

            Sentence sentence = task.sentence;
            TruthValue truth = new TruthValue(1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);

            Stamp stamp = task.sentence.stamp.clone();
            stamp.setOccurrenceTime(memory.time());

            Term ret=toTerm(sentence, memory);
            if(ret==null) {
                return;
            }
            Sentence j = new Sentence(ret, Symbols.JUDGMENT_MARK, truth, stamp);
            BudgetValue newbudget=new BudgetValue(
                    Parameters.DEFAULT_JUDGMENT_CONFIDENCE*INTERNAL_EXPERIENCE_PRIORITY_MUL,
                    Parameters.DEFAULT_JUDGMENT_PRIORITY*INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                    BudgetFunctions.truthToQuality(truth));

            Task newTask = new Task(j, (BudgetValue) newbudget, 
                    isFull() ? null : task);

            memory.addNewTask(newTask, "Remembered Action (Internal Experience)");
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

    final static String[] nonInnateBeliefOperators = new String[] {
        "^remind","^doubt","^consider","^evaluate","hestitate","^wonder","^belief","^want"
    }; 
    
    /** used in full internal experience mode only */
    protected void beliefReason(Sentence belief, Term beliefTerm, Term taskTerm, NAL nal) {
        
        Memory memory = nal.memory;
    
        if (Memory.randomNumber.nextDouble() < INTERNAL_EXPERIENCE_RARE_PROBABILITY ) {
            
            //the operators which dont have a innate belief
            //also get a chance to reveal its effects to the system this way
            Operator op=memory.getOperator(nonInnateBeliefOperators[Memory.randomNumber.nextInt(nonInnateBeliefOperators.length)]);
            
            Product prod=new Product(new Term[]{belief.term});
            
            if(op!=null && prod!=null) {
                
                Term new_term=Inheritance.make(prod, op);
                Sentence sentence = new Sentence(
                    new_term, Symbols.GOAL_MARK, 
                    new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                    new Stamp(memory));
                
                float quality = BudgetFunctions.truthToQuality(sentence.truth);
                BudgetValue budget = new BudgetValue(
                    Parameters.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL, 
                    Parameters.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                    quality);

                Task newTask = new Task(sentence, budget);       
                nal.derivedTask(newTask, false, false, null, null, false);
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
                    Operator op=memory.getOperator("^anticipate");
                    if (op == null)
                        throw new RuntimeException(this + " requires ^anticipate operator");
                    
                    Product args=new Product(new Term[]{imp.getPredicate()});
                    Term new_term=Operation.make(args,op);

                    Sentence sentence = new Sentence(
                        new_term, Symbols.GOAL_MARK, 
                        new TruthValue(1, Parameters.DEFAULT_JUDGMENT_CONFIDENCE),  // a naming convension
                        new Stamp(memory));

                    float quality = BudgetFunctions.truthToQuality(sentence.truth);
                    BudgetValue budget = new BudgetValue(
                        Parameters.DEFAULT_GOAL_PRIORITY*INTERNAL_EXPERIENCE_PRIORITY_MUL, 
                        Parameters.DEFAULT_GOAL_DURABILITY*INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                        quality);

                    Task newTask = new Task(sentence, budget);       
                    nal.derivedTask(newTask, false, false, null, null, false);
                }
            }
        }
    }    
}
