package nars.test.multistep;

import nars.core.build.DefaultNARBuilder;
import nars.core.NAR;
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
        NAR n = new DefaultNARBuilder().build();
        n.addInput(new TextInput(NALTest.getExample("nal/Examples/Example-MultiStep-edited.txt")));        
        new TextOutput(n, System.out);
        /*InferenceLogger logger = new InferenceLogger(System.out);
        n.memory.setRecorder(logger);*/

        
        n.finish(10000);
        //System.out.println(n.memory.concepts);
        
    }
            
}
