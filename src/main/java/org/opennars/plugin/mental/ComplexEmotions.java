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
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.plugin.mental;

import org.opennars.entity.Concept;
import org.opennars.entity.Task;
import static org.opennars.inference.LocalRules.solutionQuality;
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.Answer;
import org.opennars.main.NAR;
import org.opennars.plugin.Plugin;
import org.opennars.storage.Memory;

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
