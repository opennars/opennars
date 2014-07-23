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

package nars.test.util.test;

import java.io.StringWriter;
import nars.core.NAR;
import nars.entity.Sentence;
import nars.util.SentencePipe;
import nars.io.TextInput;
import nars.io.TextOutput;
import org.junit.Test;

/**
 *
 * @author me
 */
public class SentencePipeTest {
    
    int sentencesProcessed = 0;
    
    @Test
    public void testPipe() {
        NAR source = new NAR();
        NAR target = new NAR();
        
        new SentencePipe(source, target) {

            @Override
            public Sentence process(Sentence s) {
                sentencesProcessed++;
                return s;
            }            
            
        };
        
        StringWriter sw = new StringWriter();

        
        final String sourceLinePrefix = "Source: ";
        new TextOutput(source, sw).setLinePrefix(sourceLinePrefix);
        new TextOutput(target, sw).setLinePrefix("Target: ");
        
        source.addInput("<a --> b>.");
        source.addInput("<a --> b>?");
        
        assert(target.memory.concepts.size() == 0);
        
        source.finish(8);
        
        assert(sentencesProcessed > 0);
        
        target.finish(8);
        
        
        //test prefix output
        String swBuffer = sw.getBuffer().toString();
        assert(swBuffer.length() > 0);
        assert(swBuffer.contains(sourceLinePrefix));

        assert(target.memory.concepts.size() > 0);
        
    }
    
}
