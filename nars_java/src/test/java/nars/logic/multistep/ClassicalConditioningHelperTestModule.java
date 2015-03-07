/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.logic.multistep;

import nars.build.Default;
import nars.core.NAR;
import nars.operator.app.ClassicalConditioningHelper;

/**
 *
 * @author tc
 */
public class ClassicalConditioningHelperTestModule {
    
    
    public static void main(String[] args) {
        ClassicalConditioningHelper blub=new ClassicalConditioningHelper();
        NAR nar= new NAR(new Default());
        nar.on(blub);
        blub.EnableAutomaticConditioning=false;
        nar.input("<a --> M>. :|:"); //abcbbbabc
        nar.step(6);
        nar.input("<b --> M>. :|:");
        nar.step(6);
        nar.input("<c --> M>. :|:");
        nar.step(6);
        nar.input("<b --> M>. :|:");
        nar.step(6);
        nar.input("<b --> M>. :|:");
        nar.step(6);
        nar.input("<b --> M>. :|:");
        nar.step(6);
        nar.input("<a --> M>. :|:");
        nar.step(6);
        nar.input("<b --> M>. :|:");
        nar.step(6);
        nar.input("<c --> M>. :|:");
        nar.step(1);
        blub.classicalConditioning();
    }
    
}
