/*
 * Believe.java
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


package nars.operator.mental;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events;
import nars.core.Events.CycleEnd;
import nars.core.Memory;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.control.NAL;
import nars.entity.BudgetValue;
import nars.entity.Sentence;
import nars.entity.Stamp;
import nars.entity.Task;
import nars.entity.TruthValue;
import nars.inference.BudgetFunctions;
import nars.inference.TemporalRules;
import nars.io.Symbols;
import nars.language.Conjunction;
import nars.language.Interval;
import nars.language.Product;
import nars.language.Term;
import nars.operator.Operation;
import nars.operator.Operator;
import nars.plugin.mental.InternalExperience;

//**
//* Operator that creates a judgment with a given statement
 //*
public class Anticipate extends Operator implements EventObserver, Mental {

    public final Map<Vector2Int,LinkedHashSet<Term>> anticipations = new LinkedHashMap();
            
    final Set<Term> newTasks = new LinkedHashSet();
    NAL nal;

    final static TruthValue expiredTruth = new TruthValue(0.0f, Parameters.DEFAULT_JUDGMENT_CONFIDENCE);
    final static BudgetValue expiredBudget = new BudgetValue(Parameters.DEFAULT_JUDGMENT_PRIORITY, Parameters.DEFAULT_JUDGMENT_DURABILITY, BudgetFunctions.truthToQuality(expiredTruth));
    
    public Anticipate() {
        super("^anticipate");        
    }

    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        n.memory.event.set(this, enabled, Events.InduceSucceedingEvent.class, Events.CycleEnd.class);
        return true;
    }
    
    class Vector2Int {
        public long predictionCreationTime; //2014 and this is still the best way to define a data structure that simple?
        public long predictedOccurenceTime; 
        public Vector2Int(long predictionCreationTime, long predictedOccurenceTime) { //rest of the crap:
            this.predictionCreationTime=predictionCreationTime; //when the prediction happened
            this.predictedOccurenceTime=predictedOccurenceTime; //when the event is expected
        }
    }
    
    public void updateAnticipations() {

        if (anticipations.isEmpty()) return;

        long now=nal.memory.time();
                
        //share stamps created by tasks in this cycle
        
        boolean hasNewTasks = !newTasks.isEmpty();
        
        Iterator<Map.Entry<Vector2Int, LinkedHashSet<Term>>> aei = anticipations.entrySet().iterator();
        while (aei.hasNext()) {
            
            Map.Entry<Vector2Int, LinkedHashSet<Term>> ae = aei.next();
            
            long aTime = ae.getKey().predictedOccurenceTime;
            long predictionstarted=ae.getKey().predictionCreationTime;
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
            Interval Int=Interval.interval(aTime-predictionstarted, nal.memory);
            //ok we know the magnitude now, let's now construct a interval with magnitude one higher
            //(this we can skip because magnitudeToTime allows it without being explicitly constructed)
            //ok, and what predicted occurence time would that be? because only if now is bigger or equal, didnt happen is true
            double expiredate=predictionstarted+Interval.magnitudeToTime(Int.magnitude+1, nal.memory.param.duration);
            //
            
            boolean didntHappen = (now>=expiredate);
            boolean maybeHappened = hasNewTasks && !didntHappen;
                
            if ((!didntHappen) && (!maybeHappened))
                continue;
            
            LinkedHashSet<Term> terms = ae.getValue();
            
            Iterator<Term> ii = terms.iterator();
            while (ii.hasNext()) {
                Term aTerm = ii.next();
                
                boolean remove = false;
                
                if (didntHappen) {
                    deriveDidntHappen(aTerm,aTime);                                
                    remove = true;
                }

                if (maybeHappened) {
                    if (newTasks.remove(aTerm)) {
                        //it happened, temporal induction will do the rest          
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
    
        newTasks.clear();        
    }
    
    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.InduceSucceedingEvent.class) {            
            Task newEvent = (Task)args[0];
            this.nal= (NAL)args[1];
            
            if (newEvent.sentence.truth!=null) {
                newTasks.add(newEvent.getTerm()); //new: always add but keep truth value in mind
            }
        }

        if (nal!=null && event == CycleEnd.class) {            
            updateAnticipations();
        }
    }
    

    //*
    // * To create a judgment with a given statement
    // * @param args Arguments, a Statement followed by an optional tense
    // * @param memory The memory in which the operation is executed
   // * @return Immediate results as Tasks
   //  *
    @Override
    protected ArrayList<Task> execute(Operation operation, Term[] args, Memory memory) {
        if(operation!=null) {
            return null; //not as mental operator but as fundamental principle
        }
        
        anticipate(args[0],memory,memory.time()+memory.getDuration(), null);
        
        return null;
    }
    
    boolean anticipationOperator=false; //a parameter which tells whether NARS should know if it anticipated or not
    //in one case its the base functionality needed for NAL8 and in the other its a mental NAL9 operator
    
    public boolean isAnticipationAsOperator() {
        return anticipationOperator;
    }
    
    public void setAnticipationAsOperator(boolean val) {
        anticipationOperator=val;
    }
    
    public void anticipate(Term content,Memory memory, long occurenceTime, Task t) {
        if(content instanceof Conjunction && ((Conjunction)content).getTemporalOrder()!=TemporalRules.ORDER_NONE) {
            return;
        }
        
        if(t.sentence.truth.getExpectation()<Parameters.DEFAULT_CONFIRMATION_EXPECTATION) {
            return;
        } 
        
        LinkedHashSet<Term> ae = new LinkedHashSet();
        anticipations.put(new Vector2Int(memory.time(),occurenceTime), ae);

        ae.add(content);
        
        if(anticipationOperator) {
            Operation op=(Operation) Operation.make(Product.make(content), this);
            TruthValue truth=new TruthValue(1.0f,0.90f);
            Stamp st;
            if(t==null) {
                st=new Stamp(memory);
            } else {
                st=t.sentence.stamp.clone();
                st.setOccurrenceTime(memory.time());
            }

            Sentence s=new Sentence(op,Symbols.JUDGMENT_MARK,truth,st);
            Task newTask=new Task(s,new BudgetValue(
                    Parameters.DEFAULT_JUDGMENT_PRIORITY*InternalExperience.INTERNAL_EXPERIENCE_PRIORITY_MUL,
                    Parameters.DEFAULT_JUDGMENT_DURABILITY*InternalExperience.INTERNAL_EXPERIENCE_DURABILITY_MUL,
                    BudgetFunctions.truthToQuality(truth)));
            memory.addNewTask(newTask, "Perceived (Internal Experience: Anticipation)");
        }
    }

    protected void deriveDidntHappen(Term aTerm, long expectedOccurenceTime) {
                
        TruthValue truth = expiredTruth;
        BudgetValue budget = expiredBudget;

        Stamp stamp = new Stamp(nal.memory);
        //stamp.setOccurrenceTime(nal.memory.time());
        stamp.setOccurrenceTime(expectedOccurenceTime); //it did not happen, so the time of when it did not 
        //happen is exactly the time it was expected
        
        Sentence S = new Sentence(aTerm, Symbols.JUDGMENT_MARK, truth, stamp);
        Task task = new Task(S, budget);
        nal.derivedTask(task, false, true, null, null, false); 
        task.setParticipateInTemporalInductionOnSucceedingEvents(true);
    }
}