package nars.logic.multistep;

import nars.build.Default;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.ExampleFileInput;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example-MultiStep-edited.txt
 * @author me
 */
public class TestMultistepEdited {

    @Test
    public void testMultistepEndState() throws Exception {

        Parameters.DEBUG = true;

        NAR n = new NAR(new Default());
        n.input(ExampleFileInput.get(n, "example/Example-MultiStep-edited"));
        
        //new TextOutput(n, System.out);
        /*InferenceLogger logger = new InferenceLogger(System.out);
        n.memory.setRecorder(logger);*/
        //System.out.println(n.memory.concepts);
        
        n.run(100);
        
        
        assertEquals(997, n.time());
    }
            
}
