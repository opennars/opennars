package nars.core;

import java.util.ArrayList;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import static nars.inference.BudgetFunctions.rankBelief;
import nars.inference.TemporalRules;
import nars.inference.TruthFunctions;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Product;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * Operation execution and planning support.  Strengthens and accelerates goal-reaching activity
 */
public class Executive {
    
    public final Memory memory;
    
    /** previous events, for temporal induction */
    public final ArrayList<Task> shortTermMemory=new ArrayList<Task>();

    //memory for faster execution of &/ statements (experiment)
    public final ArrayList<Task> next_task=new ArrayList<Task>();
    public final ArrayList<Concept> next_concept=new ArrayList<Concept>();
    public final ArrayList<Term> next_content=new ArrayList<Term>();

    
    public Executive(Memory mem) {
        this.memory = mem;
    }

    public void reset() {
        next_task.clear();
        next_concept.clear();
        next_content.clear();
        shortTermMemory.clear();        
    }   
    

    public void manageExecution()  {
        
        if (next_task.isEmpty()) {
            return;
        }
        
        Task task=next_task.get(0);
        next_task.remove(0);
        Concept concept=next_concept.get(0);
        next_concept.remove(0);
        Term content=next_content.get(0);
        next_content.remove(0);
        if(task==null) {
            return; //we have to wait
        }
        
        //ok it is time for action:
        executeOperation(content, concept, task, true);
    }    

    public void executeOperation(final Term content, final Concept concept, final Task task, final boolean masterplan) {

        if (!(content instanceof Operation)) {
            return;
        }
        
        if(!masterplan && next_task.isEmpty()==false) {
            return; //already executing sth
        }
        
        Operation op = (Operation) content;
        Term opi=op.getPredicate();
        if(!(opi instanceof Operator)) {
            return;
        }
        op.setTask(task);
        Operator oper = (Operator) opi;
        if((op.getSubject() instanceof Product)) {
            Product args=(Product) op.getSubject();
            oper.call(op, args.term, concept.memory);
            task.setPriority(0);
        }
        
    }
    
    /** Add plausibility estimation */
    public void decisionMaking(final Task task, final Concept concept) {
        
        Term content = concept.term;
        TruthValue desireValue = concept.getDesire();
        
        if (desireValue.getExpectation() < memory.param.decisionThreshold.get()) {
            return;
        }
        
        if(content instanceof Conjunction && content.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
            //1. get first operator and execute it
            CompoundTerm cont = (CompoundTerm) content;
            
            //only allow the long plans here
            if(cont.term.length!= memory.param.shortTermMemorySize.get()) { 
                return;
            }
            
            final int duration = memory.param.duration.get();
            
            for (final Term t : cont.term) {
                
                if(t instanceof Interval) {
                    Interval intv=(Interval) t;
                    
                    long wait_steps = intv.magnitude;
                    
                    for(long i=0;i<wait_steps * duration; i++) {
                        next_task.add(null);
                        next_concept.add(null);
                        next_content.add(null);
                    }
                }
                else if(t instanceof Operation) {
                    next_task.add(task);
                    next_concept.add(concept);
                    next_content.add(t);
                }
            }
            
            return;           
        }
        
        executeOperation(content, concept, task, false);
    }
    
    
    public boolean isActionable(final Task task, final Task newEvent) {
        
        if (task.sentence.stamp.getOccurrenceTime() == Stamp.ETERNAL) {
            return false;
        }
        
        if (!task.sentence.isJudgment()) {
            return false;
        }
            
        if ((newEvent == null)
                || (rankBelief(newEvent.sentence) < rankBelief(task.sentence))) {
            
            return 
                ((shortTermMemory.isEmpty()                     
                    ||                    
                !equalSubTermsInRespectToImageAndProduct(
                            shortTermMemory.get(shortTermMemory.size()-1).getContent(),
                            task.getContent()))
            );
        }        
        
        return false;        
    }
    
    public boolean planShortTerm(final Task newEvent) {
                
        if (newEvent == null)
            return false;
        
        boolean actionable = isActionable(newEvent);
        
        if (!actionable) {
            return false;
        }

        final int maxStmSize =  memory.param.shortTermMemorySize.get();
        int stmSize = shortTermMemory.size();

        if (stmSize!=0) { //also here like in rule tables: we dont want to derive useless statements
            if(equalSubTermsInRespectToImageAndProduct(newEvent.sentence.content,shortTermMemory.get(stmSize-1).sentence.content)) {
                return false;
            }
            
            memory.setTheNewStamp(Stamp.make(newEvent.sentence.stamp, shortTermMemory.get(stmSize-1).sentence.stamp, memory.getTime()));
            
            memory.setCurrentTask(newEvent);
                        
            Sentence currentBelief = shortTermMemory.get(stmSize-1).sentence;
            
            memory.setCurrentBelief(currentBelief);

            TemporalRules.temporalInduction(newEvent.sentence, currentBelief, memory);

            ArrayList<Term> cur=new ArrayList<Term>();

            if(!(newEvent.sentence.content instanceof Operation)) {

                final int duration = memory.param.duration.get();


                for(int i=stmSize-1; i>=0; i--) {
                    
                    cur.add(shortTermMemory.get(i).getContent());
                    
                    if(i>0) {
                        int diff=(int) (shortTermMemory.get(i).getCreationTime()-shortTermMemory.get(i-1).getCreationTime());
                        
                        if (diff > duration) {
                            cur.add( Interval.intervalTime(diff) );
                        }
                    }
                    
                    if(i!=0)
                        continue; //just use last one fow now

                    memory.setCurrentBelief(shortTermMemory.get(i).sentence);
                    
                    TruthValue val=shortTermMemory.get(i).sentence.truth;
                    
                    /*for(int j=i+1;j+1<n;j++) { 
                        val=TruthFunctions.abduction(val,shortTermMemory.get(j+1).sentence.truth);
                    }*///lets let it abduction instead

                    int diff=(int) (newEvent.getCreationTime()-shortTermMemory.get(stmSize-1).getCreationTime());
                    
                    if(diff > duration) {
                        cur.add(0, Interval.intervalTime(diff) );
                    }

                    while (cur.size() < maxStmSize) {                           
                        Interval inti = Interval.intervalMagnitude(i);
                        cur.add(inti);
                    }

                    Term[] terms=new Term[cur.size()];
                    for(int j=0;j<cur.size();j++) {
                        terms[cur.size()-j-1]=cur.get(j);
                    }

                    if(terms.length>1) {
                        Conjunction subj=(Conjunction) Conjunction.make(terms, TemporalRules.ORDER_FORWARD, memory);
                        val=TruthFunctions.abduction(val, newEvent.sentence.truth);
                        
                        Term imp=Implication.make(subj, newEvent.sentence.content, TemporalRules.ORDER_FORWARD, memory);
                        
                        BudgetValue bud=BudgetFunctions.forward(val, memory);
                        
                        memory.doublePremiseTask(imp,val,bud);
                    }
                }
            }
        }

        //for this heuristic, only use input events & task effects of operations
        if (actionable) { 
            shortTermMemory.add(newEvent);
            if(shortTermMemory.size()>maxStmSize) {
                shortTermMemory.remove(0);
            }
        
        }
        
        return true;
    }
    
    public boolean isActionable(final Task newEvent) {
        return ( (newEvent.isInput()) || (newEvent.getCause()!=null) );
    }
    
}
