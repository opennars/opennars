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
package org.opennars.perf;

import org.opennars.core.NALTest;
import org.opennars.main.Nar;
import org.opennars.main.MiscFlags;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Collection;

import static org.opennars.perf.NALStressMeasure.perfNAL;

/**
 * Runs NALTestPerf continuously, for profiling
 */
public class NALPerfLoop {
    
    public static void main(final String[] args) throws IOException, InstantiationException, InvocationTargetException, NoSuchMethodException, ParserConfigurationException, IllegalAccessException, SAXException, ClassNotFoundException, ParseException {
       
        final int repeats = 2;
        final int warmups = 1;
        final int maxConcepts = 2000;
        final int extraCycles = 2048;
        final int randomExtraCycles = 512;
          
        final Nar n = new Nar();
        //Nar n = new Nar( new Neuromorphic(16).setConceptBagSize(maxConcepts) );
        //Nar n = new Nar(new Curve());
        
        //Nar n = new Discretinuous().setConceptBagSize(maxConcepts).build();

        //new NARPrologMirror(n,0.75f, true).temporal(true, true);              
        
        final Collection c = NALTest.params();
        while (true) {
            for (final Object o : c) {
                final String examplePath = (String)((Object[])o)[0];
                MiscFlags.DEBUG = false;
                
                perfNAL(n, examplePath,extraCycles+ (int)(Math.random()*randomExtraCycles),repeats,warmups,true);
            }
        }        
    }
}
