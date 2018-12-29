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

import org.opennars.io.events.Events.Answer;
import org.opennars.main.Nar;
import org.opennars.storage.Memory;

/**
 * Output Channel: Implements this and Nar.addOutput(..) to receive output signals on various channels
 *
 */
public abstract class OutputHandler extends EventHandler {
    
    
    /** implicitly repeated input (a repetition of all input) */
    public interface IN  { }
    
    /** conversational (judgments, questions, etc...) output */
    public interface OUT  { }
    
    /** warnings, errors &amp; exceptions */
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
