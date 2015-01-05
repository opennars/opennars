/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.multistep;

import nars.core.NAR;
import nars.core.build.Default;
import nars.plugin.app.ClassicalConditioningHelper;

/**
 *
 * @author tc
 */
public class ClassicalConditioningHelperTestModule {
    
    
    public static void main(String[] args) {
        ClassicalConditioningHelper blub=new ClassicalConditioningHelper();
        NAR nar=new Default().build();
        nar.addPlugin(blub);
        blub.EnableAutomaticConditioning=false;
        nar.addInput("<a --> M>. :|:"); //abcbbbabc
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
        nar.step(1);
        blub.classicalConditioning();
    }
    
}
