/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.NARPrologMirror;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.entity.Task;
import nars.io.ExampleFileInput;
import nars.io.narsese.Narsese;
import nars.language.Term;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author me
 */
public class NARPrologMirrorTest {
    
    boolean prologAnswered = false;
    
    @Test
    public void testMultistep() throws Exception {
        boolean prolog = true;
        //boolean showOutput = false;
        Parameters.DEBUG = true;

        NAR nar = new NAR( new Default().setInternalExperience(null) );

        new NARPrologMirror(nar, 0.5f, true, true, true) {

            
            @Override
            public Term answer(Task question, Term t, nars.prolog.Term pt) {
                Term r = super.answer(question, t, pt);

                //look for <a --> d> answer
                //if (t.equals(aInhd))
                    prologAnswered = true;
                
                return r;
            }


        };        
        
        NALPerformance nts = new NALPerformance(nar, ExampleFileInput.get("../nal/test/nal1.multistep.nal").getSource(), 500) {
//            
//            
//            @Override
//            public NAR newNAR() {
//
//                Term aInhd;
//                try {
//                    aInhd = new Narsese(nar).parseTerm("<a --> d>");
//                } catch (Narsese.InvalidInputException ex) {
//                    assertTrue(false);
//                    return null;
//                }
//                
//                if (prolog) {
//
//                }
//                
//                return nar;
//            }
//          
            
        };

        nts.run();
                
        assertTrue(prologAnswered);
        
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-MultiStep-edited.txt")));
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-NAL1-edited.txt")));
//        nar.addInput(new TextInput(new File("nal/test/nal1.multistep.nal")));
//        nar.finish(10);
        
        
    }    
    
}
