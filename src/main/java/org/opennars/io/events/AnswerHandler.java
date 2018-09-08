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
package org.opennars.io.events;

import org.opennars.entity.Sentence;
import org.opennars.entity.Task;
import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events.Answer;
import org.opennars.main.Nar;

/**
 *
 */
public abstract class AnswerHandler implements EventObserver {
    
    private Task question;
    private Nar nar;
    
    final static Class[] events = new Class[] {
        Answer.class
    };
    
    public void start(final Task question, final Nar n) {
        this.nar = n;
        this.question = question;
                
        nar.event(this, true, events);
    }
    
    public void off() {
        nar.event(this, false, events);
    }

    @Override
    public void event(final Class event, final Object[] args) {
        
        if (event == Answer.class) {
            final Task task = (Task)args[0];
            final Sentence belief = (Sentence)args[1];
            if (task.equals(question)) {
                onSolution(belief);
            }
        }
    }
    
    /** called when the question task has been solved directly */
    abstract public void onSolution(Sentence belief);
}
