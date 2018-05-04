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
/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package org.opennars.core;

import java.util.concurrent.atomic.AtomicBoolean;
import org.opennars.io.events.Events.Answer;
import org.opennars.main.NAR;
import org.opennars.io.events.EventHandler;
import org.opennars.util.test.OutputContainsCondition;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author me
 */
public class VariableTest {
 
    NAR n = new NAR();
    
    @Before public void init() {
        n.addInput("<a --> 3>. :|:");
        n.addInput("<a --> 4>. :/:");        
    }
    
    @Test public void testDepQueryVariableDistinct() {
                          
        n.addInput("<(&/,<a --> 3>,?what) =/> <a --> #wat>>?");
        
        /*
            A "Solved" solution of: <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%
            shouldn't happen because it should not unify #wat with 4 because its not a query variable      
        */        
        new EventHandler(n, true, Answer.class) {            
            @Override public void event(Class event, Object[] args) {
                //nothing should arrive via Solved.class channel
                assertTrue(false);
            }
        };
        
        OutputContainsCondition e = new OutputContainsCondition(n, "=/> <a --> 4>>.", 5);
        
        n.cycles(32);
  
        assertTrue(e.isTrue());
    }
    
    @Test public void testQueryVariableUnification() {
        /*
        <a --> 3>. :|:
        <a --> 4>. :/:
        <(&/,<a --> 3>,?what) =/> <a --> ?wat>>?

        Solved <(&/,<a --> 3>,+3) =/> <a --> 4>>. %1.00;0.31%

        because ?wat can be unified with 4 since ?wat is a query variable
       */

        n.addInput("<(&/,<a --> 3>,?what) =/> <a --> ?wat>>?");
        
        AtomicBoolean solutionFound = new AtomicBoolean(false);
        new EventHandler(n, true, Answer.class) {            
            @Override public void event(Class event, Object[] args) {                
                solutionFound.set(true);
                n.stop();
            }
        };

        n.cycles(1024);
          
        assertTrue(solutionFound.get());
        
    }
}
