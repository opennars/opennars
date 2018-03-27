package nars.core;

import nars.main.NAR;
import nars.io.events.TextOutputHandler;
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
