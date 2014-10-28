package nars.inference;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import nars.core.EventEmitter.Observer;
import nars.core.Events.ConceptBeliefRemove;
import nars.core.Events.TaskDerive;
import nars.core.Memory;
import nars.core.Parameters;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.GraphExecutive.ParticlePlan;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Negation;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.language.Variables;
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

    public final TreeSet<TaskExecution> tasks;
    private Set<TaskExecution> tasksToRemove = new HashSet();
    
    /** number of tasks that are active in the sorted priority buffer for execution */
    int numActiveTasks = 1;

    /** max number of tasks that a plan can generate. chooses the N best */
    int maxPlannedTasks = 1;
    
    /** global plan search parameters */
    float searchDepth = 128;
    int particles = 64;
    
    /** inline search parameters */
    float inlineSearchDepth = searchDepth;
    int inlineParticles = 24;
    
    float maxExecutionsPerDuration = 1f;

    /** how much to multiply all cause relevancies per cycle */
    double causeRelevancyFactor = 0.999;
    
    HashSet<Task> current_tasks=new HashSet<>();

    /** how much to add value to each cause involved in a successful plan */ 
    //TODO move this to a parameter class visible to both Executive and GraphExecutive
    public static double relevancyOfSuccessfulPlan = 0.10;
    
    /** time of last execution */
    long lastExecution = -1;
    
    /** motivation set on an executing task to prevent other tasks from interrupting it, unless they are relatively urgent.
     * a larger value means it is more difficult for a new task to interrupt one which has
     * already begun executing.
     */
    float motivationToFinishCurrentExecution = 1.5f;
    
    public Executive(Memory mem) {
        this.memory = mem;    
                
        this.graph = new GraphExecutive(mem,this);

        this.tasks = new TreeSet<TaskExecution>(new Comparator<TaskExecution>() {
            @Override
            public final int compare(final TaskExecution b, final TaskExecution a) {
                if (a == b) return 0;
                float ap = a.getDesire();
                float bp = b.getDesire();
                if (bp != ap) {
                    return Float.compare(ap, bp);
                } else {
                    if(a.c!=null) {
                        float ad = a.c.getPriority();
                        float bd = b.c.getPriority();
                        if (ad!=bd)
                            return Float.compare(ad, bd);
                        else {
                            float add = a.c.getDurability();
                            float bdd = b.c.getDurability();
                            return Float.compare(add, bdd);                        
                        }
                    }
                }
                return 0;
            }
        }) {

            @Override
            public boolean add(TaskExecution e) {
                boolean b = super.add(e);                
                if (!b) return false;
                if (size() > numActiveTasks) {
                    TaskExecution l = last();
                    remove(l);
                    if (l != e)
                        removeTask(l);
                }
                return true;
            }
            
        };

    }

    public void setNumActiveTasks(int numActiveTasks) {
        this.numActiveTasks = numActiveTasks;
    }

    public int getNumActiveTasks() {
        return numActiveTasks;
    }
    
    public static class TaskExecution {
        /** may be null for input tasks */
        public final Concept c;
        
        public final Task t;
        public int sequence;
        public long delayUntil = -1;
        private float motivationFactor = 1;
        private TruthValue desire;
        public final Executive executive;
        final Memory memory;
        
        public TaskExecution(final Executive executive, TruthValue desire) { 
            this.memory = executive.memory;
            this.executive = executive;
            this.desire = desire;
            this.t = null;
            this.c = null;
        }
        
        public TaskExecution(Memory mem,final Executive executive, final Concept concept, Task t) {
            this.c = concept;            
            this.executive = executive;
            this.desire = t.getDesire();
            this.memory=mem;
            
            //Check if task is 
            if(Parameters.TEMPORAL_PARTICLE_PLANNER) {
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
            }
            
            this.t = t;

        }

        //TODO support multiple inline replacements        
        protected Task inlineConjunction(Task t, final Conjunction c) {
            ArrayDeque<Term> inlined = new ArrayDeque();
            boolean modified = false;
                        
            if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                Term prev = null;
                for (Term e : c.term) {
                    
                    if (!isPlanTerm(e)) {
                        if (executive.graph.isPlannable(e)) {
                            
                                                        
                            TreeSet<ParticlePlan> plans = executive.graph.particlePlan(e, executive.inlineSearchDepth, executive.inlineParticles);
                            if (plans.size() > 0) {
                                //use the first
                                ParticlePlan pp = plans.first();
                                
                                //if terms precede this one, remove a common prefix
                                //scan from the end of the sequence backward until a term matches the previous, and splice it there
                                //TODO more rigorous prefix compraison. compare sublist prefix
                                List<Term> seq = pp.sequence;
                                
//                                if (prev!=null) {
//                                    int previousTermIndex = pp.sequence.lastIndexOf(prev);
//                                    
//                                    if (previousTermIndex!=-1) {
//                                        if (previousTermIndex == seq.size()-1)
//                                            seq = Collections.EMPTY_LIST;
//                                        else {                                            
//                                            seq = seq.subList(previousTermIndex+1, seq.size());
//                                        }
//                                    }
//                                }
                                
                                //System.out.println("inline: " + seq + " -> " + e + " in " + c);
                                
                                //TODO adjust the truth value according to the ratio of term length, so that a small inlined sequence affects less than a larger one
                                desire = TruthFunctions.deduction(desire, pp.truth);
                                
                                //System.out.println(t.sentence.truth + " <- " + pp.truth + "    -> " + desire);
                                        
                                inlined.addAll(seq);
                                
                                
                                modified = true;
                            }
                            else {
                                //no plan available, this wont be able to execute   
                                end();
                            }
                        }
                        else {
                            //this won't be able to execute here
                            end();
                        }
                    }
                    else {
                        //executable term, add
                        inlined.add(e);
                    }
                    prev = e;
                }                            
            }            
            
            //remove suffix intervals
            if (inlined.size() > 0) {
                while (inlined.peekLast() instanceof Interval) {
                    inlined.removeLast();
                    modified = true;
                }
            }
            
            if (inlined.isEmpty())
                end();
            
            if (modified) {
                Conjunction nc = c.cloneReplacingTerms(inlined.toArray(new Term[inlined.size()]));
                t = t.clone(t.sentence.clone(nc) );                
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
            return desire.getExpectation() * motivationFactor;
        }
        public final float getPriority() { 
            Concept cc=memory.concept(t.sentence.content);
            if(cc!=null) {
                return cc.getPriority();
            }
            
            return 1;         }
        public final float getDurability() { return t.getDurability(); }
        //public final float getMotivation() { return getDesire() * getPriority() * motivationFactor;         }
        public final void setMotivationFactor(final float f) { this.motivationFactor = f;  }

        @Override public int hashCode() {            
            if (t == null) return desire.hashCode();
            return t.hashCode();         
        }

        @Override
        public String toString() {
            return "!" + Texts.n2Slow(getDesire()) + "|" + sequence + "! " + t.toString();
        }

        public void end() {
            setMotivationFactor(0);
            if (t!=null)
                t.end();
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
            final TaskExecution te = new TaskExecution(memory,this, c, t);
            if (tasks.add(te)) {
                //added successfully
                memory.emit(TaskExecution.class, te);
                return true;
            }
        }

        //t.end();
        return false;
    }
    
    protected void removeTask(final TaskExecution t) {
        if (tasksToRemove.add(t)) {            
//            if (memory.getRecorder().isActive())
//               memory.getRecorder().output("Executive", "Task Remove: " + t.toString());

            t.end();
        }
    }
    
    protected void updateTasks() {
        List<TaskExecution> t = new ArrayList(tasks);
        t.removeAll(tasksToRemove);
        tasks.clear();
        for (TaskExecution x : t) {
            if (x.getDesire() > 0) { // && (x.getPriority() > 0)) {
                tasks.add(x);
                
                //this is incompatible with the other usages of motivationFactor, so do not use this:
//                if ((x.delayUntil!=-1) && (x.delayUntil <= memory.getTime())) {
//                    //restore motivation so task can resume processing                    
//                    x.motivationFactor = 1.0f;
//                }
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
        
        //task.end(true);
        
    }   
        
    public void decisionPlanning(final NAL nal, final Task t, final Concept concept) {
        
        if (Parameters.TEMPORAL_PARTICLE_PLANNER) {
            
            if (!isDesired(t, concept)) return;
        
            boolean plannable = graph.isPlannable(t.getContent());
            if (plannable) {                                    
                graph.plan(nal, concept, t, t.getContent(), particles, searchDepth, '!', maxPlannedTasks);
            }                
        }
        
    }
    
    /** Entry point for all potentially executable tasks */
    public void decisionMaking(final Task t, final Concept concept) {
                        
        if (isDesired(t, concept)) {

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
    
    /** whether a concept's desire exceeds decision threshold */
    public boolean isDesired(final Task t, final Concept c) {             
        float desire = c.getDesire().getExpectation();
        float priority = t.budget.getPriority(); //t.budget.summary();
        return true; //always plan //(desire * priority) >= memory.param.decisionThreshold.get();
        
       // double dt = memory.param.decisionThreshold.get();
       // return ((desire >= dt) || (priority >= dt));
    }
    
    /** called during each memory cycle */
    public void cycle() {
        long now = memory.time();
        
        //only execute something no less than every duration time
        if (now - lastExecution < (memory.param.duration.get()/maxExecutionsPerDuration) )
            return;
                        
        lastExecution = now;
        
        updateTasks();
        updateSensors();

        if (tasks.isEmpty())
            return;
        
        if (memory.emitting(TaskExecution.class)) {
            
            if (tasks.size() > 1)  {                                
                for (TaskExecution tcc : tasks)
                    memory.emit(Executive.class, memory.time(), tcc);                    
            }
            else {
                memory.emit(Executive.class, memory.time(), tasks.first());
            }
            
        }
        
        
        TaskExecution topExecution = tasks.first();        
        Task top = topExecution.t;
        Term term = top.getContent();
        if (term instanceof Operation) {            
            execute((Operation)term, top); //directly execute            
            removeTask(topExecution);
            return;
        }
        else if (Parameters.TEMPORAL_PARTICLE_PLANNER && term instanceof Conjunction) {
            Conjunction c = (Conjunction)term;
            if (c.operator() == Symbols.NativeOperator.SEQUENCE) {
                executeConjunctionSequence(topExecution, c);
                return;
            }
            
        }
        else if (Parameters.TEMPORAL_PARTICLE_PLANNER && term instanceof Implication) {
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
            //throw new RuntimeException("Unknown Task type: "+ top);
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
    

    public Task expected_task=null;
    public Term expected_event=null;

    private void executeConjunctionSequence(final TaskExecution task, final Conjunction c) {
        int s = task.sequence;
        Term currentTerm = c.term[s];
        
        long now = memory.time();
        
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
            task.delayUntil = memory.time() + Interval.magnitudeToTime(ui.magnitude, memory.param.duration);
            s++;
        }        
        else {            
            System.err.println("Non-executable term in sequence: " + currentTerm + " in " + c + " from task " + task.t);
            //removeTask(task); //was never executed, dont remove
        }

        if (s == c.term.length) {
            //completed task
           
            if(task.t.sentence.content instanceof Implication) {
                expected_task=task.t;
                expected_event=((Implication)task.t.sentence.content).getPredicate();
            }
            
            removeTask(task);
            task.sequence=0;
        }
        else {            
            //still incomplete
            task.sequence = s;
            task.setMotivationFactor(motivationToFinishCurrentExecution);
        }
    }
    
    public Task stmLast=null;
    public Term anticipateTerm=null;
    public long anticipateTime=0;
    public boolean inductionOnSucceedingEvents(final Task newEvent, NAL nal) {

        //new one happened and duration is already over, so add as negative task
        if(Parameters.INTERNAL_EXPERIENCE_FULL && anticipateTerm!=null && newEvent.sentence.getOccurenceTime()-anticipateTime>nal.mem.param.duration.get()) {
            Term s=newEvent.sentence.content;
            TruthValue truth=new TruthValue(0.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
            Negation N=(Negation) Negation.make(s);
            Stamp stamp=new Stamp(nal.mem);
            Sentence S=new Sentence(N, Symbols.JUDGMENT_MARK, truth, stamp);
            BudgetValue budget=new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth));
            Task task=new Task(S,budget);
            nal.derivedTask(task, false, true, null, null);
            anticipateTerm=null;
        }
        if(Parameters.INTERNAL_EXPERIENCE_FULL && anticipateTerm!=null && newEvent.sentence.truth.getExpectation()>0.5 && newEvent.sentence.content.equals(anticipateTerm)) {
            anticipateTerm=null; //it happened like expected
        }
        
        if (newEvent == null || newEvent.sentence.stamp.getOccurrenceTime()==Stamp.ETERNAL || !isInputOrTriggeredOperation(newEvent,nal.mem))
            return false;

        if (stmLast!=null) { 

            if(equalSubTermsInRespectToImageAndProduct(newEvent.sentence.content,stmLast.sentence.content)) {
                return false;
            }
            
            nal.setTheNewStamp(newEvent.sentence.stamp, stmLast.sentence.stamp, memory.time());
            nal.setCurrentTask(newEvent);
                        
            Sentence currentBelief = stmLast.sentence;
            nal.setCurrentBelief(currentBelief);
            
            //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY) {
                TemporalRules.temporalInduction(newEvent.sentence, currentBelief, nal);
            //}
        }

        //for this heuristic, only use input events & task effects of operations
        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY) {
            stmLast=newEvent;
        //}

        return true;
    }
    
    //is input or by the system triggered operation
    public boolean isInputOrTriggeredOperation(final Task newEvent, Memory mem) {
        if(!((newEvent.isInput() || Parameters.INTERNAL_EXPERIENCE_FULL) || (newEvent.getCause()!=null))) {
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
    
    
    protected void updateSensors() {
        memory.logic.PLAN_GRAPH_EDGE.commit(graph.implication.edgeSet().size());
        memory.logic.PLAN_GRAPH_VERTEX.commit(graph.implication.vertexSet().size());
        memory.logic.PLAN_TASK_EXECUTABLE.commit(tasks.size());                
    }
    
}
