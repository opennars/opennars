/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.storage;
 
import org.opennars.control.concept.ProcessTask;
import org.opennars.control.DerivationContext;
import org.opennars.control.GeneralInferenceControl;
import org.opennars.control.TemporalInferenceControl;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.interfaces.Resettable;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.ResetEnd;
import org.opennars.io.events.Events.ResetStart;
import org.opennars.io.events.Events.TaskRemove;
import org.opennars.io.events.OutputHandler.IN;
import org.opennars.io.events.OutputHandler.OUT;
import org.opennars.io.events.OutputHandler.DEBUG;
import org.opennars.language.CompoundTerm;
import org.opennars.language.Interval;
import org.opennars.language.Tense;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.main.Parameters;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.InternalExperienceBuffer;
import org.opennars.plugin.perception.NarseseChannel;
import org.opennars.plugin.mental.Emotions;
import org.opennars.main.Debug;

import java.io.Serializable;
import java.util.*;
import org.opennars.entity.Stamp.BaseEntry;

import static org.opennars.inference.BudgetFunctions.truthToQuality;
import org.opennars.io.Channel;
import org.opennars.plugin.mental.InternalExperience;


/**
 * Memory consists of the run-time state of a Nar, including:
 *   * term and concept memory
 *   * reasoner state
 *   * etc.
 * <br>
 * Excluding input/output channels which are managed by a Nar.
 * <br>
 * A memory is controlled by zero or one Nar's at a given time.
 * <br>
 * Memory is serializable so it can be persisted and transported.
 */
public class Memory implements Serializable, Iterable<Concept>, Resettable {
    
     /* Nar parameters */
    public final Parameters narParameters;
    
    public InternalExperience internalExperience = null;
    public long narId = 0;
    //emotion meter keeping track of global emotion
    public Emotions emotion = null;  
    public Task lastDecision = null;
    public boolean allowExecution = true;

    public final long randomSeed = 1;
    public final Random randomNumber = new Random(randomSeed);
    
    //todo make sense of this class and de-obfuscate
    public final Bag<Concept,Term> concepts;
    public transient EventEmitter event;
    
    /* InnateOperator registry. Containing all registered operators of the system */
    public final Map<CharSequence, Operator> operators;
    
    /* a mutex for novel and new taskks*/
    private final Boolean tasksMutex = Boolean.TRUE;
    
    /* New tasks with novel composed terms, for delayed and selective processing*/
    public final Buffer/*<Task<Term>,Sentence<Term>>*/ globalBuffer;
    public InternalExperienceBuffer internalExperienceBuffer;
    public NarseseChannel narseseChannel = null;
    
    /* Input event tasks that were either input events or derived sequences*/
    public final Bag<Task<Term>,Sentence<Term>> recent_operations;  //only used for the optional legacy handling for comparison purposes
    
    //Boolean localInferenceMutex = false;


    boolean checked=false;
    boolean isjUnit=false;
    
    /* ---------- Constructor ---------- */
    /**
     * Create a new memory
     */
    public Memory(final Parameters narParameters, final Bag<Concept,Term> concepts, final Buffer globalBuffer,
                  final Buffer seq_current,
                  final Bag<Task<Term>,Sentence<Term>> recent_operations) {
        this.narParameters = narParameters;
        this.event = new EventEmitter();
        this.concepts = concepts;
        this.globalBuffer = globalBuffer; 
        this.recent_operations = recent_operations;
        this.globalBuffer.seq_current = seq_current;
        this.operators = new LinkedHashMap<>();
        this.internalExperienceBuffer = null; //overwritten
        reset();
    }
    
    public void reset() {
        event.emit(ResetStart.class);
        synchronized (concepts) {
            concepts.clear();
        }
        synchronized (tasksMutex) {
            globalBuffer.clear();
        }
        synchronized(this.globalBuffer.seq_current) {
            this.globalBuffer.seq_current.clear();
        }
        if(emotion != null) {
            emotion.resetEmotions();
        }
        if(internalExperienceBuffer != null)
        {
            internalExperienceBuffer.clear();
        }
        this.lastDecision = null;
        randomNumber.setSeed(randomSeed);
        event.emit(ResetEnd.class);
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
        synchronized (concepts) {
            return concepts.get(CompoundTerm.replaceIntervals(t));
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
    public Concept conceptualize(final BudgetValue budget, Term term) {   
        if(term instanceof Interval) {
            return null;
        }
        term = CompoundTerm.replaceIntervals(term);

        final Concept displaced;
        Concept concept;

        synchronized (concepts) {
            concept = concepts.pickOut(term);

            //see if concept is active
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

            displaced = concepts.putBack(concept, cycles(narParameters.CONCEPT_FORGET_DURATIONS), this);
        }

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
        synchronized (tasksMutex) {
            
            if(reason.equals("Executed") || reason.equals("Derived") || reason.equals("emotion")  || reason.equals("Internal"))
            {
                //these go to internal experience first, and only after go to global buffer:
                internalExperienceBuffer.putIn(t);
            }
            else
            {
                globalBuffer.putIn(t);
            }
        }
      //  logic.TASK_ADD_NEW.commit(t.getPriority());
        emit(Events.TaskAdd.class, t, reason);
        output(t);
    }

    public static boolean isJUnitTest() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final StackTraceElement[] list = stackTrace;
        for (final StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }           
        }
        return false;
    }
    

