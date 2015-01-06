package nars.multistep;

import nars.core.NAR;
import nars.core.build.Default;
import nars.io.ExampleFileInput;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Example-MultiStep-edited.txt
 * @author me
 */
public class TestMultistepEdited {

    @Test
    public void testMultistepEndState() throws Exception {
        NAR n = new NAR(new Default());
        n.addInput(ExampleFileInput.get("Examples/Example-MultiStep-edited"));
        
        //new TextOutput(n, System.out);
        /*InferenceLogger logger = new InferenceLogger(System.out);
        n.memory.setRecorder(logger);*/
        //System.out.println(n.memory.concepts);
        
        n.run(1000);
        
        
        assertEquals(2488, n.time());
    }
            
}
