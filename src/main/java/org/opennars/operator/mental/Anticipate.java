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
package org.opennars.operator.mental;

import org.opennars.control.DerivationContext;
import org.opennars.entity.*;
import org.opennars.inference.BudgetFunctions;
import org.opennars.interfaces.Timable;
import org.opennars.io.Symbols;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.CycleEnd;
import org.opennars.io.events.OutputHandler.ANTICIPATE;
import org.opennars.io.events.OutputHandler.CONFIRM;
import org.opennars.io.events.OutputHandler.DISAPPOINT;
import org.opennars.language.Interval;
import org.opennars.language.Product;
import org.opennars.language.Term;
import org.opennars.main.Nar;
import org.opennars.operator.Operation;
import org.opennars.operator.Operator;
import org.opennars.storage.Memory;

import java.util.*;

/**
 * Operator that creates a judgment with a given statement
 */
public class Anticipate extends Operator implements EventObserver {

    public final Map<Prediction,LinkedHashSet<Term>> anticipations = new LinkedHashMap();
            
    private transient Set<Term> newTasks = new LinkedHashSet();
 
    private TruthValue expiredTruth = null;
    private BudgetValue expiredBudget = null;
    
     //internal experience has less durability?
    public float ANTICIPATION_DURABILITY_MUL=0.1f; //0.1
    //internal experience has less priority?
    public float ANTICIPATION_PRIORITY_MUL=0.1f; //0.1


    private transient DerivationContext nal; //don't serialize, it will be re-set after deserialization


