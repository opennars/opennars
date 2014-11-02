/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.util;

import nars.NARProlog;
import nars.NARPrologMirror;
import nars.core.NAR;
import nars.core.build.DefaultNARBuilder;
import nars.test.core.NALTestSome;

/**
 *
 * @author me
 */
public class NARPrologMirrorTest {
    
    public static void main(String[] args) throws Exception {
        
        boolean prolog = true;
        boolean showOutput = true;
        
        new NALTestSome("nal/test/nal1.multistep.nal", showOutput) {
        //new NALTestSome("nal/test/nars_multistep_1.nal", showOutput) {

            @Override
            public NAR newNAR() {
                NAR nar = new DefaultNARBuilder().build();

                if (prolog) {
                    NARProlog prolog = null;
                    try {
                        prolog = new NARProlog(nar);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);                    
                    }

                    new NARPrologMirror(nar, prolog).temporal(true, false);
                    new NARPrologMirror(nar, prolog).temporal(false, true);
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
