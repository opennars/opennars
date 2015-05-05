/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */

package nars.nal.multistep;

import nars.model.impl.Default;
import nars.NAR;
import nars.op.app.ClassicalConditioningHelper;

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
        nar.frame(6);
        nar.input("<b --> M>. :|:");
        nar.frame(6);
        nar.input("<c --> M>. :|:");
        nar.frame(6);
        nar.input("<b --> M>. :|:");
        nar.frame(6);
        nar.input("<b --> M>. :|:");
        nar.frame(6);
        nar.input("<b --> M>. :|:");
        nar.frame(6);
        nar.input("<a --> M>. :|:");
        nar.frame(6);
        nar.input("<b --> M>. :|:");
        nar.frame(6);
        nar.input("<c --> M>. :|:");
        nar.frame(1);
        blub.classicalConditioning();
    }
    
}
