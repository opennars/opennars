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

package nars.io;

import nars.core.NAR;
import nars.entity.Sentence;

/**
 * Pipes sentences emitted from a NAR to another NAR.  Use two in opposite directions for bidirectional communication.
 * @author SeH
 */
public class SentencePipe implements Input, Output {
    private final NAR source;
    private final NAR target;
    private boolean active;

    public SentencePipe(NAR source, NAR target) {
        this.source = source;
        this.target = target;
        
        source.addOutputChannel(this);
        target.addInputChannel(this);
        
        active = true;
    }

    
    @Override
    public boolean nextInput() {
        return active;
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
                    new TextInput(target, s.toString());
            }
        }
    }
    
    /**
     * for filtering or processing each sentence after output by source and before input to target.
     * @param s
     * @return the sentence, either as-is, modified, or null (will not transmit)
     */
    public Sentence process(Sentence s) {
        return s;
    }
    
    @Override
    public boolean isClosed() {
        return !active;
    }    
    
    public void close() {
        active = false;
    }
    
}
