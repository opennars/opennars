package nars.inference;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import nars.core.Memory;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.BudgetFunctions.rankBelief;
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
 * Operation execution and planning support.  
 * Strengthens and accelerates goal-reaching activity
 */
public class Executive {
    
    public final Memory memory;
    
    /** previous events, for temporal induction */
    public final Deque<Task> shortTermMemory = new ArrayDeque<Task>();

    //memory for faster execution of &/ statements (experiment)
    public final Deque<Task> nextTask = new ArrayDeque<Task>();
    public final Deque<Concept> nextConcept = new ArrayDeque<Concept>();
    public final Deque<Term> nextContent = new ArrayDeque<Term>();

    
    public Executive(Memory mem) {
        this.memory = mem;        
    }

    public void reset() {
        nextTask.clear();
        nextConcept.clear();
        nextContent.clear();
        shortTermMemory.clear();        
    }   
    

    public void manageExecution()  {
        
        if (nextTask.isEmpty()) {
            return;
        }
        
        Task task=nextTask.pollFirst();
        
        Concept concept=nextConcept.pollFirst();
        
        Term content=nextContent.pollFirst();
        
        
        if(task==null) {
            //we have to wait
            return; 
        }
        
        //ok it is time for action:
        executeOperation(content, concept, task, true);
    }    

    public void executeOperation(final Term content, final Concept concept, final Task task, final boolean masterplan) {

        if (!(content instanceof Operation)) {
            return;
        }
        
        if ((!masterplan) && (!nextTask.isEmpty())) {
            return; //already executing sth
        }
        
        Operation op = (Operation) content;
        Term opi = op.getPredicate();
        if(!(opi instanceof Operator)) {
            return;
        }
        
        op.setTask(task);
        
        Operator oper = (Operator) opi;
        if (op.getSubject() instanceof Product) {
            Product args = (Product)op.getSubject();
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
        
        if ((content instanceof Conjunction) && (content.getTemporalOrder()==TemporalRules.ORDER_FORWARD)) {
            
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
                        nextTask.addLast(null);
                        nextConcept.addLast(null);
                        nextContent.addLast(null);
                    }
                }
                else if(t instanceof Operation) {
                    nextTask.addLast(task);
                    nextConcept.addLast(concept);
                    nextContent.addLast(t);
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
                            shortTermMemory.getLast().getContent(),
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

        if (stmSize!=0) { 
            //also here like in rule tables: we dont want to derive useless statements
                        
            Task stmLast = shortTermMemory.getLast();

            if(equalSubTermsInRespectToImageAndProduct(newEvent.sentence.content,stmLast.sentence.content)) {
                return false;
            }
            
            
            memory.setTheNewStamp(Stamp.make(newEvent.sentence.stamp, stmLast.sentence.stamp, memory.getTime()));
            
            memory.setCurrentTask(newEvent);
                        
            
            Sentence currentBelief = stmLast.sentence;
            
            memory.setCurrentBelief(currentBelief);

            TemporalRules.temporalInduction(newEvent.sentence, currentBelief, memory);

            ArrayList<Term> cur=new ArrayList<Term>();

            if(!(newEvent.sentence.content instanceof Operation)) {

                final int duration = memory.param.duration.get();


                
                Iterator<Task> t = shortTermMemory.descendingIterator();
                Task curT = t.next(), nextT = null;
                int i = stmSize;
                do {
                    i--;
                    
                    if (t.hasNext())
                        nextT = t.next();
                    else
                        nextT = null;
                    
                    cur.add(curT.getContent());
                    
                    if (nextT!=null) {
                        long diff = curT.getCreationTime() - nextT.getCreationTime();
                        
                        if (diff > duration) {
                            cur.add( Interval.intervalTime(diff) );
                        }
                    }
                    
                    if (t.hasNext())
                        continue; //just use last one fow now

                    memory.setCurrentBelief(curT.sentence);
                    
                    TruthValue val = curT.sentence.truth;
                    
                    /*for(int j=i+1;j+1<n;j++) { 
                        val=TruthFunctions.abduction(val,shortTermMemory.get(j+1).sentence.truth);
                    }*///lets let it abduction instead

                    long diff = newEvent.getCreationTime() - stmLast.getCreationTime();
                    
                    if (diff > duration) {
                        cur.add(0, Interval.intervalTime(diff) );
                    }

                    while (cur.size() < maxStmSize) {
                        //cur.add( Interval.intervalMagnitude(i) );
                        cur.add( Interval.intervalTime(i) );
                    }

                    //if (cur.size() > 1) {
                        Term[] terms=new Term[cur.size()];
                        for(int j=0;j<cur.size();j++) {
                            terms[cur.size()-j-1]=cur.get(j);
                        }
                        
                        Conjunction subj=(Conjunction) Conjunction.make(terms, TemporalRules.ORDER_FORWARD, memory);
                        val=TruthFunctions.abduction(val, newEvent.sentence.truth);
                        
                        Term imp=Implication.make(subj, newEvent.sentence.content, TemporalRules.ORDER_FORWARD, memory);
                        
                        BudgetValue bud=BudgetFunctions.forward(val, memory);
                        
                        memory.doublePremiseTask(imp,val,bud);
                    //}
                    
                    
                    curT = nextT;
                                        
                } while (curT!=null);
                
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
