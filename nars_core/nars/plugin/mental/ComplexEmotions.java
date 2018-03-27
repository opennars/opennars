/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.plugin.mental;

import nars.entity.Concept;
import nars.entity.Task;
import nars.io.events.EventEmitter;
import nars.io.events.EventEmitter.EventObserver;
import nars.io.events.Events;
import nars.io.events.Events.Answer;
import nars.io.events.OutputHandler;
import nars.main.NAR;
import nars.plugin.Plugin;
import nars.storage.Memory;

/**
 *
 * @author Patrick
 */
public class ComplexEmotions implements Plugin {

    public EventEmitter.EventObserver obs;
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
                            float true_expectation = 0.6f;
                            float false_expectation = 0.4f;
                            if(c != null) {
                                if(c.desires.size() > 0 && c.beliefs.size() > 0) {
                                    /*
                                    want: a!
                                    have a. :|:
                                    believe it will stay that way: a. :/:
                                    Happiness about a
                                    */
                                    if(c.desires.get(0).sentence.truth.getExpectation() > true_expectation) {
                                        if(c.beliefs.get(0).sentence.truth.getExpectation() > true_expectation) {
                                                if(future_task.sentence.truth.getExpectation() > true_expectation) {
                                                        System.out.println("happy");
                                                        n.addInput("<(*,{SELF},happy) --> ^feel>. :|:");
                                                         memory.emit(Answer.class, "happy");
                                                }
                                        }
                                    }
                                    
                                    if(c.desires.get(0).sentence.truth.getExpectation() < false_expectation) {
                                        if(c.beliefs.get(0).sentence.truth.getExpectation() < false_expectation) {
                                                if(future_task.sentence.truth.getExpectation() > true_expectation) {
                                                        System.out.println("fear");
                                                        n.addInput("<(*,{SELF},fear) --> ^feel>. :|:");
                                                        memory.emit(Answer.class, "fear");
                                                }
                                        }
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
