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
package org.opennars.io.events;

import org.opennars.io.events.Events.Answer;
import org.opennars.main.Nar;
import org.opennars.storage.Memory;

/**
 * Output Channel: Implements this and Nar.addOutput(..) to receive output signals on various channels
 */
public abstract class OutputHandler extends EventHandler {
    
    
    /** implicitly repeated input (a repetition of all input) */
    public interface IN  { }
    
    /** conversational (judgments, questions, etc...) output */
    public interface OUT  { }
    
    /** warnings, errors & exceptions */
    public interface ERR { }
    
    /** explicitly repeated input (repetition of the content of input ECHO commands) */
    public interface ECHO  { }
    
    /** operation execution */
    public interface EXE  { }
    
        
    public static class ANTICIPATE {}
    
    public static class CONFIRM {}
    
    public static class DISAPPOINT {}

    public static final Class[] DefaultOutputEvents = new Class[] { IN.class, EXE.class, OUT.class, ERR.class, ECHO.class, Answer.class, ANTICIPATE.class, CONFIRM.class, DISAPPOINT.class };
            
    public OutputHandler(final EventEmitter source, final boolean active) {
        super(source, active, DefaultOutputEvents );
    }
    
    public OutputHandler(final Memory m, final boolean active) {
        this(m.event, active);
    }

    public OutputHandler(final Nar n, final boolean active) {
        this(n.memory.event, active);
    }

    public OutputHandler(final Nar n) {
        this(n, true);
    }

}