    public Anticipate() {
        super("^anticipate");        
    }
    public Anticipate(float ANTICIPATION_DURABILITY_MUL, float ANTICIPATION_PRIORITY_MUL) {
        this();
        this.ANTICIPATION_DURABILITY_MUL = ANTICIPATION_DURABILITY_MUL;
        this.ANTICIPATION_PRIORITY_MUL = ANTICIPATION_PRIORITY_MUL;
    }

    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        n.memory.event.set(this, enabled, Events.InduceSucceedingEvent.class, Events.CycleEnd.class);
        expiredTruth = new TruthValue(0.0f, n.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, n.narParameters);
        expiredBudget = new BudgetValue(n.narParameters.DEFAULT_JUDGMENT_PRIORITY, 
                                                    n.narParameters.DEFAULT_JUDGMENT_DURABILITY, 
                                                    BudgetFunctions.truthToQuality(expiredTruth), n.narParameters);
        return true;
    }

    public void updateAnticipations(DerivationContext nal) {

        if (anticipations.isEmpty()) return;

        final long now=nal.time.time();
                
        //share stamps created by tasks in this cycle
        if(newTasks == null) {
            newTasks = new LinkedHashSet();
        }
        boolean hasNewTasks = !newTasks.isEmpty();
        
        final Iterator<Map.Entry<Prediction, LinkedHashSet<Term>>> aei = anticipations.entrySet().iterator();
        while (aei.hasNext()) {
            
            final Map.Entry<Prediction, LinkedHashSet<Term>> ae = aei.next();
            
            final long aTime = ae.getKey().predictedOccurenceTime;
            final long predictionstarted=ae.getKey().predictionCreationTime;
            if(aTime < predictionstarted) { //its about the past..
                return;
            }
            
            //lets say  a and <(&/,a,+4) =/> b> leaded to prediction of b with specific occurence time
            //this indicates that this interval can be reconstructed by looking by when the prediction
            //happened and for what time it predicted, Only when the happening would already lead to <(&/,a,+5) =/> b>
            //we are allowed to apply CWA already, i think this is the perfect time to do this
            //since there is no way anymore that the observation would support <(&/,a,+4) =/> b> at this time,
            //also this way it is not applied to early, it seems to be the perfect time to me,
            //making hopeExpirationWindow parameter entirely osbolete
            final Interval Int=new Interval(aTime-predictionstarted);
            //ok we know the magnitude now, let's now construct a interval with magnitude one higher
            //(this we can skip because magnitudeToTime allows it without being explicitly constructed)
            //ok, and what predicted occurence time would that be? because only if now is bigger or equal, didnt happen is true
            final double expiredate=predictionstarted+Int.time*nal.narParameters.ANTICIPATION_TOLERANCE;
            //
            
            final boolean didntHappen = (now>=expiredate);
            final boolean maybeHappened = hasNewTasks && !didntHappen;
                
            if ((!didntHappen) && (!maybeHappened))
                continue;
            
            final LinkedHashSet<Term> terms = ae.getValue();
            
            final Iterator<Term> ii = terms.iterator();
            while (ii.hasNext()) {
                final Term aTerm = ii.next();
                
                boolean remove = false;
                
                if (didntHappen) {
                    deriveDidntHappen(aTerm,aTime,nal);
                    remove = true;
                }

                if (maybeHappened) {
                    if(newTasks == null) {
                        newTasks = new LinkedHashSet();
                    }
                    if (newTasks.remove(aTerm)) {
                        //in case it happened, temporal induction will do the rest, else deriveDidntHappen occurred
                        if(!remove) {
                            nal.memory.emit(CONFIRM.class, aTerm);
                        }
                        remove = true; 
                        hasNewTasks = !newTasks.isEmpty();
                    }
                }
                
                if (remove)
                    ii.remove();
            }
            
            if (terms.isEmpty()) {
                //remove this time entry because its terms have been emptied
                aei.remove();
            }
        }       
    
        if(newTasks == null) {
            newTasks = new LinkedHashSet();
        }
        newTasks.clear();        
    }
    
    @Override
    public void event(final Class event, final Object[] args) {
        if (event == Events.InduceSucceedingEvent.class || event == Events.TaskDerive.class) {            
            final Task newEvent = (Task)args[0];
            DerivationContext nal= (DerivationContext)args[1];
            this.nal = nal;
            
            if (newEvent.sentence.truth != null && newEvent.sentence.isJudgment() && newEvent.sentence.truth.getExpectation() > nal.narParameters.DEFAULT_CONFIRMATION_EXPECTATION && !newEvent.sentence.isEternal()) {
                if(newTasks == null) {
                    newTasks = new LinkedHashSet();
                }
                newTasks.add(newEvent.getTerm()); //new: always add but keep truth value in mind
            }
        }

        if (this.nal!=null && event == CycleEnd.class) {            
            updateAnticipations(this.nal);
        }
    }
    

    // to create a judgment with a given statement
    @Override
    protected List<Task> execute(final Operation operation, final Term[] args, final Memory memory, final Timable time) {
        if (operation==null) {
            return null; //not as mental operator but as fundamental principle
        }
        
        anticipate(args[1], memory, time.time()+memory.narParameters.DURATION, null, time);
        
        return null;
    }
    
    boolean anticipationOperator=true; //a parameter which tells whether NARS should know if it anticipated or not
    //in one case its the base functionality needed for NAL7 and in the other its a mental NAL9 operator
    
    public boolean isAnticipationAsOperator() {
        return anticipationOperator;
    }
    
    public void setAnticipationAsOperator(final boolean val) {
        anticipationOperator=val;
    }
    
    public void anticipate(final Term content, final Memory memory, final long occurenceTime, final Task t, final Timable time) {
        if(t!=null && t.sentence.truth.getExpectation() < memory.narParameters.DEFAULT_CONFIRMATION_EXPECTATION) {
            return;
        }

       if(t != null) {
           memory.emit(ANTICIPATE.class, t);
       } else  {
          memory.emit(ANTICIPATE.class, content);
       }

        final LinkedHashSet<Term> ae = new LinkedHashSet();
        anticipations.put(new Prediction(time.time(),occurenceTime), ae);

        ae.add(content);
        anticipationFeedback(content, t, memory, time);
    }

    public void anticipationFeedback(final Term content, final Task t, final Memory memory, final Timable time) {
        if(anticipationOperator) {
            final Operation op=(Operation) Operation.make(Product.make(Term.SELF,content), this);
            final TruthValue truth=new TruthValue(1.0f,memory.narParameters.DEFAULT_JUDGMENT_CONFIDENCE, memory.narParameters);
            final Stamp st;
            if(t==null) {
                st=new Stamp(time, memory);
            } else {
                st=t.sentence.stamp.clone();
                st.setOccurrenceTime(time.time());
            }

            final Sentence s=new Sentence(
                    op,
                    Symbols.JUDGMENT_MARK,
                    truth,
                    st);

            final BudgetValue budgetForNewTask = new BudgetValue(
                memory.narParameters.DEFAULT_JUDGMENT_PRIORITY*ANTICIPATION_PRIORITY_MUL,
                memory.narParameters.DEFAULT_JUDGMENT_DURABILITY*ANTICIPATION_DURABILITY_MUL,
                BudgetFunctions.truthToQuality(truth), memory.narParameters);
            final Task newTask = new Task(s, budgetForNewTask, Task.EnumType.INPUT);

            memory.addNewTask(newTask, "Perceived (Internal Experience: Anticipation)");
        }
    }

    protected void deriveDidntHappen(final Term aTerm, final long expectedOccurenceTime, DerivationContext nal) {

        final TruthValue truth = expiredTruth;
        final BudgetValue budget = expiredBudget;

        final Stamp stamp = new Stamp(nal.time, nal.memory);
        //stamp.setOccurrenceTime(nal.memory.time());
        stamp.setOccurrenceTime(expectedOccurenceTime); //it did not happen, so the time of when it did not 
        //happen is exactly the time it was expected
        
        final Sentence S = new Sentence(
            aTerm,
            Symbols.JUDGMENT_MARK,
            truth,
            stamp);

        final Task task = new Task(S, budget, Task.EnumType.INPUT);

        nal.derivedTask(task, false, true, false); 
        task.setElemOfSequenceBuffer(true);
        nal.memory.emit(DISAPPOINT.class, task);
    }

    class Prediction {
        public final long predictionCreationTime; //2014 and this is still the best way to define a data structure that simple?
        public final long predictedOccurenceTime;
        public Prediction(final long predictionCreationTime, final long predictedOccurenceTime) { //rest of the crap:
            this.predictionCreationTime=predictionCreationTime; //when the prediction happened
            this.predictedOccurenceTime=predictedOccurenceTime; //when the event is expected
        }
    }
}
