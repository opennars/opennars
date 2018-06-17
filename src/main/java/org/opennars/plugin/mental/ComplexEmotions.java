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
import org.opennars.io.events.EventEmitter;
import org.opennars.io.events.Events;
import org.opennars.io.events.Events.Answer;
import org.opennars.main.Nar;
import org.opennars.plugin.Plugin;
import org.opennars.storage.Memory;

import static org.opennars.inference.LocalRules.solutionQuality;

/**
 *
 * @author Patrick
 */
public class ComplexEmotions implements Plugin {

    public EventEmitter.EventObserver obs;
    float fear = 0.5f;
    @Override
    public boolean setEnabled(final Nar n, final boolean enabled) {
        if(enabled) {
            
            final Memory memory = n.memory;
        
            if(obs==null) {
                obs= (event, a) -> {
                    if (event != Events.TaskDerive.class &&
                            event != Events.InduceSucceedingEvent.class)
                        return;
                    final Task future_task = (Task)a[0];

                    if(future_task.sentence.getOccurenceTime() > n.time()) {
                        final Concept c = n.memory.concept(future_task.getTerm());
                        final float true_expectation = 0.5f;
                        final float false_expectation = 0.5f;
                        if(c != null) {
                            if(c.desires.size() > 0 && c.beliefs.size() > 0) {
                                //Fear:
                                if(future_task.sentence.truth.getExpectation() > true_expectation &&
                                   c.desires.get(0).sentence.truth.getExpectation() < false_expectation) {
                                    //n.addInput("<(*,{SELF},fear) --> ^feel>. :|:");
                                    final float weight = future_task.getPriority();
                                    float fear = solutionQuality(true, c.desires.get(0), future_task.sentence, memory, n);
                                    final float newValue = fear*weight;
                                    fear += newValue * weight;
                                    fear /= 1.0f + weight;
                                    //incrase concept priority by fear value:
                                    final Concept C1 = memory.concept(future_task.getTerm());
                                    if(C1 != null) {
                                        C1.incPriority(fear);
                                    }
                                    memory.emit(Answer.class, "Fear value="+fear);
                                    System.out.println("Fear value="+fear);
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
