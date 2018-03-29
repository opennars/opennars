/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.plugin.mental;

import nars.entity.Concept;
import nars.entity.Task;
import static nars.inference.LocalRules.solutionQuality;
import nars.io.events.EventEmitter;
import nars.io.events.EventEmitter.EventObserver;
import nars.io.events.Events;
import nars.io.events.Events.Answer;
import nars.language.Term;
import nars.main.NAR;
import nars.plugin.Plugin;
import nars.storage.Memory;

/**
 *
 * @author Patrick
 */
public class ComplexEmotions implements Plugin {

    public EventEmitter.EventObserver obs;
    float fear = 0.5f;
    @Override
    public boolean setEnabled(NAR n, boolean enabled) {
        if(enabled) {
            
            Memory memory = n.memory;
        
            if(obs==null) {
                obs=new EventObserver() {
                    @Override
                    public void event(Class event, Object[] a) {
                        if (event != Events.TaskDerive.class &&
                                event != Events.InduceSucceedingEvent.class)
                            return;
                        Task future_task = (Task)a[0];
                        
                        if(future_task.sentence.getOccurenceTime() > n.time()) {
                            Concept c = n.memory.concept(future_task.getTerm());
                            float true_expectation = 0.5f;
                            float false_expectation = 0.5f;
                            if(c != null) {
                                if(c.desires.size() > 0 && c.beliefs.size() > 0) {
                                    //Fear:
                                    if(future_task.sentence.truth.getExpectation() > true_expectation &&
                                       c.desires.get(0).sentence.truth.getExpectation() < false_expectation) {
                                        //n.addInput("<(*,{SELF},fear) --> ^feel>. :|:");
                                        float weight = future_task.getPriority();
                                        float fear = solutionQuality(true, c.desires.get(0), future_task.sentence, memory);
                                        float newValue = fear*weight;
                                        fear += newValue * weight;
                                        fear /= 1.0f + weight;
                                        //incrase concept priority by fear value:
                                        Concept C1 = memory.concept(future_task.getTerm());
                                        if(C1 != null) {
                                            C1.incPriority(fear);
                                        }
                                        memory.emit(Answer.class, "Fear value="+fear);
                                        System.out.println("Fear value="+fear);
                                    }
                                }
                            }
                        }
                    }
                };
            }
            memory.event.set(obs, enabled, Events.InduceSucceedingEvent.class, Events.TaskDerive.class);
        }
        return true;
    }
}
