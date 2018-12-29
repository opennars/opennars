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
