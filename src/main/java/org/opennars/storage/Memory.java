/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.opennars.storage;

import org.opennars.control.ConceptProcessing;
import org.opennars.io.events.Events;
import org.opennars.io.events.EventEmitter;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.opennars.main.NAR;
import org.opennars.main.NAR.RuntimeParameters;
import org.opennars.main.Parameters;
import org.opennars.io.events.Events.ResetEnd;
import org.opennars.io.events.Events.ResetStart;
import org.opennars.io.events.Events.TaskRemove;
import org.opennars.control.DerivationContext;
import org.opennars.control.GeneralInferenceControl;
import org.opennars.control.TemporalInferenceControl;
import org.opennars.plugin.mental.Emotions;
import org.opennars.entity.BudgetValue;
import org.opennars.entity.Concept;
import org.opennars.entity.Sentence;
import org.opennars.entity.Stamp;
import org.opennars.entity.Task;
import org.opennars.entity.TruthValue;
import org.opennars.inference.BudgetFunctions;
import static org.opennars.inference.BudgetFunctions.truthToQuality;
import org.opennars.io.events.OutputHandler.IN;
import org.opennars.io.events.OutputHandler.OUT;
import org.opennars.io.Symbols;
import org.opennars.language.Tense;
import org.opennars.language.Term;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Interval;
import org.opennars.main.NAR.PortableDouble;


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
public class Memory implements Serializable, Iterable<Concept> {
    
    //emotion meter keeping track of global emotion
    public final Emotions emotion = new Emotions();   
    public long decisionBlock = 0;
    public Task lastDecision = null;
    public boolean allowExecution = true;

    public static long randomSeed = 1;
    public static Random randomNumber = new Random(randomSeed);
    public static void resetStatic() {
        randomNumber.setSeed(randomSeed);    
    }
    
    //todo make sense of this class and de-obfuscate
    public final Bag<Concept,Term> concepts;
    public transient EventEmitter event;
    
    /* InnateOperator registry. Containing all registered operators of the system */
    public final HashMap<CharSequence, Operator> operators;
    
    /* New tasks with novel composed terms, for delayed and selective processing*/
    public final Bag<Task<Term>,Sentence<Term>> novelTasks;
    
    /* Input event tasks that were either input events or derived sequences*/
    public Bag<Task<Term>,Sentence<Term>> seq_current;
    public Bag<Task<Term>,Sentence<Term>> recent_operations;

    /* List of new tasks accumulated in one cycle, to be processed in the next cycle */
    public final Deque<Task> newTasks;
    
    /* System clock, relatively defined to guarantee the repeatability of behaviors */
    private long cycle;
    
    /* System parameters that can be changed at runtime */
    public final RuntimeParameters param;
    
    /* ---------- Constructor ---------- */
    /**
     * Create a new memory
     *
     * @param initialOperators - initial set of available operators; more may be added during runtime
     */
    public Memory(RuntimeParameters param, Bag<Concept,Term> concepts, Bag<Task<Term>,Sentence<Term>> novelTasks,
            Bag<Task<Term>,Sentence<Term>> seq_current,
            Bag<Task<Term>,Sentence<Term>> recent_operations) {                

        this.param = param;
        this.event = new EventEmitter();
        this.concepts = concepts;
        this.novelTasks = novelTasks;                
        this.newTasks = new ArrayDeque<>();
        this.recent_operations = recent_operations;
        this.seq_current = seq_current;
        this.operators = new HashMap<>();
        reset();
    }
    
    public void reset() {
        event.emit(ResetStart.class);
        decisionBlock = 0;
        concepts.clear();
        novelTasks.clear();
        newTasks.clear();    
        this.seq_current.clear();
        cycle = 0;
        emotion.resetEmotions();
        this.lastDecision = null;
        resetStatic();
        event.emit(ResetEnd.class);
    }

