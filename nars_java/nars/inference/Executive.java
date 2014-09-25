package nars.inference;

import java.util.ArrayDeque;
import java.util.Deque;
import nars.core.Memory;
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
import nars.language.Interval.AtomicDuration;
import nars.language.Statement;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.operator.Operation;
import nars.operator.Operator;

/**
 * Operation execution and planning support.  
 * Strengthens and accelerates goal-reaching activity
 */
public class Executive {
    
    public final GraphExecutive graph;
    
    public final Memory memory;
    
    /** previous events, for temporal induction */
    public final Deque<Task> shortTermMemory = new ArrayDeque<>();

    /** memory for faster execution of &/ statements (experiment) */
    public final Deque<TaskConceptContent> next = new ArrayDeque<>();

    
    
    public static class TaskConceptContent {
        
        public final Task task;
        public final Concept concept;
        public final Term content;

        public static TaskConceptContent NULL = new TaskConceptContent();
        
        /** null placeholder */
        protected TaskConceptContent() {
            this.task = null;
            this.concept = null;
            this.content = null;
        }

        public TaskConceptContent(Task task, Concept concept, Term content) {
            this.task = task;
            this.concept = concept;
            this.content = content;
        }
        
    }
    
    
    public Executive(Memory mem) {
        this.memory = mem;        
        this.graph = new GraphExecutive(mem,this);
    }

    public void reset() {
        next.clear();
        shortTermMemory.clear();        
    }   
    

//    public void manageExecution()  {
//        
//        if (next.isEmpty()) {
//            return;
//        }
//        
//        TaskConceptContent n = next.pollFirst();
//        
//        
//        if (n.task==null) {
//            //we have to wait
//            return; 
//        }                
//        
//        if (!(n.content instanceof Operation)) {
//            throw new RuntimeException("manageExecution: Term content is not Operation: " + n.content); 
//        }
//        
//        System.out.println("manageExecution: " + n.task);
//
//        //ok it is time for action:
//        execute((Operation)n.content, n.concept, n.task, true);
//    }    

    protected void execute(final Operation op, final Concept concept, final Task task, final boolean masterplan) {
        
        if ((!masterplan) && (!next.isEmpty())) {
            return; //already executing sth
        }
                
        Operator oper = op.getOperator();
        
        System.out.println("exe: " + task.getExplanation().trim());
        
        op.setTask(task);
                        
        oper.call(op, memory);
        
        task.setPriority(0);
        
    }
    
    /** Add plausibility estimation */
    public void decisionMaking(final Task task, final Concept concept) {
        
        //System.out.println("decision making: " + task + " c=" + concept);
                
        Term content = concept.term;
        
        
        TruthValue desireValue = concept.getDesire();
        
        if (desireValue.getExpectation() < memory.param.decisionThreshold.get()) {
            return;
        }
        
        
        //FAST EXECUTION OF OPERATOR SEQUENCE LIKE STM PROVIDES
       /* if ((content instanceof Conjunction) && (content.getTemporalOrder()==TemporalRules.ORDER_FORWARD)) {
            
            //1. get first operator and execute it
            CompoundTerm cont = (CompoundTerm) content;
            
            for (final Term t : cont.term) {
                if(!(t instanceof Operation) && !(t instanceof Interval)) {
                    return;
                }
            }
            
            final AtomicDuration duration = memory.param.duration;
            
            for (final Term t : cont.term) {
                
                if(t instanceof Interval) {
                    Interval intv=(Interval) t;
                    
                    long wait_steps = intv.getTime(duration);
                           
                    for(long i=0;i<wait_steps; i++) {
                        next.addLast(TaskConceptContent.NULL);
                    }
                }
                else if(t instanceof Operation) {
                    next.addLast(new TaskConceptContent(task, concept, t));
                }
            }
            //END FASTER EXECUTION OF ACTION SEQUENCE
            
            return;           
        }*/
        //END FAST EXECUTION OF OPERATOR SEQUENCE LIKE STM PROVIDES
        if (!(content instanceof Operation)) {
            //throw new RuntimeException("decisionMaking: Term content is not Operation: " + content);          
            return;
        }
        
        /*if(next.isEmpty())
            execute((Operation)content, concept, task, false);*/
    }
    
