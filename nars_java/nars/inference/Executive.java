package nars.inference;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import nars.core.Events;
import nars.core.Events.UnexecutableOperation;
import nars.core.Memory;
import nars.core.Parameters;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import static nars.inference.TemporalRules.order;
import nars.io.Symbols;
import nars.io.Texts;
import nars.language.Conjunction;
import nars.language.Implication;
import nars.language.Interval;
import nars.language.Negation;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.operator.math.Add;
import nars.operator.math.Count;
import nars.operator.mental.Anticipate;
import nars.operator.mental.Believe;
import nars.operator.mental.Consider;
import nars.operator.mental.Doubt;
import nars.operator.mental.Evaluate;
import nars.operator.mental.Feel;
import nars.operator.mental.FeelBusy;
import nars.operator.mental.FeelHappy;
import nars.operator.mental.Hesitate;
import nars.operator.mental.Name;
import nars.operator.mental.Register;
import nars.operator.mental.Remind;
import nars.operator.mental.Want;
import nars.operator.mental.Wonder;
import nars.operator.software.Javascript;
import nars.operator.software.NumericCertainty;
import nars.plugin.mental.Abbreviation.Abbreviate;
import nars.plugin.mental.TemporalParticlePlanner;

/**
 * Operation execution and planning support. Strengthens and accelerates
 * goal-reaching activity
 */
public class Executive {

    public final GraphExecutive graph;

    public final Memory memory;

    ///** memory for faster execution of &/ statements (experiment) */
    //public final Deque<TaskConceptContent> next = new ArrayDeque<>();
    public final NavigableSet<Execution> tasks;
    private final Set<Execution> tasksToRemove = new ConcurrentSkipListSet();

    /**
     * number of tasks that are active in the sorted priority buffer for
     * execution
     */
    int numActiveTasks = 1;



    float maxExecutionsPerDuration = 1f;

    /**
     * how much to multiply all cause relevancies per cycle
     */
    double causeRelevancyFactor = 0.999;

    //TODO provide single thread version when applicable, slightly faster
    Set<Task> current_tasks = new ConcurrentSkipListSet<>();

    /**
     * how much to add value to each cause involved in a successful plan
     */
    //TODO move this to a parameter class visible to both Executive and GraphExecutive
    public static double relevancyOfSuccessfulPlan = 0.10;

    /**
     * time of last execution
     */
    long lastExecution = -1;

    /**
     * motivation set on an executing task to prevent other tasks from
     * interrupting it, unless they are relatively urgent. a larger value means
     * it is more difficult for a new task to interrupt one which has already
     * begun executing.
     */
    float motivationToFinishCurrentExecution = 1.5f;

