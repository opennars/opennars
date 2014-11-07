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

import java.io.IOException;
import nars.core.NAR;
import nars.core.build.Default;
import static nars.test.util.Number1DInputTest.randomArray;
import nars.util.meter.data.DataSet;
import org.junit.Test;

/**
 *
 * @author me
 */
public class NARStateTest {
 
    @Test
    public void testNARState() throws IOException {
        int N = 2;
        
        double[] x = randomArray(N, 1.0, 0);
        
        NAR n = new Default().build();
                
        //new Number1DInput(n, "x", x, 2);
        n.finish(16);
        

        n.memory.logic.setActive(true);
        n.memory.logic.update(n.memory);
        DataSet d = n.memory.logic;

        assert(d.keySet().size() > 0);
        
    }
    
}
