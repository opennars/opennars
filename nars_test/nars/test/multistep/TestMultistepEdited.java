package nars.test.multistep;

import nars.core.NAR;
import nars.core.build.Default;
import nars.io.TextInput;
import nars.io.TextOutput;
import nars.test.core.NALTest;
import org.junit.Test;

/**
 * Example-MultiStep-edited.txt
 * @author me
 */
public class TestMultistepEdited {

    @Test
    public void testMultistepEndState() {
        NAR n = new Default().build();
        n.addInput(new TextInput(NALTest.getExample("nal/Examples/Example-MultiStep-edited.txt")));        
        new TextOutput(n, System.out);
        /*InferenceLogger logger = new InferenceLogger(System.out);
        n.memory.setRecorder(logger);*/

        
        n.finish(1000);
        //System.out.println(n.memory.concepts);
        
    }
            
}
