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
import java.util.logging.Level;
import java.util.logging.Logger;
import nars.core.NAR;
import nars.gui.NARSwing;
import nars.util.Number1DInput;
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
    public void test1() throws Exception {
        int N = 3;
        
        double[] x = randomArray(N, 1.0, 0);
        
        NAR n = new NAR();
        n.start(0);
        
        new TextOutput(n, System.out);
        
        Number1DInput v = new Number1DInput(n, "myVector", x, 3);
        for (int i = 0; i < 10; i++) {
            v.next(randomArray(N, 1.0, 0));
            
        }
        v.close();
        

        
        
        //Assert.assertTrue(true);        
    }
    
    public static void main(String[] args) throws Exception {
        new Number1DInputTest().test1();
    }
}
