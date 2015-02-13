/*
 * Memory.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.core;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import javolution.context.ConcurrentContext;
import nars.core.Attention.AttentionAware;
import nars.core.Events.ResetEnd;
import nars.core.Events.ResetStart;
import nars.core.Events.TaskRemove;
import static nars.core.Memory.Forgetting.Periodic;
import static nars.core.Memory.Timing.Iterative;
import nars.core.control.AbstractTask;
import nars.core.control.ImmediateProcess;
import nars.core.control.NAL;
import nars.io.meter.EmotionMeter;
import nars.io.meter.LogicMeter;
import nars.io.meter.ResourceMeter;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.plugin.app.plan.MultipleExecutionManager;
import static nars.plugin.app.plan.MultipleExecutionManager.isInputOrTriggeredOperation;
import nars.inference.TemporalRules;
import nars.io.Output.ERR;
import nars.io.Output.IN;
import nars.io.Output.OUT;
import nars.io.Symbols;
import nars.io.Symbols.NativeOperator;
import static nars.io.Symbols.NativeOperator.CONJUNCTION;
import static nars.io.Symbols.NativeOperator.DIFFERENCE_EXT;
import static nars.io.Symbols.NativeOperator.DIFFERENCE_INT;
import static nars.io.Symbols.NativeOperator.DISJUNCTION;
import static nars.io.Symbols.NativeOperator.EQUIVALENCE;
import static nars.io.Symbols.NativeOperator.EQUIVALENCE_AFTER;
import static nars.io.Symbols.NativeOperator.EQUIVALENCE_WHEN;
import static nars.io.Symbols.NativeOperator.IMAGE_EXT;
import static nars.io.Symbols.NativeOperator.IMAGE_INT;
import static nars.io.Symbols.NativeOperator.IMPLICATION;
import static nars.io.Symbols.NativeOperator.IMPLICATION_AFTER;
import static nars.io.Symbols.NativeOperator.IMPLICATION_BEFORE;
import static nars.io.Symbols.NativeOperator.IMPLICATION_WHEN;
import static nars.io.Symbols.NativeOperator.INHERITANCE;
import static nars.io.Symbols.NativeOperator.INTERSECTION_EXT;
import static nars.io.Symbols.NativeOperator.INTERSECTION_INT;
import static nars.io.Symbols.NativeOperator.NEGATION;
import static nars.io.Symbols.NativeOperator.PARALLEL;
import static nars.io.Symbols.NativeOperator.PRODUCT;
import static nars.io.Symbols.NativeOperator.SEQUENCE;
import static nars.io.Symbols.NativeOperator.SET_EXT_OPENER;
import static nars.io.Symbols.NativeOperator.SET_INT_OPENER;
import nars.language.CompoundTerm;
import nars.language.Conjunction;
import nars.language.DifferenceExt;
import nars.language.DifferenceInt;
import nars.language.Disjunction;
import nars.language.Equivalence;
import nars.language.Image;
import nars.language.ImageExt;
import nars.language.ImageInt;
import nars.language.Implication;
import nars.language.Inheritance;
import nars.language.IntersectionExt;
import nars.language.IntersectionInt;
import nars.language.Negation;
import nars.language.Product;
import nars.language.SetExt;
import nars.language.SetInt;
import nars.language.Tense;
import nars.language.Term;
import static nars.language.Terms.equalSubTermsInRespectToImageAndProduct;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.operator.io.Echo;
import nars.operator.io.PauseInput;
import nars.operator.io.Reset;
import nars.operator.io.SetVolume;
import nars.storage.Bag;


/**
 * Memory consists of the run-time state of a NAR, including:
 *   * term and concept memory
 *   * clock
 *   * reasoner state
 *   * etc.
 * 
 * Excluding input/output channels which are managed by a NAR.  
 * 
 * A memory is controlled by zero or one NAR's at a given time.
 * 
 * Memory is serializable so it can be persisted and transported.
 */
public class Memory implements Serializable {
    
    public final MultipleExecutionManager executive; //used for implication graph and for planner plugin, todo 
    //get it out to plugin somehow
    
    private boolean enabled = true;
    
    private long timeRealStart;
    private long timeRealNow;
    private long timePreviousCycle;
    private long timeSimulation;



