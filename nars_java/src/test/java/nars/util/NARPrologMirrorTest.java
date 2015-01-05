/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.util;

import nars.NARPrologMirror;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Default;
import nars.core.NALTestSome;
import nars.entity.Task;
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
    public void testMultistep() {
        boolean prolog = true;
        //boolean showOutput = false;
        Parameters.DEBUG = true;
        
        
        
        NALTestSome nts = new NALTestSome("nal/test/nal1.multistep.nal") {
            
            
            @Override
            public NAR newNAR() {
                NAR nar = new NAR( new Default().setInternalExperience(null) );

                Term aInhd;
                try {
                    aInhd = new Narsese(nar).parseTerm("<a --> d>");
                } catch (Narsese.InvalidInputException ex) {
                    assertTrue(false);
                    return null;
                }
                
                if (prolog) {
                    new NARPrologMirror(nar, 0.5f, true) {

                        @Override
                        public void answer(Task question, Term t, nars.prolog.Term pt) {
                            super.answer(question, t, pt);
                            
                            //look for <a --> d> answer
                            if (t.equals(aInhd))
                                prologAnswered = true;
                        }
                        
                        
                    }.temporal(true, true);
                }
                
                return nar;
            }
          
            
        };
        NALTestSome.showOutput = false;
        NALTestSome.requireSuccess = false;
        NALTestSome.showFail = false;
        nts.run();
                
        assertTrue(prologAnswered);
        
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-MultiStep-edited.txt")));
//        //nar.addInput(new TextInput(new File("nal/Examples/Example-NAL1-edited.txt")));
//        nar.addInput(new TextInput(new File("nal/test/nal1.multistep.nal")));
//        nar.finish(10);
        
        
    }    
    
}
