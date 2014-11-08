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
        System.out.println(scriptPath);
    }

    @Override
    public NAR newNAR() {
        return NAR.build(new Neuromorphic(16));
    }

    public static void main(String[] args) {        
        Parameters.DEBUG = true;
        Parameters.THREADS = 1;
        runTests(NALTestNeuromorphic.class);
    }
    
    
    
}
