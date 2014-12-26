/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.perf;

import nars.core.NAR;
import nars.core.build.Default;
import nars.plugin.mental.ClassicalConditioningHelper;

/**
 *
 * @author tc
 */
public class ClassicalConditioningHelperTest {
    
    
    public static void main(String[] args) {
        ClassicalConditioningHelper blub=new ClassicalConditioningHelper();
        NAR nar=new Default().build();
        nar.addPlugin(blub);
        blub.EnableAutomaticConditioning=false;
        nar.addInput("<a --> M>. :|:");
        nar.step(6);
        nar.addInput("<b --> M>. :|:");
        nar.step(6);
        nar.addInput("<c --> M>. :|:");
        nar.step(6);
        nar.addInput("<b --> M>. :|:");
        nar.step(6);
        nar.addInput("<b --> M>. :|:");
        nar.step(6);
        nar.addInput("<b --> M>. :|:");
        nar.step(6);
        nar.addInput("<a --> M>. :|:");
        nar.step(6);
        nar.addInput("<b --> M>. :|:");
        nar.step(6);
        nar.addInput("<c --> M>. :|:");
        blub.classicalConditioning();
    }
    
}
