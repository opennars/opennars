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

import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.util.NARState;
import nars.gui.NARSwing;
import nars.io.TextOutput;
import nars.util.kif.KIFInput;

/**
 *
 * @author me
 */
public class KIFExample {
    
 
    public static void main(String[] args) throws Exception {
        NAR n = new DefaultNARBuilder()
                .setConceptBagSize(2048)
                .setConceptBagLevels(512)
                .build();
                
        n.param.setSilenceLevel(99);
        
        KIFInput k = new KIFInput(n, "/home/me/sigma/KBs/Merge.kif");
        k.setIncludeSubclass(true);
        k.start();
        
        TextOutput t = new TextOutput(n, System.out);
        t.setErrors(true);
        t.setErrorStackTrace(true);

        n.finish(16);
        
        new NARSwing(n);

        /*
        new TextInput(n, "$0.99;0.99$ <Human --> ?x>?");
        new TextInput(n, "$0.99;0.99$ <Human--> {?x}>?");
        new TextInput(n, "$0.99;0.99$ <?x --> Human>?");*/

        
        System.err.println(new NARState(n).measure());
    }
}