    public long time() {
        return cycle;
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
        return concepts.get(CompoundTerm.replaceIntervals(t));
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
    public Concept conceptualize(final BudgetValue budget, Term term) {   
        if(term instanceof Interval) {
            return null;
        }
        term = CompoundTerm.replaceIntervals(term);
        //see if concept is active
        Concept concept = concepts.take(term);
        if (concept == null) {                            
            //create new concept, with the applied budget
            concept = new Concept(budget, term, this);
            //if (memory.logic!=null)
            //    memory.logic.CONCEPT_NEW.commit(term.getComplexity());
            emit(Events.ConceptNew.class, concept);                
        }
        else if (concept!=null) {            
            //apply budget to existing concept
            //memory.logic.CONCEPT_ACTIVATE.commit(term.getComplexity());
            BudgetFunctions.activate(concept.budget, budget, BudgetFunctions.Activating.TaskLink);            
        }
        else {
            //unable to create, ex: has variables
            return null;
        }
        Concept displaced = concepts.putBack(concept, cycles(param.conceptForgetDurations), this);   
        if (displaced == null) {
            //added without replacing anything
            return concept;
        }        
        else if (displaced == concept) {
            //not able to insert
            conceptRemoved(displaced);
            return null;
        }        
        else {
            conceptRemoved(displaced);
            return concept;
        }
    }
    
    /* ---------- new task entries ---------- */
    /**
     * add new task that waits to be processed in the next cycleMemory
     */
    public void addNewTask(final Task t, final String reason) {
        newTasks.add(t);
      //  logic.TASK_ADD_NEW.commit(t.getPriority());
        emit(Events.TaskAdd.class, t, reason);
        output(t);
    }
    
    boolean checked=false;
    boolean isjUnit=false;
    public static boolean isJUnitTest() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }           
        }
        return false;
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
    public void inputTask(final Task t, boolean emitIn) {
        if(!checked) {
            checked=true;
            isjUnit=isJUnitTest();
        }
        if (t instanceof Task) {
            Task task = (Task)t;
            Stamp s = task.sentence.stamp;                        
            if (s.getCreationTime()==-1)
                s.setCreationTime(time(), Parameters.DURATION);

            if(emitIn) {
                emit(IN.class, task);
            }

            if (task.budget.aboveThreshold()) {
                
                addNewTask(task, "Perceived");
                
            } else {
                removeTask(task, "Neglected");
            }
        }
    }
    
    public void inputTask(final Task t) {
        inputTask(t, true);
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
       // logic.TASK_EXECUTED.commit(opTask.budget.getPriority());
                
        Stamp stamp = new Stamp(this, Tense.Present); 
        Sentence sentence = new Sentence(
            operation,
            Symbols.JUDGMENT_MARK,
            truth,
            stamp);
        
        Task task = new Task(sentence, 
                             new BudgetValue(Parameters.DEFAULT_FEEDBACK_PRIORITY, 
                                             Parameters.DEFAULT_FEEDBACK_DURABILITY,
                                             truthToQuality(sentence.getTruth())),
                             true);
        task.setElemOfSequenceBuffer(true);
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
    
    public void conceptRemoved(Concept c) {
        emit(Events.ConceptForget.class, c);
    }
    
    public void cycle(final NAR inputs) {
    
        event.emit(Events.CycleStart.class);
        
        this.processNewTasks();
    //if(noResult()) //newTasks empty
        this.processNovelTask();
    //if(noResult()) //newTasks empty
        GeneralInferenceControl.selectConceptForInference(this);
        
        event.emit(Events.CycleEnd.class);
        event.synch();
        
        cycle++;
    }
    
    public void localInference(Task task) {
        DerivationContext cont = new DerivationContext(this);
        cont.setCurrentTask(task);
        cont.setCurrentTerm(task.getTerm());
        cont.setCurrentConcept(conceptualize(task.budget, cont.getCurrentTerm()));
        if (cont.getCurrentConcept() != null) {
            boolean processed = ConceptProcessing.processTask(cont.getCurrentConcept(), cont, task);
            if (processed) {
                event.emit(Events.ConceptDirectProcessedTask.class, task);
            }
        }
        
        if (!task.sentence.isEternal() && !(task.sentence.term instanceof Operation)) {
            TemporalInferenceControl.eventInference(task, cont);
        }
        
        //memory.logic.TASK_IMMEDIATE_PROCESS.commit();
        emit(Events.TaskImmediateProcess.class, task, cont);
    }
    
    /**
     * Process the newTasks accumulated in the previous workCycle, accept input
     * ones and those that corresponding to existing concepts, plus one from the
     * buffer.
     */
    public void processNewTasks() {
        Task task;
        int counter = newTasks.size();  // don't include new tasks produced in the current workCycle
        while (counter-- > 0) {
            task = newTasks.removeFirst();
            boolean enterDirect = true;
            if (/*task.isElemOfSequenceBuffer() || task.isObservablePrediction() || */ enterDirect ||  task.isInput() || task.sentence.isQuest() || task.sentence.isQuestion() || concept(task.sentence.term)!=null) { // new input or existing concept
                localInference(task);
            } else {
                Sentence s = task.sentence;
                if (s.isJudgment() || s.isGoal()) {
                    double d = s.getTruth().getExpectation();
                    if (s.isJudgment() && d > Parameters.DEFAULT_CREATION_EXPECTATION) {
                        novelTasks.putIn(task);    // new concept formation
                    } else 
                    if(s.isGoal() && d > Parameters.DEFAULT_CREATION_EXPECTATION_GOAL) {
                        novelTasks.putIn(task);    // new concept formation
                    }
                    else
                    {
                        removeTask(task, "Neglected");
                    }
                }
            }
        }
    }
    

    /**
     * Select a novel task to process.
     * @return whether a task was processed
     */
    public void processNovelTask() {
        final Task task = novelTasks.takeNext();
        if (task != null) {            
            localInference(task);
        }
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

    private long currentStampSerial = 0;
    public long newStampSerial() {
        return currentStampSerial++;
    }   

    /** converts durations to cycles */
    public final float cycles(PortableDouble durations) {
        return Parameters.DURATION * durations.floatValue();
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }
}
