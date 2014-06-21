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

import static javax.swing.text.html.HTML.Attribute.N;
import nars.core.NAR;
import nars.io.Number1DInput;
import nars.io.TextOutput;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author me
 */
public class Number1DInputTest {

    public static double[] randomArray(int size, double scale, double min) {
        double[] d = new double[size];
        for (int i = 0; i < size; i++) {
            d[i] = Math.random() * scale + min;
        }
        return d;
    }
    
    
    @Test
    public void test1() {
        int N = 8;
        
        double[] x = randomArray(N, 1.0, 0);
        double[] y = randomArray(N, 1.0, 0);
        
        NAR n = new NAR();
        
        //new TextOutput(n, System.out);
        
        new Number1DInput(n, "x", x);
        n.run(10);
        new Number1DInput(n, "y", y);
        n.run(2000);
        Assert.assertTrue(true);        
    }
    
    
}
