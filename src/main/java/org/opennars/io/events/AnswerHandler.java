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
package org.opennars.io.events;

import org.opennars.io.events.EventEmitter.EventObserver;
import org.opennars.io.events.Events.Answer;
import org.opennars.main.NAR;
import org.opennars.entity.Sentence;
import org.opennars.entity.Task;

/**
 *
 * @author me
 */
public abstract class AnswerHandler implements EventObserver {
    
    private Task question;
    private NAR nar;
    
    final static Class[] events = new Class[] { Answer.class
 };
    
    public void start(Task question, NAR n) {
        this.nar = n;
        this.question = question;
                
        nar.event(this, true, events);
    }
    
    public void off() {
        nar.event(this, false, events);
    }

    @Override
    public void event(Class event, Object[] args) {                
        
        if (event == Answer.class) {
            Task task = (Task)args[0];
            Sentence belief = (Sentence)args[1];
            if (task.equals(question)) {
                onSolution(belief);
            }
        }
    }
    
    /** called when the question task has been solved directly */
    abstract public void onSolution(Sentence belief);
}
