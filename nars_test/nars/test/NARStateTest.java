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

package nars.test;

import java.util.HashMap;
import nars.core.NAR;
import nars.core.NARState;
import nars.io.Number1DInput;
import static nars.test.Number1DInputTest.randomArray;

/**
 *
 * @author me
 */
public class NARStateTest {
 
    public static void main(String[] args) {
        int N = 2;
        
        double[] x = randomArray(N, 1.0, 0);
        
        NAR n = new NAR();
                
        new Number1DInput(n, "x", x);
        n.run(16);
        
        
        HashMap<String, Object> d = new NARState(n).measure();
        
        System.out.println(d);
    }    
}