    public Executive(Memory mem) {
        this.memory = mem;

        this.graph = new GraphExecutive(mem, this);

        this.tasks = new ConcurrentSkipListSet<Execution>() {

            @Override
            public boolean add(Execution e) {
                boolean b = super.add(e);
                if (!b) {
                    return false;
                }
                if (size() > numActiveTasks) {
                    Execution l = last();
                    remove(l);
                    if (l != e) {
                        removeExecution(l);
                    }
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

    public static class Execution implements Comparable<Execution> {

        /**
         * may be null for input tasks
         */
        public final Concept c;

        public Task t; //TODO make private
        
        public int sequence;
        public long delayUntil = -1;
        private float motivationFactor = 1;
        private TruthValue desire;
        public final Executive executive;
        final Memory memory;

        public Execution(final Executive executive, TruthValue desire) {
            this.memory = executive.memory;
            this.executive = executive;
            this.desire = desire;
            this.t = null;
            this.c = null;
        }

        public Execution(Memory mem, final Executive executive, final Concept concept, Task t) {
            this.c = concept;
            this.executive = executive;
            this.desire = t.getDesire();
            this.memory = mem;

            memory.emit(Events.NewTaskExecution.class, this);
            
            this.t = t;

        }
        
        public void setTask(Task t) {
            this.t = t;
        }               

        public TruthValue getDesireValue() {
            return desire;
        }
        

        public void setDesire(TruthValue desire) {
            this.desire = desire;
        }

        
        @Override
        public int compareTo(final Execution a) {
            final Execution b = this;

            if (a == b) {
                return 0;
            }
            float ap = a.getDesire();
            float bp = b.getDesire();
            if (bp != ap) {
                return Float.compare(ap, bp);
            } else {
                if (a.c != null) {
                    float ad = a.c.getPriority();
                    float bd = b.c.getPriority();
                    if (ad != bd) {
                        return Float.compare(ad, bd);
                    } else {
                        float add = a.c.getDurability();
                        float bdd = b.c.getDurability();
                        return Float.compare(add, bdd);
                    }
                }
            }
            return 0;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Execution) {
                return ((Execution) obj).t.equals(t);
            }
            return false;
        }

        public final float getDesire() {
            return desire.getExpectation() * motivationFactor;
        }

        public final float getPriority() {
            Concept cc = memory.concept(t.sentence.content);
            if (cc != null) {
                return cc.getPriority();
            }

            return 1;
        }

        public final float getDurability() {
            return t.getDurability();
        }

        //public final float getMotivation() { return getDesire() * getPriority() * motivationFactor;         }

        public final void setMotivationFactor(final float f) {
            this.motivationFactor = f;
        }

        @Override
        public int hashCode() {
            if (t == null) {
                return desire.hashCode();
            }
            return t.hashCode();
        }

        @Override
        public String toString() {
            return "!" + Texts.n2Slow(getDesire()) + "|" + sequence + "! " + t.toString();
        }

        public void end() {
            setMotivationFactor(0);
            if (t != null) {
                t.end();
            }
        }

        public Task getTask() {
            return t;
        }

    }

    protected Execution getExecution(final Task parent) {
        for (final Execution t : tasks) {
            if (t.t.parentTask != null) {
                if (t.t.parentTask.equals(parent)) {
                    return t;
                }
            }
        }
        return null;
    }

    public boolean addExecution(final Concept c, final Task t) {

        Execution existingExecutable = getExecution(t.parentTask);
        boolean valid = true;
        if (existingExecutable != null) {

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
            final Execution te = new Execution(memory, this, c, t);
            if (tasks.add(te)) {
                //added successfully

                memory.emit(Execution.class, te);
                return true;
            }
        }

        //t.end();
        return false;
    }

    protected void removeExecution(final Execution t) {
        if (tasksToRemove.add(t)) {
            t.end();
        }
    }

    protected void updateTasks() {
        Set<Execution> t = new HashSet(tasks);
        
        for (Execution e : tasksToRemove) {
            t.remove(e);
        }

        tasks.clear();
        for (Execution x : t) {
            
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
    
    /** execute TaskExecution that contains a single operation, and when complete, reomve the task */
    public void execute(Execution executing, final Operation op, final Task task) {
        execute(op, task);
        removeExecution(executing);
    }
    
    public void execute(final Operation op, final Task task) {

        Operator oper = op.getOperator();

        //if (NAR.DEBUG)
        //System.out.println("exe: " + task.getExplanation().trim());
        op.setTask(task);

        oper.call(op, memory);

        //task.end(true);
    }

    /**
     * Entry point for all potentially executable tasks
     */
    public void decisionMaking(final Task t, final Concept concept) {

        if (isDesired(t, concept)) {

            Term content = concept.term;

            if ((content instanceof Operation) || (TemporalParticlePlanner.used && isSequenceConjunction(content))) {
                addExecution(concept, t);
            } 
        } else {
            //t.end();
        }
    }

    /**
     * whether a concept's desire exceeds decision threshold
     */
    public boolean isDesired(final Task t, final Concept c) {
        float desire = c.getDesire().getExpectation();
        float priority = t.budget.getPriority(); //t.budget.summary();
        return desire > memory.param.decisionThreshold.get(); //always plan //(desire * priority) >= memory.param.decisionThreshold.get();

       // double dt = memory.param.decisionThreshold.get();
        // return ((desire >= dt) || (priority >= dt));
    }

    /**
     * called during each memory cycle
     */
    public void cycle() {
        long now = memory.time();

        //only execute something no less than every duration time
        if (now - lastExecution < (memory.param.duration.get() / maxExecutionsPerDuration)) {
            return;
        }

        lastExecution = now;

        updateTasks();
        updateSensors();

        
        if (tasks.isEmpty()) {
            return;
        }

        //System.out.println(now + " tasks=" + tasks);
        
        if (memory.emitting(Execution.class)) {

            if (tasks.size() > 1) {
                for (Execution tcc : tasks) {
                    memory.emit(Executive.class, memory.time(), tcc);
                }
            } else {
                memory.emit(Executive.class, memory.time(), tasks.first());
            }

        }

        Execution executing = tasks.first();
        Task top = executing.t;
        Term term = top.getContent();
        if (term instanceof Operation) {
            execute(executing, (Operation) term, top); //directly execute            
            return;
        } 
        else {
            memory.emit(UnexecutableOperation.class, executing, this);            
        }
        
        //throw new RuntimeException("Unrecognized executable term: " + it.getSubject() + "[" + it.getSubject().getClass() + "] from " + top);

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
            Conjunction cc = ((Conjunction) c);
            return (cc.operator() == Symbols.NativeOperator.SEQUENCE);
            //{
            //return (cc.getTemporalOrder()==TemporalRules.ORDER_FORWARD) || (cc.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT);
            //}
        }
        return false;
    }

    public Task expected_task = null;
    public Term expected_event = null;

    public void executeConjunctionSequence(final Execution task, final Conjunction c) {
        int s = task.sequence;
        Term currentTerm = c.term[s];

        long now = memory.time();

        if (task.delayUntil > now) {
            //not ready to execute next term
            return;
        }

        if (currentTerm instanceof Operation) {
            Concept conc = memory.concept(currentTerm);
            execute((Operation) currentTerm, task.t);
            task.delayUntil = now + memory.getDuration();
            s++;
        } else if (currentTerm instanceof Interval) {
            Interval ui = (Interval) currentTerm;
            task.delayUntil = memory.time() + Interval.magnitudeToTime(ui.magnitude, memory.param.duration);
            s++;
        } else {
            /*System.err.println("Non-executable term in sequence: " + currentTerm + " in " + c + " from task " + task.t);*/
            removeExecution(task); //was never executed, dont remove
        }

        if (s == c.term.length) {
            //completed task

            if (task.t.sentence.content instanceof Implication) {
                expected_task = task.t;
                expected_event = ((Implication) task.t.sentence.content).getPredicate();
            }

            removeExecution(task);
            
        } else {
            //still incomplete
            task.sequence = s;
            task.setMotivationFactor(motivationToFinishCurrentExecution);
        }
    }

    public Task stmLast = null;
    public Term anticipateTerm = null;
    public long anticipateTime = 0;

    public boolean inductionOnSucceedingEvents(final Task newEvent, NAL nal) {

        if(newEvent.budget==null) {
            return false;
        }
        //new one happened and duration is already over, so add as negative task
        if (Parameters.INTERNAL_EXPERIENCE_FULL && anticipateTerm != null && 
                (order(anticipateTime, newEvent.sentence.getOccurenceTime(), memory.getDuration()) == TemporalRules.ORDER_FORWARD)) {
            Term s = newEvent.sentence.content;
            TruthValue truth = new TruthValue(0.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
            Negation N = (Negation) Negation.make(s);
            Stamp stamp = new Stamp(nal.memory);
            Sentence S = new Sentence(N, Symbols.JUDGMENT_MARK, truth, stamp);
            BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth));
            Task task = new Task(S, budget);
            nal.derivedTask(task, false, true, null, null);
            anticipateTerm = null;
        }
        if (Parameters.INTERNAL_EXPERIENCE_FULL && anticipateTerm != null && newEvent.sentence.truth.getExpectation() > 0.5 && newEvent.sentence.content.equals(anticipateTerm)) {
            anticipateTerm = null; //it happened like expected
        }

        if (newEvent == null || newEvent.sentence.isEternal() || !isInputOrTriggeredOperation(newEvent, nal.memory)) {
            return false;
        }

        if (stmLast != null) {

            if (equalSubTermsInRespectToImageAndProduct(newEvent.sentence.content, stmLast.sentence.content)) {
                return false;
            }

            nal.setTheNewStamp(newEvent.sentence.stamp, stmLast.sentence.stamp, memory.time());
            nal.setCurrentTask(newEvent);

            Sentence currentBelief = stmLast.sentence;
            nal.setCurrentBelief(currentBelief);

            //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
            TemporalRules.temporalInduction(newEvent.sentence, currentBelief, nal);
        }

        //for this heuristic, only use input events & task effects of operations
        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY) {
        stmLast = newEvent;
        //}

        return true;
    }

    public boolean contains_mental(Task t) {
        if(!(t.sentence.content instanceof Operation)) {
            return false;
        }
        Operation o=(Operation) t.sentence.content;
        Operator op=(Operator) o.getPredicate();
        
        //TODO replace with common interface that can be instanceof compared
        if(op instanceof Anticipate || op instanceof Believe || op instanceof Consider || op instanceof Doubt ||
                op instanceof Evaluate || op instanceof Feel || op instanceof FeelBusy || op instanceof FeelHappy ||
                op instanceof Hesitate || op instanceof Name || op instanceof Register || op instanceof Remind ||
                op instanceof Want || op instanceof Wonder || op instanceof Add ||
                op instanceof Count || op instanceof Javascript || op instanceof NumericCertainty || op instanceof Abbreviate) {
            return true;
        }
        return false;
    }
    
    //is input or by the system triggered operation
    public boolean isInputOrTriggeredOperation(final Task newEvent, Memory mem) {
        if (!((newEvent.isInput() || (Parameters.INTERNAL_EXPERIENCE && contains_mental(newEvent))) || (newEvent.getCause() != null))) {
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
