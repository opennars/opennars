/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.NAR;
import nars.config.Plugins;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.io.Answered;
import nars.io.Narsese;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static nars.language.Tense.Eternal;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author me
 */
public class NALQueryTest {
        
    
    @Test 
    public void testQuery1() throws Narsese.InvalidInputException {
        testQueryAnswered(0, 8);
        testQueryAnswered(8, 0);
    }
    
    public void testQueryAnswered(int cyclesBeforeQuestion, int cyclesAfterQuestion) throws Narsese.InvalidInputException {
        
        final AtomicBoolean b = new AtomicBoolean(false);
        
        String question = cyclesBeforeQuestion == 0 ?
                "<a --> b>" /* unknown solution to be derived */ : 
                "<b --> a>" /* existing solution, to test finding existing solutions */;
        
        new NAR(new Plugins()).
                
                believe("<a <-> b>", Eternal, 1.0f, 0.5f).
                
                believe("<b --> a>", Eternal, 1.0f, 0.5f).      
                
                run(cyclesBeforeQuestion).
                
                ask(question, new Answered() {

                    @Override
                    public void onSolution(Sentence belief) {
                        //System.out.println("solution: " + belief);
                        b.set(true);
                        off();
                    }

                    @Override
                    public void onChildSolution(Task child, Sentence belief) {
                        //System.out.println("  child: " + 
                        //child + " solution: " + belief);
                    }            
                }).
                
                run(cyclesAfterQuestion);
                
        assertTrue(b.get());
        
    }
}