    /**
     * Input task processing. Invoked by the outside or inside environment.
     * Outside: StringParser (addInput);
     * Inside: InnateOperator (feedback).
     *
     * @param time indirection to retrieve time
     * @param task The addInput task
     */
    /* There are several types of new tasks, all added into the
     * newTasks list, to be processed in the next cycleMemory.
     * Some of them are reported and/or logged. */
    /*
     * Input tasks with low priority are ignored, and the others are put into task buffer.
     */
    public void inputTask(final Timable time, final Task task, final boolean emitIn) {
        if(!checked) {
            checked=true;
            isjUnit=isJUnitTest();
        }
        if (task != null) {
            final Stamp s = task.sentence.stamp;
            if (s.getCreationTime()==-1)
                s.setCreationTime(time.time(), narParameters.DURATION);

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

    /**
     * @param time indirection to retrieve time
     */
    public void inputTask(final Timable time, final Task t) {
        inputTask(time, t, true);
    }

    public void removeTask(final Task task, final String reason) {        
        emit(TaskRemove.class, task, reason);    
    }
    
    /**
     * ExecutedTask called in Operator.call
     *
     * @param operation The operation just executed
     * @param time indirection to retrieve time
     */
    public void executedTask(final Timable time, final Operation operation, final TruthValue truth) {
        final Task opTask = operation.getTask();
       // logic.TASK_EXECUTED.commit(opTask.budget.getPriority());
                
        final Stamp stamp = new Stamp(time, this, Tense.Present);
        final Sentence sentence = new Sentence(
            operation,
            Symbols.JUDGMENT_MARK,
            truth,
            stamp);

        final BudgetValue budgetForNewTask = new BudgetValue(narParameters.DEFAULT_FEEDBACK_PRIORITY,
            narParameters.DEFAULT_FEEDBACK_DURABILITY,
            truthToQuality(sentence.getTruth()), narParameters);
        final Task newTask = new Task(sentence, budgetForNewTask, Task.EnumType.INPUT);

        newTask.setElemOfSequenceBuffer(true);
        addNewTask(newTask, "Executed");
    }

    public void output(final Task t) {
        
        final float budget = t.budget.summary();
        final float noiseLevel = 1.0f - (narParameters.VOLUME / 100.0f);
        
        if (budget >= noiseLevel) {  // only report significant derived Tasks
            emit(OUT.class, t);
            if (Debug.PARENTS) {
                emit(DEBUG.class, "Parent Belief\t" + t.parentBelief);
                emit(DEBUG.class, "Parent Task\t" + t.parentTask + "\n\n");
            }
        }        
    }
    
    final public void emit(final Class c, final Object... signal) {        
        event.emit(c, signal);
    }

    final public boolean emitting(final Class channel) {
        return event.isActive(channel);
    }
    
    public void conceptRemoved(final Concept c) {
        emit(Events.ConceptForget.class, c);
    }
    
    public void cycle(final Nar nar) {
    
        event.emit(Events.CycleStart.class);
        //1. Channels to global buffer
        for(Channel c : nar.sensoryChannels.values()) {
            Task task = c.takeOut(); //retrieve an item from the Narsese channel
            if(task != null) {
                //optional: re-routing feature for "vision Narsese". not necessary but nice
                if(c == this.narseseChannel) {
                    if(nar.dispatchToSensoryChannel(task)) {
                        continue; //commented
                    }
                }
                //if it's not meant to enter another channel just put into global buffer
                this.addNewTask(task, "Perceived"); //goes to global buffer, but printing it
            }
        }
        //2. Internal experience buffer to global buffer
        Task t_internal = internalExperienceBuffer.takeOut(); //might have more than 1 item to take out
        if(t_internal != null) {
            globalBuffer.putIn(t_internal);
            internalExperienceBuffer.putBack(t_internal, narParameters.INTERNAL_BUFFER_FORGET_DURATIONS, this);
        }
        //3. process a task of global buffer
        this.processGlobalBufferTask(nar.narParameters, nar);
        //4. apply general inference step
        GeneralInferenceControl.selectConceptForInference(this, nar.narParameters, nar);
        event.emit(Events.CycleEnd.class);
        event.synch();
    }

    /**
     *
     * @param task task to be processed
     * @param narParameters parameters for the Reasoner instance
     * @param time indirection to retrieve time
     */
    public void localInference(final Task task, Parameters narParameters, final Timable time) {
        //synchronized (localInferenceMutex) {
            final DerivationContext cont = new DerivationContext(this, narParameters, time);
            cont.setCurrentTask(task);
            cont.setCurrentTerm(task.getTerm());
            cont.setCurrentConcept(conceptualize(task.budget, cont.getCurrentTerm()));
            if (cont.getCurrentConcept() != null) {
                final boolean processed = ProcessTask.processTask(cont.getCurrentConcept(), cont, task, time);
                if (processed) {
                    event.emit(Events.ConceptDirectProcessedTask.class, task);
                }
            }

            if (!task.sentence.isEternal() && !(task.sentence.term instanceof Operation)) {
                globalBuffer.eventInference(task, cont, false); //can be triggered by Buffer itself in the future
            }

            //memory.logic.TASK_IMMEDIATE_PROCESS.commit();
            emit(Events.TaskImmediateProcess.class, task, cont);
        //}
    }

    /**
     * Select a novel task to process
     *
     * @param narParameters parameters for the Reasoner instance
     * @param time indirection to retrieve time
     */
    public void processGlobalBufferTask(Parameters narParameters, final Timable time) {
        synchronized (tasksMutex) {
            final Task task = globalBuffer.takeOut();
            if (task != null) {
                if(!task.processed) {
                    task.processed = true;
                    localInference(task, narParameters, time);
                }
                globalBuffer.putBack(task, narParameters.GLOBAL_BUFFER_FORGET_DURATIONS, this);
            }
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
    public BaseEntry newStampSerial() {
        return new BaseEntry(this.narId, currentStampSerial++);
    }   

    /** converts durations to cycles */
    public final float cycles(final double durations) {
        return narParameters.DURATION * (float) durations;
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }
}
