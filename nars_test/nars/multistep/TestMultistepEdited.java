package nars.multistep;

import nars.NAR;
import nars.config.Plugins;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.core.NALTest;
import org.junit.Test;

/**
 * Example-MultiStep-edited.txt
 * @author me
 */
public class TestMultistepEdited {

    @Test
    public void testMultistepEndState() {
        NAR n = new NAR(new Plugins());
        n.addInput(new TextInput(NALTest.getExample("nal/Examples/Example-MultiStep-edited.txt")));        
        new TextOutput(n, System.out);
        /*InferenceLogger logger = new InferenceLogger(System.out);
        n.memory.setRecorder(logger);*/

        
        n.run(1000);
        //System.out.println(n.memory.concepts);
        
    }
            
}
