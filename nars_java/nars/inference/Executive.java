package nars.inference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.inference.GraphExecutive.ParticlePlan;
import nars.io.Symbols;
import nars.io.Texts;
import nars.io.buffer.PriorityBuffer;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
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

    ///** memory for faster execution of &/ statements (experiment) */
    //public final Deque<TaskConceptContent> next = new ArrayDeque<>();

    PriorityBuffer<TaskExecution> tasks;
    private Set<TaskExecution> tasksToRemove = new HashSet();
    
    /** number of tasks that are active in the sorted priority buffer for execution */
    int numActiveTasks = 1;

    /** max number of tasks that a plan can generate. chooses the N most confident */
    int maxPlannedTasks = 4;
    
    float searchDepth = 16;
    int particles = 64;
    
    long lastExecution = -1;
    

    
    public Executive(Memory mem) {
        this.memory = mem;        
        this.graph = new GraphExecutive(mem,this);

        this.tasks = new PriorityBuffer<TaskExecution>(new Comparator<TaskExecution>() {
            @Override
            public final int compare(final TaskExecution a, final TaskExecution b) {
                float ap = a.getDesire();
                float bp = b.getDesire();
                if (bp != ap) {
                    return Float.compare(ap, bp);
                } else {
                    float ad = a.getPriority();
                    float bd = b.getPriority();
                    if (ad!=bd)
                        return Float.compare(ad, bd);
                    else {
                        float add = a.getDurability();
                        float bdd = b.getDurability();
                        return Float.compare(add, bdd);                        
                    }
                }

            }
        }, numActiveTasks) {

            @Override protected void reject(final TaskExecution t) {
                removeTask(t);
            }
            
        };
        
    }
    
    public class TaskExecution {
        /** may be null for input tasks */
        public final Concept c;
        
        public final Task t;
        public int sequence;
        public long delayUntil = -1;
        private float motivationFactor = 1;
        
        public TaskExecution(final Concept concept, Task t) {
            this.c = concept;
            
            
            //Check if task is 
            Term term = t.getContent();
            if (term instanceof Implication) {
                Implication it = (Implication)term;
                if ((it.getTemporalOrder() == TemporalRules.ORDER_FORWARD) || (it.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {
                    if (it.getSubject() instanceof Conjunction) {
                        t = inlineConjunction(t, (Conjunction)it.getSubject());
                    }
                }
            }
            else if (term instanceof Conjunction) {
                t = inlineConjunction(t, (Conjunction)term);
            }
            
            this.t = t;

        }

        protected Task inlineConjunction(Task t, final Conjunction c) {
            List<Term> inlined = new ArrayList();
            boolean wasInlined = false;
            if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                Term prev = null;
                for (Term e : c.term) {
                    if (!isPlanTerm(e)) {
                        if (graph.isPlannable(e)) {
                            
                            TreeSet<ParticlePlan> plans = graph.particlePlan(e, searchDepth, particles);
                            if (plans.size() > 0) {
                                //use the first
                                ParticlePlan pp = plans.first();
                                
                                //if terms precede this one, remove a common prefix
                                //scan from the end of the sequence backward until a term matches the previous, and splice it there
                                //TODO more rigorous prefix compraison. compare sublist prefix
                                List<Term> seq = pp.sequence;
                                
                                if (prev!=null) {
                                    int previousTermIndex = pp.sequence.lastIndexOf(prev);
                                    
                                    if (previousTermIndex!=-1) {
                                        if (previousTermIndex == seq.size()-1)
                                            seq = Collections.EMPTY_LIST;
                                        else {                                            
                                            seq = seq.subList(previousTermIndex+1, seq.size());
                                        }
                                    }
                                }
                                
                                //System.out.println("inline: " + pp.sequence + " -> " + seq);
                                
                                
                                inlined.addAll(seq);
                                //System.err.println("Inline " + e + " in " + t.getContent() + " = " + pp.sequence);  
                                wasInlined = true;
                            }
                            else {
                                //System.err.println("Inline: no planavailable");
                                //no plan available, this wont be able to execute
                                setMotivationFactor(0);
                            }
                        }
                        else {
                            //this won't be able to execute here
                            setMotivationFactor(0);
                        }
                    }
                    else {
                        //executable term, add
                        inlined.add(e);
                    }
                    prev = e;
                }                            
            }            
            
            if (wasInlined) {
                Conjunction nc = c.cloneReplacingTerms(inlined.toArray(new Term[inlined.size()]));
                t = t.clone(t.sentence.clone(nc) );
                //System.err.println("  replaced task: " + t);
            }
            return t;
        }
        
        @Override public boolean equals(final Object obj) {
            if (obj instanceof TaskExecution) {
                return ((TaskExecution)obj).t.equals(t);
            }
            return false;
        }
        
        public final float getDesire() { 
            return t.getDesire().getExpectation() * motivationFactor;
        }
        public final float getPriority() { return t.getPriority();         }
        public final float getDurability() { return t.getDurability(); }
        //public final float getMotivation() { return getDesire() * getPriority() * motivationFactor;         }
        public final void setMotivationFactor(final float f) { this.motivationFactor = f;  }

        @Override public int hashCode() {            return t.hashCode();         }

        @Override
        public String toString() {
            return "!" + Texts.n2(getDesire()) + "." + sequence + "! " + t.toString();
        }


        

        
    }

    protected TaskExecution getExecution(final Task parent) {        
        for (final TaskExecution t : tasks) {
            if (t.t.parentTask!=null)
                if (t.t.parentTask.equals(parent))
                    return t;            
        }
        return null;
    }
    
    
    public boolean addTask(final Concept c, final Task t) {        
        TaskExecution existingExecutable = getExecution(t.parentTask);
        boolean valid = true;
        if (existingExecutable!=null) {

            //TODO compare motivation (desire * priority) instead?

            //if the new task for the existin goal has a lower priority, ignore it
            if (existingExecutable.getDesire() > t.getDesire().getExpectation()) {
                //System.out.println("ignored lower priority task: " + t + " for parent " + t.parentTask);              
                valid = false;
            }

            //do not allow interrupting a lower priority, but already executing task
            //TODO allow interruption if priority difference is above some threshold                
            if (existingExecutable.sequence > 0) {
                //System.out.println("ignored late task: " + t + " for parent " + t.parentTask);
                valid = false;
            }
            
        }
            
        if (valid) {
            if (tasks.add(new TaskExecution(c, t))) {
                //added successfully
                if (memory.getRecorder().isActive())
                   memory.getRecorder().append("Executive", "Task Add: " + t.toString());
                return true;
            }
        }

        //t.end();
        return false;
    }
    
    protected void removeTask(final TaskExecution t) {
        if (tasksToRemove.add(t)) {
            //t.t.setPriority(0); //dint set priority of entire statement to 0
            //t.t.end();
            if (memory.getRecorder().isActive())
               memory.getRecorder().append("Executive", "Task Remove: " + t.toString());
        }
    }
    
    protected void updateTasks() {
        List<TaskExecution> t = new ArrayList(tasks);
        t.removeAll(tasksToRemove);
        tasks.clear();
        for (TaskExecution x : t) {
            if (x.getDesire() > 0) { // && (x.getPriority() > 0)) {
                tasks.add(x);
                if ((x.delayUntil!=-1) && (x.delayUntil <= memory.getTime())) {
                    //restore motivation so task can resume processing
                    x.motivationFactor = 1.0f;
                }
            }            
        }
        tasksToRemove.clear();
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

    protected void execute(final Operation op, final Task task) {
    
        
        Operator oper = op.getOperator();
        
        //if (NAR.DEBUG)
            //System.out.println("exe: " + task.getExplanation().trim());
        
        op.setTask(task);
                        
        oper.call(op, memory);        
        
    }   
        
    public void decisionPlanning(final NAL nal, final Task t, final Concept concept) {
        
        if (Parameters.TEMPORAL_PARTICLE_PLANNER) {
            
            if (!isUrgent(concept)) return;
        
            boolean plannable = graph.isPlannable(t.getContent());
            if (plannable) {                    
                if (memory.getRecorder().isActive())
                   memory.getRecorder().append("Goal Planned", t.toString());

                graph.plan(nal, concept, t, t.getContent(), 
                        particles, searchDepth, '!', maxPlannedTasks);
            }                
        }
        
    }
    
    /** Entry point for all potentially executable tasks */
    public void decisionMaking(final Task t, final Concept concept) {
                        
        if (isUrgent(concept)) {

            Term content = concept.term;

         

            if (content instanceof Operation) {
                addTask(concept, t);
            }
            else if (isSequenceConjunction(content)) {
                addTask(concept, t);
            }
        }
        else {
            //t.end();
        }
    }
    
    public boolean isUrgent(Concept c) {               
        return (c.getDesire().getExpectation() >= memory.param.decisionThreshold.get());        
    }
    
    public void cycle() {
        long now = memory.getTime();
        
        //only execute something no less than every duration time
        if (now - lastExecution < memory.param.duration.get())
            return;
                        
        lastExecution = now;
        
        updateTasks();

        if (tasks.size() == 0)
            return;
        
        /*if (NAR.DEBUG)*/ {
            //TODO make a print function
            
            if (tasks.size() > 1)  {
                System.out.println("Tasks @ " + memory.getTime());
                for (TaskExecution tcc : tasks)
                    System.out.println("  " + tcc.toString());
            }
            else {
                System.out.println("Task @ " + memory.getTime() + ": " + tasks.get(0));
            }
            
        }
        
        
        TaskExecution topExecution = tasks.getFirst();
        Task top = topExecution.t;
        Term term = top.getContent();
        if (term instanceof Operation) {            
            execute((Operation)term, top); //directly execute            
            removeTask(topExecution);
            return;
        }
        else if (term instanceof Conjunction) {
            Conjunction c = (Conjunction)term;
            if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                executeConjunctionSequence(topExecution, c);
                return;
            }
            
        }
        else if (term instanceof Implication) {
            Implication it = (Implication)term;
            if ((it.getTemporalOrder() == TemporalRules.ORDER_FORWARD) || (it.getTemporalOrder() == TemporalRules.ORDER_CONCURRENT)) {
                if (it.getSubject() instanceof Conjunction) {
                    Conjunction c = (Conjunction)it.getSubject();
                    if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                        executeConjunctionSequence(topExecution, c);
                        return;
                    }
                }
                else if (it.getSubject() instanceof Operation) {
                    execute((Operation)it.getSubject(), top); //directly execute
                    removeTask(topExecution);
                    return;
                }
            }
            throw new RuntimeException("Unrecognized executable term: " + it.getSubject() + "[" + it.getSubject().getClass() + "] from " + top);
        }
        else {
            throw new RuntimeException("Unknown Task type: "+ top);
        }
        
        
//        //Example prediction
//        if (memory.getCurrentBelief()!=null) {
//            Term currentTerm = memory.getCurrentBelief().content;
//            if (implication.containsVertex(currentTerm)) {
//                particlePredict(currentTerm, 12, particles);
//            }                
//        }
    }

   
    public static boolean isPlanTerm(final Term t) {
        return ((t instanceof Interval) || (t instanceof Operation));
    }
    
    public static boolean isExecutableTerm(final Term t) {
        return (t instanceof Operation) || isSequenceConjunction(t);
 //task.sentence.content instanceof Operation || (task.sentence.content instanceof Conjunction && task.sentence.content.getTemporalOrder()==TemporalRules.ORDER_FORWARD)))
    }
    
    public static boolean isSequenceConjunction(final Term c) {
        if (c instanceof Conjunction) {
            Conjunction cc = ((Conjunction)c);
            return ( cc.operator() == Symbols.NativeOperator.SEQUENCE  );
            //{
                //return (cc.getTemporalOrder()==TemporalRules.ORDER_FORWARD) || (cc.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT);
            //}
        }
        return false;
    }
    

    
    private void executeConjunctionSequence(final TaskExecution task, final Conjunction c) {
        int s = task.sequence;
        Term currentTerm = c.term[s];
        
        long now = memory.getTime();
        
        if (task.delayUntil > now) {
            //not ready to execute next term
            return;
        }
        
        if (currentTerm instanceof Operation) {
            Concept conc=memory.concept(currentTerm);            
            execute((Operation)currentTerm, task.t);
            task.delayUntil = now + memory.param.duration.get();
            s++;
        }
        else if (currentTerm instanceof Interval) {
            Interval ui = (Interval)currentTerm;
            task.delayUntil = memory.getTime() + Interval.magnitudeToTime(ui.magnitude, memory.param.duration);
            s++;
        }        
        else {            
            System.err.println("Non-executable term in sequence: " + currentTerm + " in " + c + " from task " + task.t);
            removeTask(task);
        }

        if (s == c.term.length) {
            //end of task
            //System.out.println("  Completed " + task);
            removeTask(task);
        }
        else {            
            task.sequence = s;//update new value for next cycle
        }
    }
    
    public Task stmLast=null;
    public boolean inductionOnSucceedingEvents(final Task newEvent, NAL nal) {

        if (newEvent == null || newEvent.sentence.stamp.getOccurrenceTime()==Stamp.ETERNAL || !isInputOrTriggeredOperation(newEvent,nal.mem))
            return false;

        if (stmLast!=null) { 

            if(equalSubTermsInRespectToImageAndProduct(newEvent.sentence.content,stmLast.sentence.content)) {
                return false;
            }
            
            nal.setTheNewStamp(Stamp.make(newEvent.sentence.stamp, stmLast.sentence.stamp, memory.getTime()));
            nal.setCurrentTask(newEvent);
                        
            Sentence currentBelief = stmLast.sentence;
            nal.setCurrentBelief(currentBelief);

            TemporalRules.temporalInduction(newEvent.sentence, currentBelief, nal);
        }

        //for this heuristic, only use input events & task effects of operations
        stmLast=newEvent;

        return true;
    }
    
    //is input or by the system triggered operation
    public boolean isInputOrTriggeredOperation(final Task newEvent, Memory mem) {
        if(!((newEvent.isInput()) || (newEvent.getCause()!=null))) {
            return false;
        }
        /*Term newcontent=newEvent.sentence.content;
        if(newcontent instanceof Operation) {
            Term pred=((Operation)newcontent).getPredicate();
            if(pred.equals(mem.getOperator("^want")) || pred.equals(mem.getOperator("^believe"))) {
                return false;
            }
        }*/
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
    
//    public static class TaskConceptContent {
//        
//        public final Task task;
//        public final Concept concept;
//        public final Term content;
//
//        public static TaskConceptContent NULL = new TaskConceptContent();
//        
//        /** null placeholder */
//        protected TaskConceptContent() {
//            this.task = null;
//            this.concept = null;
//            this.content = null;
//        }
//
//        public TaskConceptContent(Task task, Concept concept, Term content) {
//            this.task = task;
//            this.concept = concept;
//            this.content = content;
//        }
//        
//    }
    
    
}
