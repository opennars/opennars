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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import nars.core.Memory.Events.ResetPost;
import nars.core.Memory.Events.ResetPre;
import nars.core.Memory.Events.WorkCycleStart;
import nars.core.Memory.Events.WorkCycleStop;
import nars.core.sense.EmotionSense;
import nars.core.sense.LogicSense;
import nars.core.sense.ResourceSense;
import nars.entity.AbstractTask;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.Item;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.inference.Executive;
import nars.inference.InferenceRecorder;
import nars.inference.TemporalRules;
import nars.io.Output;
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
import nars.language.Variable;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.operator.io.Echo;
import nars.operator.io.PauseInput;
import nars.operator.io.Reset;
import nars.operator.io.SetVolume;
import nars.storage.AbstractBag;
import nars.storage.BagObserver;


/**
 * Memory consists  the run-time state of a NAR, including:
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
public class Memory implements Output, Serializable {
    public final Executive executive;

    /** empty event classes for use with EventEmitter */
    public static class Events {

        /** fired at the beginning of each main cycle */
        public static class CycleStart { }
        
        /** fired at the end of each main cycle */
        public static class CycleStop { }

        
        /** fired at the beginning of each individual Memory work cycle */
        public static class WorkCycleStart { }
        
        /** fired at the end of each Memory individual cycle */
        public static class WorkCycleStop { }
                
        /** called before memory.reset() proceeds */
        public static class ResetPre { }
        
        /** called after memory.reset() proceeds */
        public static class ResetPost { }
        
    }
    
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
    
    private final ConceptProcessor conceptProcessor;
    
    public final EventEmitter event = new EventEmitter();
    
    

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
            return ImageExt.make(components, ((Image) compound).relationIndex, this);
        } else if (compound instanceof ImageInt) {
            return ImageInt.make(components, ((Image) compound).relationIndex, this);
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
                return SetExt.make(CompoundTerm.termList(a), this);
            case SET_INT_OPENER:
                return SetInt.make(CompoundTerm.termList(a), this);
            case INTERSECTION_EXT:
                return IntersectionExt.make(CompoundTerm.termList(a), this);
            case INTERSECTION_INT:
                return IntersectionInt.make(CompoundTerm.termList(a), this);
            case DIFFERENCE_EXT:
                return DifferenceExt.make(a, this);
            case DIFFERENCE_INT:
                return DifferenceInt.make(a, this);
            case INHERITANCE:
                return Inheritance.make(a[0], a[1], this);
            case PRODUCT:
                return Product.make(a, this);
            case IMAGE_EXT:
                return ImageExt.make(a, this);
            case IMAGE_INT:
                return ImageInt.make(a, this);
            case NEGATION:
                return Negation.make(a, this);
            case DISJUNCTION:
                return Disjunction.make(CompoundTerm.termList(a), this);
            case CONJUNCTION:
                return Conjunction.make(a, this);
            case SEQUENCE:
                return Conjunction.make(a, TemporalRules.ORDER_FORWARD, this);
            case PARALLEL:
                return Conjunction.make(a, TemporalRules.ORDER_CONCURRENT, this);
            case IMPLICATION:
                return Implication.make(a[0], a[1], this);
            case IMPLICATION_AFTER:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_FORWARD, this);
            case IMPLICATION_BEFORE:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_BACKWARD, this);
            case IMPLICATION_WHEN:
                return Implication.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT, this);
            case EQUIVALENCE:
                return Equivalence.make(a[0], a[1], this);
            case EQUIVALENCE_WHEN:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_CONCURRENT, this);
            case EQUIVALENCE_AFTER:
                return Equivalence.make(a[0], a[1], TemporalRules.ORDER_FORWARD, this);
        }
        throw new RuntimeException("Unknown Term operator: " + op + " (" + op.name() + ")");
    }
    


    
    
    /* InnateOperator registry. Containing all registered operators of the system */
    public final HashMap<CharSequence, Operator> operators;
    
    private long currentStampSerial = 0;
    private long currentTermSerial = 1;
    
    
    /**
     * New tasks with novel composed terms, for delayed and selective processing
     */
    public final AbstractBag<Task> novelTasks;
    /**
     * Inference record text to be written into a log file
     */
    private InferenceRecorder recorder;
    
 
    

    /* ---------- Short-term workspace for a single cycle ---	------- */
    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    public final ArrayDeque<Task> newTasks;
    

    public Term currentTerm;

    public Concept currentConcept;

    private Task currentTask;

    private TermLink currentBeliefLink;
    private TaskLink currentTaskLink;

    private Sentence currentBelief;

    

    private Stamp newStamp;

    public final Term self;

    
    

    
    public final EmotionSense emotion = new EmotionSense();    
    public final LogicSense logic;
    public final ResourceSense resource;
    
    
    
    private boolean working;    
    /**
     * The remaining number of steps to be carried out (stepLater mode)
     */
    private int stepsQueued;

    
    /**
     * System clock, relatively defined to guarantee the repeatability of
     * behaviors
     */
    private long clock;
    


    
    public final Param param;
    
    transient private Output output;

    /* ---------- Constructor ---------- */
    /**
     * Create a new memory
     *
     * @param initialOperators - initial set of available operators; more may be added during runtime
     */
    public Memory(Param param, ConceptProcessor cycleControl, AbstractBag<Task> novelTasks, Operator[] initialOperators) {                
        
        this.param = param;
        this.conceptProcessor = cycleControl;
        
        this.novelTasks = novelTasks;                
        this.newTasks = new ArrayDeque<>();
        this.operators = new HashMap<>();
        
        this.executive = new Executive(this);

        this.resource = new ResourceSense();
        this.logic = new LogicSense() {

            @Override
            public void sense(Memory memory) {
                double prioritySum = 0;        
                double prioritySumSq = 0;
                int count = 0;
                int totalQuestions = 0;
                int totalBeliefs = 0;
                int histogramBins = 4;
                double[] histogram = new double[histogramBins];

                for (final Concept c : getConcepts()) {
                    double p = c.getPriority();
                    totalQuestions += c.questions.size();        
                    totalBeliefs += c.beliefs.size();        
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
                //setConceptPrioritySum(totalPriority);
                setConceptPriorityMean(mean);
                setConceptPriorityVariance(variance);
                setConceptPriorityHistogram(histogram);
                
                super.sense(memory);
            }

        };
        
        recorder = NullInferenceRecorder.global;
        
        //after this line begins actual inference, now that the essential data strucures are allocated
        //------------------------------------ 
                
        
        // create self
        self = conceptualize(new Term(Symbols.SELF)).term;

        for (Operator o : initialOperators)
            addOperator(o);
                
        reset();

    }

    public void reset() {
        event.emit(ResetPre.class);
        
        conceptProcessor.clear();
        novelTasks.clear();
        newTasks.clear();     
        
        executive.reset();
        
        clock = 0;
        stepsQueued = 0;
        working = true;
        
        emotion.set(0.5f, 0.5f);
        
        event.emit(ResetPost.class);
       
        if (getRecorder().isActive()) {
            getRecorder().append("Reset");
        }
    }

    public InferenceRecorder getRecorder() {
        return recorder;
    }

    public void setRecorder(InferenceRecorder recorder) {
        this.recorder = recorder;
    }

    public long getTime() {
        return clock;
    }

     /* ---------- operator processing ---------- */
     public boolean isOperatorRegistered(String op) {
         return operators.containsKey(op);
     }
 
    /**
     * Actually means that there are no new Tasks
     *
     * @return Whether the newTasks list is empty
     */
    public int getNewTaskCount() {
        return newTasks.size();
    }

    /* ---------- conversion utilities ---------- */
    /**
     * Get an existing Concept for a given name
     * <p>
     * called from Term and ConceptWindow.
     *
     * @param name the name of a concept
     * @return a Concept or null
     */
    public Concept concept(final CharSequence name) {
        return conceptProcessor.concept(name);
    }

    /**
     * Get a Term for a given name of a Concept or InnateOperator
     * <p>
     * called in StringParser and the make____() methods of compound terms.
     *
     * @param name the name of a concept or operator
     * @return a Term or null (if no Concept/InnateOperator has this name)
     */
    public Term conceptTerm(final CharSequence name) {
        final Concept concept = concept(name);
        if (concept != null) {
            return concept.term;
        }
        return null;
    }

    /**
     * Get an existing Concept for a given Term.
     *
     * @param term The Term naming a concept
     * @return a Concept or null
     */
    public Concept concept(final Term term) {
        return concept(term.name());
    }

    /**
     * Get the Concept associated to a Term, or create it.
     *
     * @param term indicating the concept
     * @return an existing Concept, or a new one, or null ( TODO bad smell )
     */
    public Concept conceptualize(final Term term) {
        if (!term.isConstant()) {
            return null;
        }
        final CharSequence n = term.name();
        Concept concept = concept(n);
        if (concept == null) {
            // The only part of NARS that instantiates new Concepts
            Concept newConcept = conceptProcessor.addConcept(term, this);
            if (newConcept == null) {
                return null;
            } else {
                logic.CONCEPT_NEW.commit(term.getComplexity());
                        
                if (recorder.isActive()) {
                    recorder.onConceptNew(concept);
                }
                return newConcept;
            }
        }
        return concept;
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

    /* ---------- adjustment functions ---------- */
    /**
     * Adjust the activation level of a Concept
     * <p>
     * called in Concept.insertTaskLink only
     *
     * @param c the concept to be adjusted
     * @param b the new BudgetValue
     */
    public void conceptActivate(final Concept c, final BudgetValue b) {
        conceptProcessor.conceptActivate(c, b);
    }

    /* ---------- new task entries ---------- */
    /**
     * add new task that waits to be processed in the next cycleMemory
     */
    protected void addNewTask(final Task t, final String reason) {
        logic.TASK_ADD_NEW.commit(t.getPriority());
                
        newTasks.add(t);
        
        if (recorder.isActive()) {
            recorder.onTaskAdd(t, reason);
        }
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
     * @param task The addInput task
     */
    public void inputTask(final AbstractTask t) {                                                 
        if (t instanceof Task) {                                       
            Task task = (Task)t;

            output(IN.class, task);

            if (task.budget.aboveThreshold()) {
                addNewTask(task, "Perceived");
            } else {
                if (recorder.isActive()) {
                    recorder.onTaskRemove(task, "Neglected");
                }
            }
        }
        else if (t instanceof PauseInput) {            
            stepLater(((PauseInput)t).cycles);            
            output(IN.class, t);
        }
        else if (t instanceof Reset) {
            reset();
            output(IN.class, t);
        }
        else if (t instanceof Echo) {
            Echo e = (Echo)t;
            output(e.channel, e.signal);
        }
        else if (t instanceof SetVolume) {            
            param.noiseLevel.set(((SetVolume)t).volume);
            output(IN.class, t);
        }            
        else {
            output(IN.class, "Unrecognized Input Task: " + t);
        }
    }

    /**
     * Activated task called in MatchingRules.trySolution and
     * Concept.processGoal
     *
     * @param budget The budget value of the new Task
     * @param sentence The content of the new Task
     * @param candidateBelief The belief to be used in future inference, for
     * forward/backward correspondence
     */
    public void activatedTask(final BudgetValue budget, final Sentence sentence, final Sentence candidateBelief) {
        final Task task = new Task(sentence, budget, getCurrentTask(), sentence, candidateBelief);

        if (sentence.isQuestion()) {
            output(task);
        }

        addNewTask(task, "Activated");
    }
    
    /**
     * ExecutedTask called in Operator.call
     *
     * @param operation The operation just executed
     */
    public void executedTask(final Operation operation) {
        logic.TASK_EXECUTED.commit(currentTask.budget.getPriority());
        
        TruthValue truth = new TruthValue(1f,0.9999f);
        Stamp stamp = new Stamp(this, Tense.Present); 
        Sentence sentence = new Sentence(operation, Symbols.JUDGMENT_MARK, truth, stamp);
        
        Task task = new Task(sentence, currentTask.budget, operation.getTask());
        task.setCause(operation);
        
        addNewTask(task, "Executed");
    }

    public void output(final Task t) {
        if (output == null) return;
        
        final float budget = t.budget.summary();
        final float noiseLevel = 1.0f - (param.noiseLevel.get() / 100.0f);
        
        if (budget >= noiseLevel) {  // only report significant derived Tasks
            output(OUT.class, t);
        }        
    }
    
    @Override
    public void output(final Class c, final Object signal) {
        if (output!=null)
            output.output(c, signal);
    }

    /**
     * Derived task comes from the inference rules.
     *
     * @param task the derived task
     */
    public void derivedTask(final Task task, final boolean revised, final boolean single, Sentence occurence, Sentence occurence2) {
        

        if (task.budget.aboveThreshold()) {
        
            if (task.sentence != null && task.sentence.truth != null) {
                  float conf = task.sentence.truth.getConfidence();                
                  if (conf == 0) { 
                      //no confidence - we can delete the wrongs out that way.
                      if (recorder.isActive())
                          recorder.onTaskRemove(task, "Ignored (zero confidence)");
                      return;
                  }
            }

            
            final Stamp stamp = task.sentence.stamp;
            if(occurence!=null && occurence.getOccurenceTime()!=Stamp.ETERNAL) {
                stamp.setOccurrenceTime(occurence.getOccurenceTime());
            }
            if(occurence2!=null && occurence2.getOccurenceTime()!=Stamp.ETERNAL) {
                stamp.setOccurrenceTime(occurence2.getOccurenceTime());
            }
            if (stamp.latency > 0) {
                logic.DERIVATION_LATENCY.commit(stamp.latency);
            }
            
            final ArrayList<Term> chain = stamp.getChain();

            final Term currentTaskContent = getCurrentTask().getContent();

            if (getCurrentBelief() != null && getCurrentBelief().isJudgment()) {
                final Term currentBeliefContent = getCurrentBelief().content;
                if(chain.contains(currentBeliefContent)) {
                //if(stamp.chainContainsInstance(currentBeliefContent)) {
                    chain.remove(currentBeliefContent);
                }
                stamp.addToChain(currentBeliefContent);
            }


            //workaround for single premise task issue:
            if(currentBelief == null && single && currentTask != null && currentTask.sentence.isJudgment()) {
                if(chain.contains(currentTaskContent)) {
                //if(stamp.chainContainsInstance(currentTaskContent)) {
                    chain.remove(currentTaskContent);
                }
                stamp.addToChain(currentTaskContent);
            }
            //end workaround

            if (currentTask != null && !single && currentTask.sentence.isJudgment()) {
                if(chain.contains(currentTaskContent)) {                
                //if(stamp.chainContainsInstance(currentTaskContent)) {                    
                    chain.remove(currentTaskContent);
                }
                stamp.addToChain(currentTaskContent);
            }


            //its a inference rule, so we have to do the derivation chain check to hamper cycles
            if (!revised) { 

                int i = 0;
                for (Term chain1 : chain) {
                    if (task.sentence.isJudgment() && task.getContent().equals(chain1)) {
                        if(task.getParentTask()==null || 
                           (!(task.getParentTask().getContent().equals(Negation.make(task.getContent(), this))) &&
                           !(task.getContent().equals(Negation.make(task.getParentTask().getContent(), this))))) {
                        if (recorder.isActive()) {
                            recorder.onTaskRemove(task, "Cyclic Reasoning (index " + i + ")");
                        }
                        return;
                        }
                    }
                    i++;                    
                }
            } else { //its revision, of course its cyclic, apply evidental base policy
                final int stampLength = stamp.baseLength;
                for (int i = 0; i < stampLength; i++) {
                    final long baseI = stamp.evidentialBase[i];

                    for (int j = 0; j < stampLength; j++) {     
                        if ((i != j) && (baseI == stamp.evidentialBase[j]) && !(task.sentence.punctuation==Symbols.GOAL_MARK && task.sentence.content instanceof Operation)) {
                            if (recorder.isActive()) {                                
                                recorder.onTaskRemove(task, "Overlapping Evidence on Revision: " + i + "," + j + " in " + stamp.toString());
                            }
                            return;
                        }
                    }
                }
            }

            //is it complex and also important? then give it a name:
            if (!(task.sentence.content instanceof Operation) && 
                 (param.internalExperience.get()) && 
                 (task.sentence.content.getComplexity() > param.abbreviationMinComplexity.get()) &&
                 (task.budget.getQuality() > param.abbreviationMinQuality.get())) {

                Term opTerm = this.getOperator("^abbreviate");
                Term[] arg = new Term[1];
                arg[0]=task.sentence.content;
                Term argTerm = Product.make(arg,this);
                
                Term operation = Inheritance.make(argTerm, opTerm,this);
                TruthValue truth = new TruthValue(1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
                Stamp stampi = (Stamp) task.sentence.stamp.clone();
                stamp.setOccurrenceTime(this.getTime());
                
                Sentence j = new Sentence(operation,Symbols.GOAL_MARK, truth, stampi);
                BudgetValue budg=new BudgetValue(Parameters.DEFAULT_GOAL_PRIORITY, Parameters.DEFAULT_GOAL_DURABILITY, 1);
                Task newTask = new Task(j, budg,Parameters.INTERNAL_EXPERIENCE_FULL ? null : task);
                if (getRecorder().isActive()) {
                    getRecorder().append("Named: " + j.toString());
                }
                output(newTask);
                addNewTask(newTask, "Derived (abbreviated)");
            }

            if(param.experimentalNarsPlus.get() && task.sentence.punctuation==Symbols.JUDGMENT_MARK) { 
                //lets say we have <{...} --> M>.
                if(task.sentence.content instanceof Inheritance) {
                    Inheritance inh=(Inheritance) task.sentence.content;
                    if(inh.getSubject() instanceof SetExt) {
                        SetExt set_term=(SetExt) inh.getSubject();
                        Integer cardinality=set_term.size();   //this gets the cardinality of M
                        //now create term <(*,M,cardinality) --> CARDINALITY>.

                        Term[] product_args = new Term[] { 
                            inh.getPredicate(),
                            new Term(cardinality.toString()) 
                        };

                        Term new_subject=Product.make(product_args, this);
                        Term new_predicate=new Term("CARDINALITY"); //TODO this can be a static final instance shared by all
                        Term new_term=Inheritance.make(new_subject, new_predicate, this);

                        TruthValue truth = (TruthValue) task.sentence.truth.clone();
                        Stamp stampi = (Stamp) task.sentence.stamp.clone();
                        Sentence j = new Sentence(new_term, Symbols.JUDGMENT_MARK, truth, stampi);
                        BudgetValue budg=(BudgetValue) task.budget.clone();
                        Task newTask = new Task(j, budg,task);
                        if (getRecorder().isActive()) {
                            this.recorder.append("Counted: " + j.toString());
                        }
                        output(newTask);
                        addNewTask(newTask, "Derived (cardinality)");
                    }
                }
            }

            if(task.sentence.content instanceof Operation) {
                Operation op=(Operation) task.sentence.content;
                if(op.getSubject() instanceof Variable || op.getPredicate() instanceof Variable) {
                    return;
                }
            }

            logic.TASK_DERIVED.commit(task.budget.getPriority());

            output(task);

            addNewTask(task, "Derived");
        }
        else {            
            if (recorder.isActive())
                recorder.onTaskRemove(task, "Ignored (insufficient budget)");
        }
            
    }

    /* --------------- new task building --------------- */
    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void doublePremiseTaskRevised(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget) {
        Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
        Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
        derivedTask(newTask, true, false, null, null);
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void doublePremiseTask(final Term newContent, final TruthValue newTruth, final BudgetValue newBudget) {
        if (newContent != null) {
            final Sentence newSentence = new Sentence(newContent, getCurrentTask().sentence.punctuation, newTruth, getTheNewStamp());
            final Task newTask = new Task(newSentence, newBudget, getCurrentTask(), getCurrentBelief());
            derivedTask(newTask, false, false, null, null);
        }
    }

    /**
     * Shared final operations by all double-premise rules, called from the
     * rules except StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     * @param revisible Whether the sentence is revisible
     */
//    public void doublePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget, boolean revisible) {
//        if (newContent != null) {
//            Sentence taskSentence = currentTask.getSentence();
//            Sentence newSentence = new Sentence(newContent, taskSentence.getPunctuation(), newTruth, newStamp, revisible);
//            Task newTask = new Task(newSentence, newBudget, currentTask, currentBelief);
//            derivedTask(newTask, false, false);
//        }
//    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void singlePremiseTask(Term newContent, TruthValue newTruth, BudgetValue newBudget) {
        singlePremiseTask(newContent, getCurrentTask().sentence.punctuation, newTruth, newBudget);
    }

    /**
     * Shared final operations by all single-premise rules, called in
     * StructuralRules
     *
     * @param newContent The content of the sentence in task
     * @param punctuation The punctuation of the sentence in task
     * @param newTruth The truth value of the sentence in task
     * @param newBudget The budget value in task
     */
    public void singlePremiseTask(final Term newContent, final char punctuation, final TruthValue newTruth, final BudgetValue newBudget) {
        Task parentTask = getCurrentTask().getParentTask();
                
        if (parentTask != null) {
            if (parentTask.getContent() == null)
                return;     
            if(newContent==null) {
                return;
            }
            if (newContent.equals(parentTask.getContent())) // circular structural inference
                return;            
        }
        Sentence taskSentence = getCurrentTask().sentence;
        if (taskSentence.isJudgment() || getCurrentBelief() == null) {
            setTheNewStamp(new Stamp(taskSentence.stamp, getTime()));
        } else {    // to answer a question with negation in NAL-5 --- move to activated task?
            setTheNewStamp(new Stamp(getCurrentBelief().stamp, getTime()));
        }
        
        Sentence newSentence = new Sentence(newContent, punctuation, newTruth, getTheNewStamp());
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        derivedTask(newTask, false, true, null, null);
    }

    public void singlePremiseTask(Sentence newSentence, BudgetValue newBudget) {
        Task newTask = new Task(newSentence, newBudget, getCurrentTask());
        derivedTask(newTask, false, true, null, null);
    }

    
    public void cycle(final TaskSource taskSource) {
        
        resource.CYCLE.start();
        resource.CYCLE_CPU_TIME.start();
        resource.CYCLE_RAM_USED.start();

        int inputCycles = param.cycleInputTasks.get();
        int memCycles = param.cycleMemory.get();

        event.emit(Memory.Events.CycleStart.class);

        //IO cycle
        resource.IO_CYCLE.start();

        if (logic.IO_INPUTS_BUFFERED.isActive())
            logic.IO_INPUTS_BUFFERED.commit(taskSource.getInputItemsBuffered());
        
        if (getCyclesQueued()==0) {                
            final int duration = param.duration.get();
            for (int i = 0; i < inputCycles; i++) {
                AbstractTask t = taskSource.nextTask();
                if (t instanceof Task) {
                    ((Task)t).sentence.stamp.setCreationTime(getTime(), duration);
                }
                if (t!=null)
                    inputTask(t);
            }
        }
        resource.IO_CYCLE.stop();

        
        
        //Memory working 
        resource.MEMORY_CYCLE.start();
        if (working) {            
            for (int i = 0; i < memCycles; i++) {
                cycleWork();                
            }            
        }
        resource.MEMORY_CYCLE.stop();
        

        event.emit(Memory.Events.CycleStop.class);
        
        resource.CYCLE_RAM_USED.stop();
        resource.CYCLE_CPU_TIME.stop();
        resource.CYCLE.stop();
    }
    
    
    /* ---------- system working cycleMemory ---------- */
    /**
     * An atomic working cycle of the system: process new Tasks, then fire a
     * concept
     * <p>
     * Called from Reasoner.tick only
     *
     * @param clock The current time to be displayed
     */
    public void cycleWork() {
        
        event.emit(WorkCycleStart.class);
        
        boolean recorderActive = recorder.isActive();
        if (recorderActive)
            recorder.onCycleStart(clock);

        //--------m-a-i-n-----l-o-o-p--------
            
        conceptProcessor.cycle(this);

        executive.manageExecution();
            
        //--------m-a-i-n-----l-o-o-p--------

        if (recorderActive)
            recorder.onCycleEnd(clock);

        if (stepsQueued > 0)
            stepsQueued--;         
        
        clock++;
                
        event.emit(WorkCycleStop.class);      
        
    }

    
    
    
    /**
     * Process the newTasks accumulated in the previous cycleMemory, accept
 addInput ones and those that corresponding to existing concepts, plus one
 from the buffer. 
        @return number of tasks processed
     */
    public int processNewTasks() {        
        return processNewTasks(newTasks.size());
    }
    
    /** Processes a specific number of new tasks */
    public int processNewTasks(int maxTasks) {        
        
        int processed = 0;
        // don't include new tasks produced in the current cycleMemory
        int counter = Math.min(maxTasks, newTasks.size());
        
        Task newEvent = null;
        while (counter-- > 0) {
            
            final Task task = newTasks.removeFirst();
            processed++;
            
            emotion.adjustBusy(task.getPriority(), task.getDurability());            
            
            if (task.isInput()  || concept(task.getContent())!=null || (task!=null && task.getContent()!=null && task.sentence!=null && 
                    task.getContent() instanceof Operation && task.sentence.isGoal() && conceptualize(task.getContent()) != null)) {
                
                // new addInput or existing concept
                immediateProcess(task);
                
                if (executive.isActionable(task, newEvent))
                    newEvent = task;
                
                
            } else {
                final Sentence s = task.sentence;
                if ((s!=null) && (s.isJudgment())) {
                    final double exp = s.truth.getExpectation();
                    if (exp > Parameters.DEFAULT_CREATION_EXPECTATION) {
                        
                        logic.TASK_ADD_NOVEL.commit();
                        
                        // new concept formation                        
                        novelTasks.putIn(task);
                        
                    } else {
                        
                        if (recorder.isActive()) {
                            recorder.onTaskRemove(task, "Neglected");
                        }
                        
                    }
                }
            }
        }
        
        boolean stmUpdated = executive.planShortTerm(newEvent);
        if (stmUpdated)
            logic.SHORT_TERM_MEMORY_UPDATE.commit();
                
        return processed;
    }
    
    /**
     * Select a novel task to process.
     * @return whether a task was processed
     */
    public boolean processNovelTask() {
        final Task task = novelTasks.takeOut();       // select a task from novelTasks
        if (task != null) {
            immediateProcess(task);
            return true;
        }
        return false;
    }


    /* ---------- task processing ---------- */
    /**
     * Immediate processing of a new task, in constant time Local processing, in
     * one concept only
     *
     * @param task the task to be accepted
     */
    private void immediateProcess(final Task task) {
        logic.TASK_IMMEDIATE_PROCESS.commit();

        setCurrentTask(task); // one of the two places where this variable is set
        
        if (recorder.isActive()) {
            recorder.append("Task Immediately Processed: " + task);
        }
        
        setCurrentTerm(task.getContent());
        currentConcept = conceptualize(getCurrentTerm());
        
        if (getCurrentConcept() != null) {
            conceptActivate(getCurrentConcept(), task.budget);
            getCurrentConcept().directProcess(task);
        }
        
    }

    
     public Operator getOperator(String op) {
        return operators.get(op);
     }
     
     public void addOperator(Operator op) {
         operators.put(op.name(), op);
     }
     
 
//    /* ---------- display ---------- */
//    /**
//     * Start display active concepts on given bagObserver, called from
//     * MainWindow.
//     *
//     * we don't want to expose fields concepts and novelTasks, AND we want to
//     * separate GUI and inference, so this method takes as argument a
//     * {@link BagObserver} and calls
//     * {@link ConceptBag#addBagObserver(BagObserver, String)} ;
//     *
//     * see design for {@link Bag} and {@link nars.gui.BagWindow} in
//     * {@link Bag#addBagObserver(BagObserver, String)}
//     *
//     * @param bagObserver bag Observer that will receive notifications
//     * @param title the window title
//     */
//    public void conceptsStartPlay(final BagObserver<Concept> bagObserver, final String title) {
//        bagObserver.setBag(concepts);
//        concepts.addBagObserver(bagObserver, title);
//    }

    /**
     * Display new tasks, called from MainWindow. see
     * {@link #conceptsStartPlay(BagObserver, String)}
     *
     * @param bagObserver
     * @param s the window title
     */
    public void taskBuffersStartPlay(final BagObserver<Task> bagObserver, final String s) {
        bagObserver.setBag(novelTasks);
        //novelTasks.addBagObserver(bagObserver, s);
    }



    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(1024);
        sb.append(getConcepts().toString())
                .append(toStringLongIfNotNull(novelTasks, "novelTasks"))
                .append(toStringIfNotNull(newTasks, "newTasks"))
                .append(toStringLongIfNotNull(getCurrentTask(), "currentTask"))
                .append(toStringLongIfNotNull(getCurrentBeliefLink(), "currentBeliefLink"))
                .append(toStringIfNotNull(getCurrentBelief(), "currentBelief"));
        return sb.toString();
    }

    private String toStringLongIfNotNull(AbstractBag<?> item, String title) {
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



    /** returns a collection of all concepts */
    public Collection<? extends Concept> getConcepts() {
        return conceptProcessor.getConcepts();
    }

    /**
     * @return the currentTask
     */
    public Task getCurrentTask() {
        return currentTask;
    }

    /**
     * @param currentTask the currentTask to set
     */
    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    /**
     * @return the newStamp
     */
    public Stamp getTheNewStamp() {
        return newStamp;
    }

    /**
     * @param newStamp the newStamp to set
     */
    public void setTheNewStamp(Stamp newStamp) {
        this.newStamp = newStamp;
    }

    /**
     * @return the currentBelief
     */
    public Sentence getCurrentBelief() {
        return currentBelief;
    }

    /**
     * @param currentBelief the currentBelief to set
     */
    public void setCurrentBelief(Sentence currentBelief) {
        this.currentBelief = currentBelief;
    }

    /**
     * @return the currentBeliefLink
     */
    public TermLink getCurrentBeliefLink() {
        return currentBeliefLink;
    }

    /**
     * @param currentBeliefLink the currentBeliefLink to set
     */
    public void setCurrentBeliefLink(TermLink currentBeliefLink) {
        this.currentBeliefLink = currentBeliefLink;
    }

    /**
     * @return the currentTaskLink
     */
    public TaskLink getCurrentTaskLink() {
        return currentTaskLink;
    }

    /**
     * @param currentTaskLink the currentTaskLink to set
     */
    public void setCurrentTaskLink(TaskLink currentTaskLink) {
        this.currentTaskLink = currentTaskLink;
    }

    /**
     * @return the currentTerm
     */
    public Term getCurrentTerm() {
        return currentTerm;
    }

    /**
     * @param currentTerm the currentTerm to set
     */
    public void setCurrentTerm(Term currentTerm) {
        this.currentTerm = currentTerm;
    }

    /**
     * @return the currentConcept
     */
    public Concept getCurrentConcept() {
        return currentConcept;
    }

    public long newStampSerial() {
        return currentStampSerial++;
    }
    public Term newSerialTerm(char prefix) {
        return new Term(prefix + String.valueOf(currentTermSerial++));
    }

    /**
     * sets the current output destination for Memory's emitted signals     
     */
    public void setOutput(Output o) {
        output = o;
    }

    public int getCyclesQueued() {
        return stepsQueued;
    }



    public static final class NullInferenceRecorder implements InferenceRecorder {

        public static final NullInferenceRecorder global = new NullInferenceRecorder();

        
        private NullInferenceRecorder() {        }
        
        @Override public boolean isActive() { return false;  }

        @Override public void append(String s) {        }

        @Override public void onCycleStart(long clock) {        }
        @Override public void onCycleEnd(long clock) {        }                
        @Override public void onConceptNew(Concept concept) {        }
        @Override public void onTaskAdd(Task task, String reason) {        }        
        @Override public void onTaskRemove(Task task, String reason) {        }        
               
    }
    

    public boolean isWorking() {
        return working;
    }
        
    /** Can be used to pause/resume inference, without killing the running thread. */
    public void setWorking(boolean b) {
        this.working = b;
    }
    
    /**
     * Queue additional cycle()'s to the inference process.
     *
     * @param cycles The number of inference steps
     */
    public void stepLater(final int cycles) {
        stepsQueued += cycles;
    }    

    /** convenience method for forming a new Task from a term */
    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability) {
        return newTask(content, sentenceType, freq, conf, priority, durability, null);
    }
    
    /** convenience method for forming a new Task from a term */
    public Task newTask(Term content, char sentenceType, float freq, float conf, float priority, float durability, Task parentTask) {
        
        TruthValue truth = new TruthValue(freq, conf);
        Sentence sentence = new Sentence(
                content, 
                sentenceType, 
                truth, 
                new Stamp(this));
        BudgetValue budget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(truth));
        Task task = new Task(sentence, budget, parentTask);
        return task;
    }
    
    /** gets a next concept for processing */
    public Concept sampleNextConcept() {
        return conceptProcessor.sampleNextConcept();
    }
    
    /**
     * To rememberAction an internal action as an operation
     * <p>
     * called from Concept
     * @param task The task processed
     */
    public void rememberAction(final Task task) {
        Term content = task.getContent();
        if (content instanceof Operation) {
            return;     // to prevent infinite recursions
        }
        Sentence sentence = task.sentence;
        TruthValue truth = new TruthValue(1.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
        Stamp stamp = (Stamp) task.sentence.stamp.clone();
        stamp.setOccurrenceTime(getTime());
        
        Sentence j = new Sentence(sentence.toTerm(this), Symbols.JUDGMENT_MARK, truth, stamp);
        BudgetValue newbudget=new BudgetValue(
                task.budget.getPriority()*Parameters.INTERNAL_EXPERIENCE_PRIORITY_MUL,
                task.budget.getDurability()*Parameters.INTERNAL_EXPERIENCE_DURABILITY_MUL, 
                task.budget.getQuality()*Parameters.INTERNAL_EXPERIENCE_QUALITY_MUL);
        Task newTask = new Task(j, (BudgetValue) newbudget,Parameters.INTERNAL_EXPERIENCE_FULL ? null : task);
        if (getRecorder().isActive()) {
            recorder.append("Remembered: " + j.toString());
        }
        newTasks.add(newTask);
    }

//    /**
//     * Updates the LogicState measurements and returns the data     
//     */
//    public LogicSense updateLogicSense() {
//        logic.update(this);
//        return logic;
//    }
//    
//    public MultiSense updateSenses() {
//        
//        updateLogicSense();
//        
//        resource.update(this);
//        
//        return new MultiSense(logic, resource);
//    }
//
        
    
}
