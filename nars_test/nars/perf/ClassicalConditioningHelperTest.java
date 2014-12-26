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
    
    ClassicalConditioningHelper blub=new ClassicalConditioningHelper();
    public static void main(String[] args) {
        NAR nar=new Default().build();
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
    }
    
}