      /** Add plausibility estimation */
    public void decisionMaking2(final Task task) {
        
        //System.out.println("decision making: " + task + " c=" + concept);
                
        Term content = ((Statement)task.sentence.content).getSubject();
        
    
        
        
        //FAST EXECUTION OF OPERATOR SEQUENCE LIKE STM PROVIDES
        if ((content instanceof Conjunction) && (content.getTemporalOrder()==TemporalRules.ORDER_FORWARD)) {
            
            //1. get first operator and execute it
            CompoundTerm cont = (CompoundTerm) content;
            
            for (final Term t : cont.term) {
                if(!(t instanceof Operation) && !(t instanceof Interval)) {
                    return;
                }
            }
            
            final AtomicDuration duration = memory.param.duration;
            
            for (final Term t : cont.term) {
                
                if(t instanceof Interval) {
                    Interval intv=(Interval) t;
                    
                    long wait_steps = intv.getTime(duration);
                           
                    for(long i=0;i<wait_steps; i++) {
                        next.addLast(TaskConceptContent.NULL);
                    }
                }
                else if(t instanceof Operation) {
                    next.addLast(new TaskConceptContent(task, null, t));
                }
            }
            return;           
        }
        //END FAST EXECUTION OF OPERATOR SEQUENCE LIKE STM PROVIDES
        if (!(content instanceof Operation)) {
            //throw new RuntimeException("decisionMaking: Term content is not Operation: " + content);          
            return;
        }
        
        if(next.isEmpty())
            execute((Operation)content, null, task, false);
    }
    
    
    @Deprecated public boolean isActionable(final Task task, final Task newEvent) {
        
        if ((task.sentence!=null) && (task.sentence.content!=null) && (task.sentence.content instanceof Implication))
            return true;
        
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

    public boolean isActionable(final Task task) {
        
        if ((task.sentence!=null) && (task.sentence.content!=null) && (task.sentence.content instanceof Implication))
            return true;
        
        if (task.sentence.stamp.getOccurrenceTime() == Stamp.ETERNAL) {
            return false;
        }
        
        if (!task.sentence.isJudgment()) {
            return false;
        }

        return false;        
    }
    
//    public boolean planShortTerm(final Concept concept, final Task task, final Memory mem) {
//        return graph.inputTask(concept, task);
//    }
    
    public boolean planShortTerm(final Task newEvent, Memory mem) {

        if (newEvent == null)
            return false;
        
                
        boolean actionable = isActionable(newEvent,mem);
        //boolean actionable = true;
        
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

            //SHORT TERM MEMORY: REMEMBER OWN ACTION SEQUENCES
            /*if(!(newEvent.sentence.content instanceof Operation)) {

                final AtomicDuration duration = memory.param.duration;


                ArrayList<Term> cur=new ArrayList<Term>();
                
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
                        
                        if (diff >= duration.get()) {
                            cur.add( Interval.intervalTime(diff, duration) );
                        }
                    }
                    
                    if (t.hasNext()) {
                        curT = nextT;
                        continue; //just use last one
                    }
                    else {
                        //Finalize
                        
                        memory.setCurrentBelief(curT.sentence);

                        TruthValue val = curT.sentence.truth;

                        //for(int j=i+1;j+1<n;j++) { 
                        //    val=TruthFunctions.abduction(val,shortTermMemory.get(j+1).sentence.truth);
                        // ///lets let it abduction instead

                        long diff = newEvent.getCreationTime() - stmLast.getCreationTime();

                        if (diff >= duration.get()) {
                            cur.add(0, Interval.intervalTime(diff, duration) );
                        }

                        //while (cur.size() < maxStmSize) {
                        //    cur.add( Interval.intervalMagnitude(i) );
                        //    //cur.add( Interval.intervalTime(i) );
                        //

                        //if (cur.size() > 1) {
                        //term = reverse of cur
                        Term[] terms=new Term[cur.size()];
                        for(int j=0;j<cur.size();j++) {
                            terms[cur.size()-j-1]=cur.get(j);
                        }
                        int u=0, o=0;
                        for(int j=0;j<terms.length;j++) {
                            if(terms[j] instanceof Operation  || terms[j] instanceof Interval) {
                                u++;
                            }
                        }
                        
                        if(u!=0) {
                            Term[] terms_temp=new Term[u];
                            for(int j=0;j<terms.length;j++) { //only for operator/wait chains
                                if(terms[j] instanceof Operation || terms[j] instanceof Interval) {
                                    terms_temp[o]=terms[j];
                                    o++;
                                }
                            }
                            terms=terms_temp;

                            if (terms.length > 1) {

                                Conjunction subj=(Conjunction) Conjunction.make(terms, TemporalRules.ORDER_FORWARD, memory);
                                val=TruthFunctions.abduction(val, newEvent.sentence.truth);

                                Term imp=Implication.make(subj, newEvent.sentence.content, TemporalRules.ORDER_FORWARD, memory);

                                BudgetValue bud=BudgetFunctions.forward(val, memory);

                                memory.doublePremiseTask(imp,val,bud);
                            }
                        }
                    }
                    
                    curT = nextT;
                                        
                } while (curT!=null);
            }*/
            //END SHORT TERM MEMORY
        }

        //for this heuristic, only use input events & task effects of operations
        if (actionable) { 
            shortTermMemory.add(newEvent);
            
            if(shortTermMemory.size()>maxStmSize) {
                shortTermMemory.removeFirst();
            }
        
            return true;
        }
        return false;
    }
    
    //import from older revision where temporal induction was working fine:
    public boolean isActionable(final Task newEvent, Memory mem) {
        if(!((newEvent.isInput()) || (newEvent.getCause()!=null))) {
            return false;
        }
        Term newcontent=newEvent.sentence.content;
        if(newcontent instanceof Operation) {
            Term pred=((Operation)newcontent).getPredicate();
            if(pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
                return false;
            }
        }
        return true;
    }
    /*
    public boolean isActionable(final Task newEvent, Memory mem) {
        if(!((newEvent.isInput()))) {
            return false;
        }
        Term newcontent=newEvent.sentence.content;
        if(newcontent instanceof Operation) {
            Term pred=((Operation)newcontent).getPredicate();
            if(pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
                return false;
            }
        }
        return true;
    }*/
    
}
