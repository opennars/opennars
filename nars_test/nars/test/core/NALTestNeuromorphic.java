/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.test.core;

import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Neuromorphic;

/**
 *
 * @author me
 */
public class NALTestNeuromorphic extends NALTest {

    public NALTestNeuromorphic(String scriptPath) {
        super(scriptPath);
    }

    @Override
    public NAR newNAR() {
        return new Neuromorphic().build();
    }

    public static void main(String[] args) {        
        Parameters.DEBUG = false;
        Parameters.THREADS = 1;
        runTests(NALTestNeuromorphic.class);
    }
    
    
    
}
