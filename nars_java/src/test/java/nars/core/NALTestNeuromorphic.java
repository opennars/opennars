/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core;

import nars.core.build.Neuromorphic;
import org.junit.Ignore;

/**
 *
 * @author me
 */

@Ignore
public class NALTestNeuromorphic extends NALTest {

    public NALTestNeuromorphic(String scriptPath) {
        super(scriptPath);
        System.out.println(scriptPath);
    }

    @Override
    public NAR newNAR() {
        return new NAR(new Neuromorphic(16));
    }

    public static void main(String[] args) {        
        Parameters.DEBUG = false;
        Parameters.THREADS = 1;
        runTests(NALTestNeuromorphic.class);
    }
    
    
    
}
