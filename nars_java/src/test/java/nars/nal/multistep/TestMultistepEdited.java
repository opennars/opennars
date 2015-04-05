package nars.nal.multistep;

import nars.Global;
import nars.NAR;
import nars.io.ExampleFileInput;
import nars.io.TextOutput;
import nars.prototype.Default;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example-MultiStep-edited.txt
 * @author me
 */
public class TestMultistepEdited {

    @Test
    public void testMultistepEndState() throws Exception {

        Global.DEBUG = true;

        NAR n = new NAR(new Default());
        n.input(ExampleFileInput.get(n, "example/Example-MultiStep-edited"));
        
        new TextOutput(n, System.out);
        /*InferenceLogger logger = new InferenceLogger(System.out);
        n.memory.setRecorder(logger);*/
        //System.out.println(n.memory.concepts);
        
        n.run(100);
        
        
        assertEquals(997, n.time());
    }
            
}