    public static enum Forgetting {
        Iterative, Periodic
    }
    
    public enum Timing {
        /** internal, subjective time (inference steps) */
        Iterative, 
        
        /** hard real-time, uses system clock */
        Real, 
        
        /** soft real-time, uses controlled simulation time */
        Simulation
    }
    
    Timing timing;
    
    
    public static interface TaskSource {
        public AbstractTask nextTask();

        public int getInputItemsBuffered();
    }
    
    //public static Random randomNumber = new Random(1);
    public static long randomSeed = 1;
    public static Random randomNumber = new Random(randomSeed);

    public static void resetStatic() {
        randomNumber.setSeed(randomSeed);    
    }
    
    public final Attention concepts;
    
    public final EventEmitter event;
    
    

    
    
    /* InnateOperator registry. Containing all registered operators of the system */
    public final HashMap<CharSequence, Operator> operators;
    
    private long currentStampSerial = 0;
    
    
    
    /**
     * New tasks with novel composed terms, for delayed and selective processing
     */
    public final Bag<Task<Term>,Sentence<Term>> novelTasks;
   
    /* ---------- Short-term workspace for a single cycle ---	------- */
    
    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    public final Deque<Task> newTasks;
    
    
    
    
    // ----------------------------------------
//    public Term currentTerm;
//
//    public Concept currentConcept;
//
//    private Task currentTask;
//
//    private TermLink currentBeliefLink;
//    private TaskLink currentTaskLink;
//
//    private Sentence currentBelief;    
//
//    private Stamp newStamp;
    // ----------------------------------------
    
    
    //public final Term self;

    
    

    
    public final EmotionMeter emotion = new EmotionMeter();    
    public final LogicMeter logic;
    public final ResourceMeter resource;
    
    
    /**
     * The remaining number of steps to be carried out (stepLater mode)
     */
    private int inputPausedUntil;

    
    /**
     * System clock, relatively defined to guarantee the repeatability of
     * behaviors
     */
    private long cycle;
    
    
    public final Param param;
    
    
    //index of Conjunction questions
    transient private Set<Task> questionsConjunction = new HashSet();
    

    
    private class MemoryEventEmitter extends EventEmitter {        
        @Override public void emit(final Class eventClass, final Object... params) {
            super.emit(eventClass, params); 

            if (eventClass == Events.ConceptQuestionAdd.class) {                    
                //Concept c = params[0];
                Task t = (Task)params[1];
                Term term = t.getTerm();
                if (term instanceof Conjunction) {
                    questionsConjunction.add(t);
                }
            }
            else if (eventClass == Events.ConceptQuestionAdd.class) {
                //Concept c = params[0];
                Task t = (Task)params[1];
                Term term = t.getTerm();
                if (term instanceof Conjunction) {
                    questionsConjunction.remove(t);
                }
            }
        }
    }
    
    /* ---------- Constructor ---------- */
    /**
     * Create a new memory
     *
     * @param initialOperators - initial set of available operators; more may be added during runtime
     */
    public Memory(Param param, Attention concepts, Bag<Task<Term>,Sentence<Term>> novelTasks) {                

        this.param = param;
        
        this.event = new MemoryEventEmitter();
        
        this.concepts = concepts;
        this.concepts.init(this);
        
        this.novelTasks = novelTasks;                
        if (novelTasks instanceof AttentionAware)
            ((AttentionAware)novelTasks).setAttention(concepts);
        
        this.newTasks = (Parameters.THREADS > 1) ?  
                new ConcurrentLinkedDeque<>() : new ArrayDeque<>();
        
        this.operators = new HashMap<>();
        

        this.resource = new ResourceMeter();
        this.logic = new LogicMeter() {

            @Override
            public void commit(Memory memory) {
                double prioritySum = 0;        
                double prioritySumSq = 0;
                int count = 0;
                int totalQuestions = 0;
                int totalBeliefs = 0;
                int histogramBins = 4;
                double[] histogram = new double[histogramBins];

                for (final Concept c : concepts) {
                    double p = c.getPriority();
                    totalQuestions += c.questions.size();        
                    totalBeliefs += c.beliefs.size();        
                    //TODO totalGoals...
                    //TODO totalQuests...
                    
                    prioritySum += p;
                    prioritySumSq += p*p;
                    
                    if (p > 0.75) histogram[0]++;
                    else if (p > 0.5) histogram[1]++;
                    else if (p > 0.25) histogram[2]++;
                    else histogram[3]++;
                    
                    count++;
                }
                double mean, variance;
                if (count > 0) {
                    mean = prioritySum / count;
                    
                    //http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
                    variance = (prioritySumSq - ((prioritySum*prioritySum)/count))/(count-1);
                    for (int i = 0; i < histogram.length; i++)
                        histogram[i]/=count;
                }
                else {
                    mean = variance = 0;
                }

                setConceptNum(count);
                setConceptBeliefsSum(totalBeliefs);
                setConceptQuestionsSum(totalQuestions);
                setConceptPriorityMean(mean);
                setConceptPriorityVariance(variance);
                setConceptPriorityHistogram(histogram);
                
                super.commit(memory);
            }

        };
        
        
        
        this.executive = new MultipleExecutionManager(this);
                
        //after this line begins actual inference, now that the essential data strucures are allocated
        //------------------------------------ 
                
                
        reset();

    }
    
