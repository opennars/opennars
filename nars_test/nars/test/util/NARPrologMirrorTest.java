/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.util;

import nars.NARPrologMirror;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.test.core.NALTestSome;
import org.junit.Test;

/**
 *
 * @author me
 */
public class NARPrologMirrorTest {
    
    @Test
    public void testMultistep() {
        boolean prolog = true;
        boolean showOutput = false;
        Parameters.DEBUG = true;
        
        new NALTestSome("nal/test/nal1.multistep.nal", showOutput) {

            @Override
            public NAR newNAR() {
                NAR nar = new Default().build();

                if (prolog) {
                    new NARPrologMirror(nar, 0.5f, true).temporal(true, true);
                }
                
                return nar;
            }
          
            
        }.run();
                
        
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-MultiStep-edited.txt")));
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-NAL1-edited.txt")));
//        nar.addInput(new TextInput(new File("nal/test/nal1.multistep.nal")));
//        nar.finish(10);
        
        
    }    
    
}
