/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.operate.mental;

import nars.Events;
import nars.NAR;
import nars.NAR.PluggedIn;
import nars.Global;
import nars.budget.Budget;
import nars.nal.*;
import nars.nal.term.Term;
import nars.operate.IOperator;
import nars.event.Reaction;
import nars.io.Symbols;
import nars.nal.stamp.Stamp;
import nars.nal.nal5.Conjunction;
import nars.nal.nal5.Implication;
import nars.nal.nal7.Interval;
import nars.nal.nal7.TemporalRules;
import nars.nal.nal7.Tense;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tc
 */
public class GlobalAnticipation implements IOperator, Reaction {

    public final ArrayDeque<Task> stm = new ArrayDeque();
    public final List<Task> current_tasks=new ArrayList<Task>();
    int MatchUpTo=20;
    
    public void setMatchEventsMax(double value) {
        MatchUpTo=(int) value;
    }
    
    public double getMatchEventsMax() {
        return MatchUpTo;
    }
    
    public double TEMPORAL_PREDICTION_FEEDBACK_ACCURACY_DIV=0.01;
    
    public double getTemporalAccuracy() {
        return TEMPORAL_PREDICTION_FEEDBACK_ACCURACY_DIV;
    }
    
    public void setTemporalAccuracy(double value) {
        TEMPORAL_PREDICTION_FEEDBACK_ACCURACY_DIV=value;
    }
    
    @Override
    public void event(Class event, Object[] args) {
        if (event == Events.TaskDerive.class) {
            Task derivedTask=(Task) args[0];
            if(derivedTask.sentence.term instanceof Implication &&
               (derivedTask.sentence.term.getTemporalOrder()==TemporalRules.ORDER_FORWARD ||
                    derivedTask.sentence.term.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT)) {

                if(!current_tasks.contains(derivedTask)) {
                    current_tasks.add(derivedTask);
                }
            }
        }
        else if (event == Events.ConceptBeliefRemove.class) {
            Task removedTask=(Task) args[2]; //task is 3nd
            if(current_tasks.contains(removedTask)) {
                current_tasks.remove(removedTask);
            }            
        }
        else if (event == Events.InduceSucceedingEvent.class) {            
            Task newEvent = (Task)args[0];
            NAL nal= (NAL)args[1];
            
            if (newEvent.sentence.truth!=null) {
                stm.add(newEvent);
                while(stm.size()>MatchUpTo) {
                    stm.removeFirst();
                }
            }
            
            temporalPredictionsAdapt(nal);
        }
    }  
    
