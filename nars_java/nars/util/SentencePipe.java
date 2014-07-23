/*
 * Copyright (C) 2014 me
 *
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

package nars.util;

import java.util.concurrent.ArrayBlockingQueue;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.io.Input;
import nars.io.Output;

/**
 * Pipes sentences emitted from a NAR to another NAR.  Use two in opposite directions for bidirectional communication.
 * @author SeH
 */
public class SentencePipe implements Input, Output {
    private final NAR source;
    private final NAR target;
    private boolean active;
    private final ArrayBlockingQueue<Sentence> buffer;
    int bufferSize = 2048;
    
    public SentencePipe(NAR source, NAR target) {
        this.source = source;
        this.target = target;
        
        buffer = new ArrayBlockingQueue(bufferSize);
        active = true;
        
        source.addOutput(this);
        target.addInput(this);
        
    }

    

    @Override
    public void output(Class channel, Object o) {
        if (channel == OUT.class) {
            if (o instanceof Sentence) {
                //TODO avoid converting to string and instead insert directly to target's memory
                Sentence s = (Sentence)o;
                s = (Sentence)s.clone();
                s = process(s);
                //TODO: <statement_from_other_nars --> narsinput>.
                if (s!=null)
                    buffer.add(s);
            }
        }
    }
    
   @Override
    public boolean finished(boolean stop) {
        if (stop)
            active = false;
        return !active;
    }    

    @Override
    public Object next() {
        if (buffer.size() > 0)
            return buffer.remove();
        return null;
    }
    
    /**
     * for filtering or processing each sentence after output by source and before addInput to target.
     * @param s
     * @return the sentence, either as-is, modified, or null (will not transmit)
     */
    public Sentence process(Sentence s) {
        return s;
    }
    
 
    
    
    
}