    public void reset() {
        event.emit(ResetStart.class);
        
        concepts.reset();
        novelTasks.clear();
        newTasks.clear();     
        
        timing = param.getTiming();      
        cycle = 0;
        timeRealStart = timeRealNow = System.currentTimeMillis();
        timePreviousCycle = time();
        
        inputPausedUntil = 0;
        
        emotion.set(0.5f, 0.5f);
        resetStatic();
        
        event.emit(ResetEnd.class);
       
    }

    public long time() {
        switch (timing) {
            case Iterative: return getCycleTime();
            case Real: return getRealTime();
            case Simulation: return getSimulationTime();
        }
        return 0;
    }
    
    public int getDuration() {
        return param.duration.get();
    }
    
    /** internal, subjective time (inference steps) */
    public long getCycleTime() {
        return cycle;
    }

    /** hard real-time, uses system clock */
    public long getRealTime() {
        return timeRealNow - timeRealStart;
    }
    
    /** soft real-time, uses controlled simulation time */
    public long getSimulationTime() {
        return timeSimulation;
    }
    
    public void addSimulationTime(long dt) {
        timeSimulation += dt;
    }
    
    /** difference in time since last cycle */
    public long getTimeDelta() {
        return time() - timePreviousCycle;
    }

    public Deque<Task> getNewTasks() {
        return newTasks;
    }
    

    /* ---------- conversion utilities ---------- */
    /**
     * Get an existing Concept for a given name
     * <p>
     * called from Term and ConceptWindow.
     *
     * @param t the name of a concept
     * @return a Concept or null
     */
    public Concept concept(final Term t) {
        return concepts.concept(t);
    }
  



    //TODO decide if this is necessary
    public void temporalRuleOutputToGraph(Sentence s, Task t) {
        if(t.sentence.term instanceof Implication && t.sentence.term.getTemporalOrder()!=TemporalRules.ORDER_INVALID && t.sentence.term.getTemporalOrder()!=TemporalRules.ORDER_NONE && t.sentence.term.getTemporalOrder()!=TemporalRules.ORDER_BACKWARD) {
            
            executive.graph.implication.add(s, (CompoundTerm)s.term, t);            
        }

    }


    /**
     * Get the Concept associated to a Term, or create it.
     * 
     *   Existing concept: apply tasklink activation (remove from bag, adjust budget, reinsert)
     *   New concept: set initial activation, insert
     *   Subconcept: extract from cache, apply activation, insert
     * 
     * If failed to insert as a result of null bag, returns null
     *
     * A displaced Concept resulting from insert is forgotten (but may be stored in optional  subconcept memory
     * 
     * @param term indicating the concept
     * @return an existing Concept, or a new one, or null 
     */
    public Concept conceptualize(final BudgetValue budget, final Term term) {
        boolean createIfMissing = true;
        
        /*Concept c = concept(term);
        if (c!=null)
            System.out.print(c.budget + "   ");
        System.out.println(term + " conceptualize: " + budget);*/
                
        return concepts.conceptualize(budget, term, createIfMissing);
    }

