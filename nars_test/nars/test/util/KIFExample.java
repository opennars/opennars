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

package nars.test.util;

import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.RealTimeNARBuilder;
import nars.gui.NARSwing;
import nars.io.TextOutput;
import nars.util.NARState;
import nars.util.kif.KIFInput;

/**
 *
 * @author me
 */
public class KIFExample {
    
 
    public static void main(String[] args) throws Exception {
        Parameters.DEFAULT_JUDGMENT_PRIORITY = 0.5f;
        
        NAR n = new RealTimeNARBuilder(true)
                .setConceptBagSize(16192)
                .build();
                
        
        n.param().noiseLevel.set(40);
        
        KIFInput k = new KIFInput("/home/me/sigma/KBs/Merge.kif");
        k.setIncludeSubclass(true);
        k.setIncludeInstance(true);
        k.setIncludeSubrelation(true);
        //k.setIncludeDisjoint(true);
        k.setIncludeRelatedInternalConcept(true);
        
        
        //start before adding input to begin filling buffer
        k.start();
        n.addInput(k);
        
        
        TextOutput t = new TextOutput(n, System.out);
        t.setErrors(true);
        t.setErrorStackTrace(true);

        
        n.finish(4000);
        //n.param().cycleMemory.set(512);
        
        t.stop();
        
        new NARSwing(n);
        

        /*
        new TextInput(n, "$0.99;0.99$ <Human --> ?x>?");
        new TextInput(n, "$0.99;0.99$ <Human--> {?x}>?");
        new TextInput(n, "$0.99;0.99$ <?x --> Human>?");*/

        
        System.err.println(new NARState(n).measure());
    }
}
