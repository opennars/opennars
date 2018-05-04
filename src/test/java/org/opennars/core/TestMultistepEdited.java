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
package org.opennars.core;

import org.opennars.main.NAR;
import org.opennars.io.events.TextOutputHandler;
import org.junit.Test;

/**
 * Example-MultiStep-edited.txt
 * @author me
 */
public class TestMultistepEdited {

    @Test
    public void testMultistepEndState() {
        NAR n = new NAR();
        n.addInputFile("nal/Examples/Example-MultiStep-edited.txt");        
        new TextOutputHandler(n, System.out);
        /*InferenceLogger logger = new InferenceLogger(System.out);
        n.memory.setRecorder(logger);*/

        
        n.cycles(1000);
        //System.out.println(n.memory.concepts);
        
    }
            
}