    //check all predictive statements, match them with last events
    public void temporalPredictionsAdapt(NAL nal) {
        if(TEMPORAL_PREDICTION_FEEDBACK_ACCURACY_DIV==0.0f) {
            return; 
        }
        
        ArrayList<Task> lastEvents=new ArrayList<Task>();
        for (Task stmLast : stm) {
            lastEvents.add(stmLast);
        }
        
        if(lastEvents.isEmpty()) {
            return;
        }
        
        final long duration = nal.memory.param.duration.get();
        ArrayList<Task> derivetasks=new ArrayList<Task>();
        
        for(final Task c : current_tasks) { //a =/> b or (&/ a1...an) =/> b
            boolean concurrent_conjunction=false;
            Term[] args=new Term[1];
            Implication imp=(Implication) c.sentence.term.clone();
            boolean concurrent_implication=imp.getTemporalOrder()==TemporalRules.ORDER_CONCURRENT;
            args[0]=imp.getSubject();
            if(imp.getSubject() instanceof Conjunction) {
                Conjunction conj=(Conjunction) imp.getSubject();
                if(conj.temporalOrder==TemporalRules.ORDER_FORWARD || conj.temporalOrder==TemporalRules.ORDER_CONCURRENT) {
                    concurrent_conjunction=conj.temporalOrder==TemporalRules.ORDER_CONCURRENT;
                    args=conj.term; //in case of &/ this are the terms
                }
            }
            int i=0;
            boolean matched=true;
            int off=0;
            long expected_time=lastEvents.get(0).sentence.getOccurrenceTime();
            
            for(i=0;i<args.length;i++) {
                //handling of intervals:
                if(args[i] instanceof Interval) {
                    if(!concurrent_conjunction) {
                        expected_time+=((Interval)args[i]).cycles(nal.memory.param.duration);
                    }
                    off++;
                    continue;
                }

                if(i-off>=lastEvents.size()) {
                    break;
                }

                //handling of other events, seeing if they match and are right in time
                
                if(!Variables.hasSubstitute(Symbols.VAR_INDEPENDENT, args[i], lastEvents.get(i-off).sentence.term)) { //it didnt match, instead sth different unexpected happened
                    matched=false; //whether intermediate events should be tolerated or not was a important question when considering this,
                    break; //if it should be allowed, the sequential match does not matter only if the events come like predicted.
                } else { //however I decided that sequence matters also for now, because then the more accurate hypothesis wins.

                    if(lastEvents.get(i-off).sentence.truth.getExpectation()<=0.5f) { //it matched according to sequence, but is its expectation bigger than 0.5? todo: decide how truth values of the expected events
                        //it didn't happen
                        matched=false;
                        break;
                    }

                    long occurence=lastEvents.get(i-off).sentence.getOccurrenceTime();
                    boolean right_in_time=Math.abs(occurence-expected_time) < ((double)duration)/TEMPORAL_PREDICTION_FEEDBACK_ACCURACY_DIV;
                    if(!right_in_time) { //it matched so far, but is the timing right or did it happen when not relevant anymore?
                        matched=false;
                        break;
                    }
                }

                if(!concurrent_conjunction) {
                    expected_time+=duration;
                }
            }

            if(concurrent_conjunction && !concurrent_implication) { //implication is not concurrent
                expected_time+=duration; //so here we have to add duration
            }
            else
            if(!concurrent_conjunction && concurrent_implication) {
                expected_time-=duration;
            } //else if both are concurrent, time has never been added so correct
              //else if both are not concurrent, time was always added so also correct

            //ok it matched, is the consequence also right?
            if(matched && lastEvents.size()>args.length-off) { 
                long occurence=lastEvents.get(args.length-off).sentence.getOccurrenceTime();
                boolean right_in_time=Math.abs(occurence-expected_time)<((double)duration)/TEMPORAL_PREDICTION_FEEDBACK_ACCURACY_DIV;
                 
                if(right_in_time && Variables.hasSubstitute(Symbols.VAR_INDEPENDENT,imp.getPredicate(),lastEvents.get(args.length-off).sentence.term)) { //it matched and same consequence, so positive evidence
                    //c.sentence.truth=TruthFunctions.revision(c.sentence.truth, new TruthValue(1.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE));
                    Sentence s2=new Sentence(c.sentence.term.clone(),Symbols.JUDGMENT,new TruthValue(1.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE),new Stamp(nal.memory, Tense.Present));
                    Task t=new Task(s2,new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY,s2.truth));
                    derivetasks.add(t);
                } else { //it matched and other consequence, so negative evidence
                   // c.sentence.truth=TruthFunctions.revision(c.sentence.truth, new TruthValue(0.0f,Parameters.DEFAULT_JUDGMENT_CONFIDENCE));
                    Sentence s2=new Sentence(c.sentence.term.clone(),Symbols.JUDGMENT,new TruthValue(0.0f, Global.DEFAULT_JUDGMENT_CONFIDENCE),new Stamp(nal.memory, Tense.Present));
                    Task t=new Task(s2,new Budget(Global.DEFAULT_JUDGMENT_PRIORITY, Global.DEFAULT_JUDGMENT_DURABILITY,s2.truth));
                    derivetasks.add(t);
                } //todo use derived task with revision instead
            }
        }
        for(Task t: derivetasks) {
            if(nal.deriveTask(t, false, false, null, false)) {
                boolean debug=true;
            }
        }
        ArrayList<Task> toDelete=new ArrayList<Task>();
        for(Task t: current_tasks) {
            Concept w=nal.memory.concept(t.sentence.term);
            if(w==null) { //concept does not exist anymore, delete
                toDelete.add(t);
            }
        }
        for(Task t: toDelete) {
            current_tasks.remove(t);
        }
    }
    
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        //Events.TaskDerive.class Events.ConceptBeliefRemove.class
        n.memory.event.set(this, enabled, Events.InduceSucceedingEvent.class, Events.TaskDerive.class, Events.ConceptBeliefRemove.class);
        for(PluggedIn s : n.getPlugins()) {
            if(s.IOperator instanceof Anticipate)
            {
                s.IOperator.setEnabled(n, !enabled);
            }
        }
        
        return true;
    }
    
}