    /**
     * Get the current activation level of a concept.
     *
     * @param t The Term naming a concept
     * @return the priority value of the concept
     */
    public float conceptActivation(final Term t) {
        final Concept c = concept(t);
        return (c == null) ? 0f : c.getPriority();
    }


    /* static methods making new compounds, which may return null */
    /**
     * Try to make a compound term from a template and a list of term
     *
     * @param compound The template
     * @param components The term
     * @param memory Reference to the memory
     * @return A compound term or null
     */
    public Term term(final CompoundTerm compound, final Term[] components) {
        if (compound instanceof ImageExt) {
            return new ImageExt(components, ((Image) compound).relationIndex);
        } else if (compound instanceof ImageInt) {
            return ImageInt.make(components, ((Image) compound).relationIndex);
        } else {
            return term(compound.operator(), components);
        }
    }

    public Term term(final CompoundTerm compound, Collection<Term> components) {
        Term[] c = components.toArray(new Term[components.size()]);
        return term(compound, c);
    }
    

    /**
     * Try to make a compound term from an operator and a list of term
     * <p>
     * Called from StringParser
     *
     * @param op Term operator
     * @param arg Component list
     * @return A term or null
     */
    public Term term(final NativeOperator op, final Term[] a) {
        
        switch (op) {
            
            case SET_EXT_OPENER:
                return SetExt.make(a);
            case SET_INT_OPENER:
                return SetInt.make(a);
            case INTERSECTION_EXT:
                return IntersectionExt.make(a);
            case INTERSECTION_INT:
                return IntersectionInt.make(a);
            case DIFFERENCE_EXT:
                return DifferenceExt.make(a);
            case DIFFERENCE_INT:
                return DifferenceInt.make(a);
            case INHERITANCE:
                return Inheritance.make(a[0], a[1]);
            case PRODUCT:
                return new Product(a);
            case IMAGE_EXT:
                return ImageExt.make(a);
            case IMAGE_INT:
                return ImageInt.make(a);
            case NEGATION:
                return Negation.make(a);
            case DISJUNCTION:
                return Disjunction.make(a);
            case CONJUNCTION:
                return Conjunction.make(a);
            case SEQUENCE:
                return Conjunction.make(a, TemporalRules.ORDER_FORWARD);
            case PARALLEL:
                return Conjunction.make(a, TemporalRules.ORDER_CONCURRENT);
            case IMPLICATION:
                return Implication.make(a[0], a[1]);
            case IMPLICATION_AFTER:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_FORWARD);
            case IMPLICATION_BEFORE:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_BACKWARD);
            case IMPLICATION_WHEN:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE:
                return Equivalence.make(a[0], a[1]);
            case EQUIVALENCE_WHEN:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_FORWARD);
        }
        throw new RuntimeException("Unknown Term operator: " + op + " (" + op.name() + ")");
    }
    
    /** 
     * this will not remove a concept.  it is not good to use directly because it can disrupt 
     * the bag's priority order. it should only be used after it has been removed then before inserted */
    public void forget(final Item x, final float forgetCycles, final float relativeThreshold) {
        switch (param.forgetting) {
            case Iterative:                
                BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
                break;
            case Periodic:
                BudgetFunctions.forgetPeriodic(x.budget, forgetCycles, relativeThreshold, time());
                break;
        }
    }    
    
    /* ---------- new task entries ---------- */
    /**
     * add new task that waits to be processed in the next cycleMemory
     */
    public void addNewTask(final Task t, final String reason) {
        /*if (!Term.valid(t.getContent()))
            throw new RuntimeException("Invalid term: " + t);*/
        
        newTasks.add(t);
                
        logic.TASK_ADD_NEW.commit(t.getPriority());
        
        emit(Events.TaskAdd.class, t, reason);
        
        output(t);
    }
    
    /* There are several types of new tasks, all added into the
     newTasks list, to be processed in the next cycleMemory.
     Some of them are reported and/or logged. */
    /**
     * Input task processing. Invoked by the outside or inside environment.
 Outside: StringParser (addInput); Inside: InnateOperator (feedback). Input
 tasks with low priority are ignored, and the others are put into task
 buffer.
     *
     * @param t The addInput task
     */
    public void inputTask(final AbstractTask t) {
        
        if (t instanceof Task) {
            Task task = (Task)t;
            Stamp s = task.sentence.stamp;                        
            if (s.getCreationTime()==-1)
                s.setCreationTime(time(), param.duration.get());

            emit(IN.class, task);

            if (task.budget.aboveThreshold()) {
                temporalRuleOutputToGraph(task.sentence,task);
                
                addNewTask(task, "Perceived");
                
            } else {
                removeTask(task, "Neglected");
            }
        }
        else if (t instanceof PauseInput) {            
            stepLater(((PauseInput)t).cycles);            
            emit(IN.class, t);
        }
        else if (t instanceof Reset) {
            reset();
            emit(OUT.class,((Reset) t).input);
            emit(IN.class, t);
        }
        else if (t instanceof Echo) {
            Echo e = (Echo)t;
            emit(e.channel, e.signal);
        }
        else if (t instanceof SetVolume) {            
            param.noiseLevel.set(((SetVolume)t).volume);
            emit(IN.class, t);
        }            
        else {
            emit(IN.class, "Unrecognized Input Task: " + t);
        }
    }



    public void removeTask(final Task task, final String reason) {        
        emit(TaskRemove.class, task, reason);
        task.end();        
    }
    
    /**
     * ExecutedTask called in Operator.call
     *
     * @param operation The operation just executed
     */
    public void executedTask(final Operation operation, TruthValue truth) {
        Task opTask = operation.getTask();
        logic.TASK_EXECUTED.commit(opTask.budget.getPriority());
                
        Stamp stamp = new Stamp(this, Tense.Present); 
        Sentence sentence = new Sentence(operation, Symbols.JUDGMENT_MARK, truth, stamp);
        
        Task task = new Task(sentence, opTask.budget, operation.getTask());
        task.setCause(operation);
        
        addNewTask(task, "Executed");
    }

    public void output(final Task t) {
        
        final float budget = t.budget.summary();
        final float noiseLevel = 1.0f - (param.noiseLevel.get() / 100.0f);
        
        if (budget >= noiseLevel) {  // only report significant derived Tasks
            emit(OUT.class, t);
        }        
    }
    
    final public void emit(final Class c, final Object... signal) {        
        event.emit(c, signal);
    }

    final public boolean emitting(final Class channel) {
        return event.isActive(channel);
    }

    
    /** enable/disable all I/O and memory processing.  CycleStart and CycleStop events will 
     continue to be generated, allowing the memory to be used as a clock tick while disabled. */
    public void setEnabled(boolean e) {
        this.enabled = e;
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    
    
    public void cycle(final TaskSource inputs) {

        if (!isEnabled())
            return;
        
        resource.CYCLE.start();
        resource.CYCLE_CPU_TIME.start();
        resource.CYCLE_RAM_USED.start();

        if (logic.IO_INPUTS_BUFFERED.isActive())
            logic.IO_INPUTS_BUFFERED.commit(inputs.getInputItemsBuffered());
        
        event.emit(Events.CycleStart.class);                

            
        int inputTaskPriority = concepts.getInputPriority();
        
        /** adds input tasks to newTasks */
        for (int i = 0; (i < inputTaskPriority) && (isProcessingInput()); i++) {
            AbstractTask t = inputs.nextTask();                    
            if (t!=null) 
                inputTask(t);            
        }
      

        concepts.cycle();
                           
        executive.cycle();

        event.emit(Events.CycleEnd.class);
        event.synch();
        
        updateTime();
        

        resource.CYCLE_RAM_USED.stop();
        resource.CYCLE_CPU_TIME.stop();
        resource.CYCLE.stop();
    }
    
    

    /**
     * automatically called each cycle */    
    protected void updateTime() {
        timePreviousCycle = time();
        cycle++;
        timeRealNow = System.currentTimeMillis();
    }
    
    /** Processes a specific number of new tasks */
    public int processNewTasks(int maxTasks, Collection<Runnable> queue) {
        if (maxTasks == 0) return 0;
        
        int processed = 0;
                
        int numTasks = Math.min(maxTasks, newTasks.size());
        
        for (int i = 0; (!newTasks.isEmpty()) && (i < numTasks); i++) {
            
            final Task task = newTasks.removeFirst();
                        
            processed++;
            
            emotion.adjustBusy(task.getPriority(), task.getDurability());            
 
            
            if (task.isInput() || !task.sentence.isJudgment() || concept(task.sentence.term)!=null) { //it is a question/goal/quest or a concept which exists                   
                // ok so lets fire it
                queue.add(new ImmediateProcess(this, task, numTasks - 1)); 
            } else { 
                final Sentence s = task.sentence;
                if ((s!=null) && (s.isJudgment()||s.isGoal())) {
                    final double exp = s.truth.getExpectation();
                    if (exp > Parameters.DEFAULT_CREATION_EXPECTATION) {
                        //i dont see yet how frequency could play a role here - patrick
                        //just imagine a board game where you are confident about all the board rules
                        //but the implications reach all the frequency spectrum in certain situations
                        //but every concept can also be represented with (--,) so i guess its ok
                        logic.TASK_ADD_NOVEL.commit();
                        
                        // new concept formation                        
                        Task displacedNovelTask = novelTasks.putIn(task);
                        if (displacedNovelTask!=null) {
                            if (displacedNovelTask==task) {
                                removeTask(task, "Ignored");
                            }
                            else {
                                removeTask(displacedNovelTask, "Displaced novel task");
                            }
                        }
                        
                    } else {                        
                        removeTask(task, "Neglected");
                    }
                }
            }
        }        
                         
        return processed;
    }
 
    protected void error(Throwable ex) {
        emit(ERR.class, ex);
        
        if (Parameters.DEBUG) {
            ex.printStackTrace();            
        }
        
    }
    
    public <T> void run(final List<Runnable> tasks) {
        run(tasks, 1);
    }
    
    public <T> void run(final List<Runnable> tasks, int concurrency) {        
        
        if ((tasks == null) || (tasks.isEmpty())) return;
        
        else if (tasks.size() == 1) {            
            tasks.get(0).run();
        }
        else if (concurrency == 1) {
            //single threaded
            for (final Runnable t : tasks) {
                t.run();
            }
        }
        else {   
            //execute in parallel, multithreaded                        
            final ConcurrentContext ctx = ConcurrentContext.enter(); 
            
            ctx.setConcurrency(concurrency);
            try { 
                for (final Runnable r : tasks) {                    
                    ctx.execute(r);
                }
            } finally {
                // Waits for all concurrent executions to complete.
                // Re-exports any exception raised during concurrent executions. 
                ctx.exit();                              
            }
        }
    }

    

    /**
     * Select a novel task to process.
     * @return whether a task was processed
     */
    public int processNovelTasks(int num, Collection<Runnable> queue) {
        if (num == 0) return 0;
        
        int executed = 0;                

        for (int i = 0; i < novelTasks.size(); i++) {
            final Task task = novelTasks.takeNext();       // select a task from novelTasks
            if (task != null) {            
                queue.add(new ImmediateProcess(this, task, 0));
                executed++;
            }
        }
        
        
        return executed;
    }


    
     public Operator getOperator(final String op) {
        return operators.get(op);
     }
     
     public Operator addOperator(final Operator op) {
         operators.put(op.name(), op);
         return op;
     }
     
     public Operator removeOperator(final Operator op) {
         return operators.remove(op.name());
     }
     



    @Override
    public String toString() {
        //final StringBuilder sb = new StringBuilder(1024);
        //sb.append(toStringLongIfNotNull(novelTasks, "novelTasks"))
          //      .append(toStringIfNotNull(newTasks, "newTasks"));
                //.append(toStringLongIfNotNull(getCurrentTask(), "currentTask"))
                //.append(toStringLongIfNotNull(getCurrentBeliefLink(), "currentBeliefLink"))
                //.append(toStringIfNotNull(getCurrentBelief(), "currentBelief"));
        //return sb.toString();
        return super.toString();
    }

    private String toStringLongIfNotNull(Bag<?,?> item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toString();
    }

    private String toStringLongIfNotNull(Item item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toStringLong();
    }

    private String toStringIfNotNull(Object item, String title) {
        return item == null ? "" : "\n " + title + ":\n"
                + item.toString();
    }



    public long newStampSerial() {
        return currentStampSerial++;
    }


    public boolean isProcessingInput() {
        return time() >= inputPausedUntil;
    }

    
    /**
     * Queue additional cycle()'s to the inference process.
     *
     * @param cycles The number of inference steps
     */
    public void stepLater(final int cycles) {
        inputPausedUntil = (int) (time() + cycles);
    }    
    
    /** get all tasks in the system by iterating all newTasks, novelTasks, Concept TaskLinks */
    public Set<Task> getTasks(boolean includeTaskLinks, boolean includeNewTasks, boolean includeNovelTasks) {
        
        Set<Task> t = new HashSet();
        
        if (includeTaskLinks) {
            for (Concept c : concepts) {
                for (TaskLink tl : c.taskLinks) {
                    t.add(tl.targetTask);
                }
            }
        }
        
        if (includeNewTasks)
            t.addAll(newTasks);
        
        if (includeNovelTasks)
            for (Task n : novelTasks)
                t.add(n);
            
        return t;        
    }

    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability) {
        return newTask(content, sentenceType, freq, conf, priority, durability, (Task)null);
    }
            
            
    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability, final Task parentTask) {
        return newTask(content, sentenceType, freq, conf, priority, durability, parentTask, Tense.Present);
    }
    
    /** convenience method for forming a new Task from a term */
    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability, Tense tense) {
        return newTask(content, sentenceType, freq, conf, priority, durability, null, tense);
    }
    
    /** convenience method for forming a new Task from a term */
    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability, Task parentTask, Tense tense) {
        
        TruthValue truth = new TruthValue(freq, conf);
        Sentence sentence = new Sentence(
                content, 
                sentenceType, 
                truth, 
                new Stamp(this, tense));
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, truth);
        Task task = new Task(sentence, budget, parentTask);
        return task;
    }
    
    /** gets a next concept for processing */
    public Concept sampleNextConcept() {
        return concepts.sampleNextConcept();
    }
    
    public Timing getTiming() {
        return timing;
    }

    public Collection<Task> conceptQuestions(Class c) {
        if (c == Conjunction.class) {
            return questionsConjunction;
        }
        throw new RuntimeException("Questions index for " + c + " does not exist");
    }
    
    public final ArrayDeque<Task> stm = new ArrayDeque();
    //public Task stmLast = null;
    
    public boolean proceedWithTemporalInduction(final Sentence newEvent, final Sentence stmLast, Task controllerTask, NAL nal) {
        if(!controllerTask.isParticipatingInTemporalInduction()) { //todo refine, add directbool in task
            return false;
        }
       
        if (newEvent.isEternal() || !isInputOrTriggeredOperation(controllerTask, nal.memory)) {
            return false;
        }

        if (equalSubTermsInRespectToImageAndProduct(newEvent.term, stmLast.term)) {
            return false;
        }

        nal.setTheNewStamp(newEvent.stamp, stmLast.stamp, time());
        nal.setCurrentTask(controllerTask);

        Sentence previousBelief = stmLast;
        nal.setCurrentBelief(previousBelief);

        Sentence currentBelief = newEvent;

        //if(newEvent.getPriority()>Parameters.TEMPORAL_INDUCTION_MIN_PRIORITY)
        TemporalRules.temporalInduction(currentBelief, previousBelief, nal);
        return false;
    }
    
    public boolean inductionOnSucceedingEvents(final Task newEvent, NAL nal) {

        if(newEvent.budget==null || !newEvent.isParticipatingInTemporalInduction()) { //todo refine, add directbool in task
            return false;
        }

        nal.emit(Events.InduceSucceedingEvent.class, newEvent, nal);
                

        if (newEvent.sentence.isEternal() || !isInputOrTriggeredOperation(newEvent, nal.memory)) {
            return false;
        }

        if(Parameters.TEMPORAL_INDUCTION_ON_SUCCEEDING_EVENTS) {
            for (Task stmLast : stm) {
                proceedWithTemporalInduction(newEvent.sentence, stmLast.sentence, newEvent, nal);
            }
        }
        
        while (stm.size()+1 > Parameters.STM_SIZE)
            stm.removeFirst();
        stm.addLast(newEvent);

        return true;
    }

    
}
